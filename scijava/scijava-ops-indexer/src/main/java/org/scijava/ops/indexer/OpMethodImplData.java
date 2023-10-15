/*-
 * #%L
 * SciJava Ops Indexer: An annotation processor for indexing Ops
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link OpImplData} implementation handling {@link Method}s annotated with
 * "implNote op"
 *
 * @author Gabriel Selzer
 */
public class OpMethodImplData extends OpImplData {

	public OpMethodImplData(ExecutableElement source, String doc,
		ProcessingEnvironment env)
	{
		super(source, doc, env);
	}

	/**
	 * Parse javadoc tags pertaining exclusively to {@link Method}s
	 * 
	 * @param source the {@link Element} representing the {@link Method}. In
	 *          practice, this will always be an {@link ExecutableElement}
	 * @param additionalTags the tags pertaining exclusively to {@link Method}s.
	 */
	@Override
	void parseAdditionalTags(Element source, List<String[]> additionalTags) {
		ExecutableElement exSource = (ExecutableElement) source;
		// First, parse parameters
		List<VariableElement> opDependencies = new ArrayList<>();
		var paramItr = exSource.getParameters().iterator();
		for (String[] tag : additionalTags) {
			if (!"@param".equals(tag[0])) continue;
			if (paramIsTypeVariable(tag[1])) {
				// Ignore type variables
				continue;
			}
			VariableElement param = paramItr.next();
			// HACK A dependency on SciJava Ops SPI is really tricky - creates a
			// circular dependency so this is the easiest way to check for an
			// OpDependency
			boolean isOpDep = param.getAnnotationMirrors().stream() //
					.anyMatch(a -> a.toString().contains("OpDependency"));
			if (isOpDep)
				opDependencies.add(param);
			else {
				// Coerce @param tag + VariableElement into an OpParameter
				String name = param.getSimpleName().toString();
				String type = param.asType().toString();
				String remainder = tag[1];
				String description;
				if (remainder.contains(" ")) {
					description = remainder.substring(remainder.indexOf(" "));
				}
				else {
					description = "";
				}
				params.add(new OpParameter(name, type, OpParameter.IO_TYPE.INPUT,
						description));
			}
		}
		// Validate number of inputs
		if (opDependencies.size() + params.size() != exSource
				.getParameters().size())
		{
			env.getMessager().printMessage(Diagnostic.Kind.ERROR,
					"The number of @param tags on " + exSource +
							" does not match the number of parameters!");
		}

		// Finally, parse the return
		Optional<String[]> returnTag = additionalTags.stream() //
			.filter(t -> t[0].equals("@return")).findFirst();
		if (returnTag.isPresent()) {
			String returnType = exSource.getReturnType().toString();
			params.add(new OpParameter( //
				"output", //
				returnType, //
				OpParameter.IO_TYPE.OUTPUT, //
				returnTag.get()[1] //
			));
		}
		// Validate number of outputs
		if (!(exSource.getReturnType() instanceof NoType) && returnTag.isEmpty()) {
			env.getMessager().printMessage(Diagnostic.Kind.ERROR, exSource +
				" has a return, but no @return parameter");
		}
	}

	/**
	 * HACK to find type variable param tags
	 * For a parameter tag, returns {@code true} iff the following tag is a type variable tag.
	 * Type variable tags start with a greater than sign, and then has a string of letters, and then a less than sign.
	 * @param tag the string following an param tag
	 * @return true iff the tag is an param tag
	 */
	private boolean paramIsTypeVariable(String tag) {
		// TODO: Why doesn't Pattern.matches(".*<\\p{L}>.*", tag) work??
		if (tag.charAt(0) != '<') return false;
		for(int i = 1; i < tag.length(); i++) {
			char c = tag.charAt(i);
			if (Character.isLetter(c)) continue;
			return c == '>';
		}
		return false;
	}

	protected String formulateSource(Element source) {
		ExecutableElement exSource = (ExecutableElement) source;
		// First, append the class
		StringBuilder sb = new StringBuilder();
		sb.append(source.getEnclosingElement());
		sb.append(".");
		// Then, append the method
		sb.append(source.getSimpleName());

		// Then, append the parameters
		var params = exSource.getParameters();
		sb.append("(");
		for (int i = 0; i < params.size(); i++) {
			var d = env.getTypeUtils().erasure(params.get(i).asType());
			sb.append(d);
			if (i < params.size() - 1) {
				sb.append(",");
			}
		}
		sb.append(")");

		return "javaMethod:/" + URLEncoder.encode(sb.toString(),
			StandardCharsets.UTF_8);
	}
}
