
package org.scijava.ops.indexer;

import static org.scijava.ops.indexer.ProcessingUtils.blockSeparator;
import static org.scijava.ops.indexer.ProcessingUtils.tagElementSeparator;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;

/**
 * {@link OpImplData} implementation handling {@link Class}es annotated with
 * "implNote op"
 * 
 * @author Gabriel Selzer
 */
public class OpClassImplData extends OpImplData {

	/**
	 * @param source the {@link TypeElement} that is an "implNote op"
	 * @param fMethod the functional method of the Op
	 * @param doc the Javadoc on {@code source}
	 * @param fMethodDoc the Javadoc on the functional method {@code fMethod}
	 * @param env the {@link ProcessingEnvironment} used for annotation processing
	 */
	public OpClassImplData(TypeElement source, ExecutableElement fMethod,
		String doc, String fMethodDoc, ProcessingEnvironment env)
	{
		super(source, doc, env);
		parseFunctionalMethod(fMethod, fMethodDoc);
	}

	private void parseFunctionalMethod(ExecutableElement fMethod,
		String fMethodDoc)
	{
		if (fMethodDoc == null || fMethodDoc.isEmpty()) return;
		String[] sections = blockSeparator.split(fMethodDoc);
		var paramTypes = fMethod.getParameters().iterator();

		int expNoParams = fMethod.getParameters().size();
		int expNoReturns = fMethod.getReturnType() instanceof NoType ? 0 : 1;
		int noParams = 0, noReturns = 0;

		for (String section : sections) {
			String[] elements = tagElementSeparator.split(section, 2);
			switch (elements[0]) {
				case "@param": {
					noParams++;
					String[] foo = tagElementSeparator.split(elements[1], 2);
					String name = foo[0];
					String description = foo[1];
					if (paramTypes.hasNext()) {
						String type = paramTypes.next().asType().toString();
						params.add(new OpParameter(name, type, OpParameter.IO_TYPE.INPUT,
							description));
					}
					else {
						throw new IllegalArgumentException("Op " + this.source + " has " +
							noParams + " @param tags, but the functional method only has " +
							expNoParams + " parameters!");
					}
					break;
				}
				case "@return": {
					noReturns++;
					String name = "output";
					String description = elements[1];
					String type = fMethod.getReturnType().toString();
					params.add(new OpParameter(name, type, OpParameter.IO_TYPE.OUTPUT,
						description));
					break;
				}
				case "@author":
					addAuthor(tagElementSeparator.split(section, 2)[1]);
					break;
			}
		}
		if (noParams != expNoParams) {
			throw new IllegalArgumentException("Op " + this.source + " has " +
				noParams + " @param tags, but the functional method " + fMethod +
				" only has " + expNoParams + " parameters!");
		}
		if (noReturns != expNoReturns) {
			throw new IllegalArgumentException("Op " + this.source + " has " +
				noReturns + " @return tags, but the functional method only has " +
				expNoReturns + " returns!");
		}
	}

	/**
	 * Parse javadoc tags pertaining exclusively to {@link Class}es
	 *
	 * @param source the {@link Element} representing the {@link Class}.
	 * @param additionalTags the tags that pertain particularly to {@link Class}es
	 */
	@Override
	void parseAdditionalTags(Element source, List<String[]> additionalTags) {}

	protected String formulateSource(Element source) {
		var srcString = source.toString();
		var parent = source.getEnclosingElement();
		// handle inner classes
		while (parent.getKind() == ElementKind.CLASS) {
			int badPeriod = srcString.lastIndexOf('.');
			srcString = srcString.substring(0, badPeriod) + '$' + srcString.substring(
				badPeriod + 1);
			parent = parent.getEnclosingElement();
		}
		return "javaClass:/" + URLEncoder.encode(srcString, StandardCharsets.UTF_8);
	}

}
