/*-
 * #%L
 * An annotation processor for indexing Ops with javadoc.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.indexer;

import static org.scijava.ops.indexer.ProcessingUtils.isNullable;
import static org.scijava.ops.indexer.ProcessingUtils.tagElementSeparator;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
class OpFieldImplData extends OpImplData {

	// Regex to match Op types, coming from SciJava Function, who describe all
	// of their parameters in order within their type parameters
	private static final Pattern SJF_PATTERN = Pattern.compile(
		"^org\\.scijava\\.function\\.(Computers|Functions|Inplaces|Producer).*");
	// Regex to match Op types, coming from vanilla Java, who describe all of
	// their parameters in order within their type parameters
	private static final Pattern JAVA_PATTERN = Pattern.compile(
		"^java\\.util\\.function\\.(Function|BiFunction).*");

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
		// Get the types of each Op parameter.
        var itr = getParamTypes(source, additionalTags).iterator();
		// Create the list of Op parameters by checking for @input, @container,
		// @mutable, @output tags
		for (var tag : additionalTags) {
			// In the case where parameter types cannot be determined, describe the
			// types as "UNKNOWN"
            var pType = itr.hasNext() ? itr.next() : "UNKNOWN";
			switch (tag[0]) {
				case "@input":
                    var inData = tagElementSeparator.split(tag[1], 2);
					params.add(new OpParameter(inData[0], pType,
						OpParameter.IO_TYPE.INPUT, inData[1], isNullable(inData[1])));
					break;
				case "@output":
					// NB outputs generally don't have names
					params.add(new OpParameter("output", pType,
						OpParameter.IO_TYPE.OUTPUT, tag[1], false));
					break;
				case "@container":
                    var containerData = tagElementSeparator.split(tag[1], 2);
					params.add(new OpParameter(containerData[0], pType,
						OpParameter.IO_TYPE.CONTAINER, containerData[1], false));
					break;
				case "@mutable":
                    var mutableData = tagElementSeparator.split(tag[1], 2);
					params.add(new OpParameter(mutableData[0], pType,
						OpParameter.IO_TYPE.MUTABLE, mutableData[1], false));
					break;
			}

		}

		// With the number of inputs and outputs collected, validate that we have
		// the correct number of eaach
        var fieldType = env.getTypeUtils().asElement(source.asType());
		if (fieldType instanceof TypeElement) {
			// Find functional method of the Op type
            var fMethod = ProcessingUtils.findFunctionalMethod(env,
				(TypeElement) fieldType);
			// Determine number of outputs (in practice, always 0 or 1)
            var numReturns = 0;
			for (var p : params) {
				if (p.ioType == OpParameter.IO_TYPE.OUTPUT) {
					numReturns++;
				}
			}
			// Compare number of outputs with the number of @output tags
            var expNumReturns = fMethod.getReturnType() instanceof NoType ? 0 : 1;
			if (expNumReturns != numReturns) {
				env.getMessager().printMessage(Diagnostic.Kind.ERROR, this.source +
					" has " + numReturns + " @output tag(s) when it should have " +
					expNumReturns);
			}
			// Compare number of inputs with the number of @input, @container,
			// @mutable tags
            var numParams = params.size() - numReturns;
            var expNumParams = fMethod.getParameters().size();
			if (numParams != expNumParams) {
				env.getMessager().printMessage(Diagnostic.Kind.ERROR, this.source +
					" has " + numParams +
					" @input/@container/@mutable tag(s) when it should have " +
					expNumParams);
			}

		}
	}

	/**
	 * Helper method that provides a best attempt at finding the parameter types
	 * of an Op written as a Java field. Because we haven't yet compiled the
	 * field, we cannot introspect the class hierarchy to determine the parameter
	 * types of the functional method. This function uses pre-existing knowledge
	 * that a set of common functional interfaces describe all of their parameter
	 * types via their type parameters, and parses out those type variables to
	 * provide a list of Op parameter types. If the Op field does not conform to
	 * one of those specified functional interfaces, this method returns an empty
	 * list.
	 *
	 * @param source the Op field {@link Element}
	 * @param additionalTags the Javadoc belonging to {@code source}, split by
	 *          Javadoc tag. Used to ensure that the method returns the correct
	 *          number of parameter types.
	 * @return a {@link List} containing a string representing the type of each Op
	 *         parameter, or an empty {@link List} if that cannot be done.
	 */
	private List<String> getParamTypes(Element source,
		List<String[]> additionalTags)
	{
		var fieldStr = source.asType().toString();
		if ( //
		!SJF_PATTERN.matcher(fieldStr).find() && //
			!JAVA_PATTERN.matcher(fieldStr).find() //
		) {
			env.getMessager().printMessage(Diagnostic.Kind.WARNING, "Op Field " +
				source + " has type" + fieldStr +
				" - we cannot infer parameter types from this type!");
			return Collections.emptyList();
		}

		// Find the enclosing class, so we can grab all the type variables
        var enclosing = source.getEnclosingElement();
		while (enclosing.getKind() != ElementKind.CLASS) {
			enclosing = enclosing.getEnclosingElement();
		}

		// Replace all instances of each type variable in the field's type
		// string.
		for (var e : ((TypeElement) enclosing).getTypeParameters()) {
			// Convert the type variable into a string representation
            var tpString = new StringBuilder(e.toString()).append(
				" extends ");
			var bounds = e.getBounds();
			for (var i = 0; i < bounds.size(); i++) {
				tpString.append(bounds.get(i).toString());
				if (i < bounds.size() - 1) {
					tpString.append(" & ");
				}
			}
			// Replace each instance of the type variable with the stringification.
			var regex = "(?<![a-zA-Z])" + e + "(?![a-zA-Z])";
			fieldStr = fieldStr.replaceAll(regex, tpString.toString());
		}

		// Next, parse out the parameters from the field type e.g.
		// "Function<T, U>" -> "T, U"
		var ParamsStr = fieldStr.substring( //
			fieldStr.indexOf('<') + 1, //
			fieldStr.length() - 1 //
		);
		// Split the type parameters by comma, taking care to avoid nested commas
		List<String> paramTypes = new ArrayList<>();
        var tmp = new StringBuilder();
        var nestCount = 0;
		for (var i = 0; i < ParamsStr.length(); i++) {
			if (ParamsStr.charAt(i) == '<') {
				tmp.append(ParamsStr.charAt(i));
				nestCount++;
			}
			else if (ParamsStr.charAt(i) == '>') {
				tmp.append(ParamsStr.charAt(i));
				nestCount--;
			}
			else if (ParamsStr.charAt(i) == ',' && nestCount == 0) {
				paramTypes.add(tmp.toString());
				tmp = new StringBuilder();
			}
			else {
				tmp.append(ParamsStr.charAt(i));
			}
		}
		paramTypes.add(tmp.toString());

		// Finally, a sanity check - ensure correct number of types.
		if (paramTypes.size() != additionalTags.size()) {
			env.getMessager().printMessage(Diagnostic.Kind.WARNING,
				"Could not infer parameter types from Field type.");
			return Collections.emptyList();
		}

		return paramTypes;
	}

	@Override
	String formulateSource(Element source) {
		return "javaField:/" + URLEncoder.encode(source.getEnclosingElement() +
			"$" + source, StandardCharsets.UTF_8);
	}

}
