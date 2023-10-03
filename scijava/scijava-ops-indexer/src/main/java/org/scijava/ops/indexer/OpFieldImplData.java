
package org.scijava.ops.indexer;

import static org.scijava.ops.indexer.ProcessingUtils.tagElementSeparator;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.tools.Diagnostic;

/**
 * {@link OpImplData} implementation handling {@link Field}s annotated with
 * "implNote op"
 *
 * @author Gabriel Selzer
 */
public class OpFieldImplData extends OpImplData {

	public OpFieldImplData(Element source, String doc,
		ProcessingEnvironment env)
	{
		super(source, doc, env);
	}

	/**
	 * Parse javadoc tags pertaining exclusively to {@link Field}s
	 *
	 * @param source the {@link Element} representing the {@link Field}.
	 * @param additionalTags the tags pertaining exclusively to {@link Field}s
	 */
	@Override
	void parseAdditionalTags(Element source, List<String[]> additionalTags) {
		// Create the list of Op parameters by checking for @input, @container, @mutable, @output tags
		for (String[] tag : additionalTags) {
			switch (tag[0]) {
				case "@input":
					String[] inData = tagElementSeparator.split(tag[1], 2);
					params.add(new OpParameter(inData[0], null, OpParameter.IO_TYPE.INPUT,
						inData[1]));
					break;
				case "@output":
					// NB outputs generally don't have names
					params.add(new OpParameter("output", null, OpParameter.IO_TYPE.OUTPUT,
						tag[1]));
					break;
				case "@container":
					String[] containerData = tagElementSeparator.split(tag[1], 2);
					params.add(new OpParameter(containerData[0], null,
						OpParameter.IO_TYPE.CONTAINER, containerData[1]));
					break;
				case "@mutable":
					String[] mutableData = tagElementSeparator.split(tag[1], 2);
					params.add(new OpParameter(mutableData[0], null,
						OpParameter.IO_TYPE.MUTABLE, mutableData[1]));
					break;
			}

		}

		// With the number of inputs and outputs collected, validate that we have the correct number of eaach
		Element fieldType = env.getTypeUtils().asElement(source.asType());
		if (fieldType instanceof TypeElement) {
			// Find functional method of the Op type
			ExecutableElement fMethod = ProcessingUtils
				.findFunctionalMethod(env, (TypeElement) fieldType);
			// Determine number of outputs (in practice, always 0 or 1)
			int numReturns = 0;
			for (OpParameter p : params) {
				if (p.ioType == OpParameter.IO_TYPE.OUTPUT) {
					numReturns++;
				}
			}
			// Compare number of outputs with the number of @output tags
			int expNumReturns = fMethod.getReturnType() instanceof NoType ? 0 : 1;
			if (expNumReturns != numReturns) {
				env.getMessager().printMessage(Diagnostic.Kind.ERROR, this.source +
					" has " + numReturns + " @output tag(s) when it should have " +
					expNumReturns);
			}
			// Compare number of inputs with the number of @input, @container, @mutable tags
			int numParams = params.size() - numReturns;
			int expNumParams = fMethod.getParameters().size();
			if (numParams != expNumParams) {
				env.getMessager().printMessage(Diagnostic.Kind.ERROR, this.source +
					" has " + numParams +
					" @input/@container/@mutable tag(s) when it should have " +
					expNumParams);
			}

		}
	}

	@Override
	String formulateSource(Element source) {
		return "javaField:/" + URLEncoder.encode(source.getEnclosingElement() +
			"$" + source, StandardCharsets.UTF_8);
	}

}
