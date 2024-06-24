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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link OpImplData} implementation handling {@link Method}s annotated with
 * "implNote op"
 *
 * @author Gabriel Selzer
 */
class OpMethodImplData extends OpImplData {

	// Regex matching org.scijava.function.Computers type hints
	private static final Pattern COMPUTER_TYPE = Pattern.compile(
		"[cC]omputer(\\d*)$");
	// Regex matching org.scijava.function.Inplaces type hints
	private static final Pattern INPLACE_TYPE = Pattern.compile(
		"[iI]nplace(\\d+)$");

	public OpMethodImplData(ExecutableElement source, String doc,
		ProcessingEnvironment env)
	{
		super(source, doc, env);
		validateMethod(source);
	}

	/**
	 * Catches some Op errors early, to prevent confusing errors at runtime.
	 *
	 * @param source the {@link ExecutableElement} referring to an Op described as
	 *          a method.
	 */
	private void validateMethod(ExecutableElement source) {
		// Allow only public methods
		if (!source.getModifiers().contains(Modifier.PUBLIC)) {
			printError(source, " should be a public method!");
		}
		// Allow only static methods
		if (!source.getModifiers().contains(Modifier.STATIC)) {
			printError(source, " should be a static method!");
		}
		// All Op dependencies must come before other parameters
		int lastOpDependency = -1;
		List<? extends VariableElement> params = source.getParameters();
		for (int i = 0; i < params.size(); i++) {
			if (isDependency(params.get(i))) {
				if (i != lastOpDependency + 1) {
					printError(source,
						" declares Op dependencies after it declares parameters - all Op dependencies must come first!");
					break;
				}
				lastOpDependency++;
			}

		}

	}

	private boolean isDependency(VariableElement e) {
		// HACK A dependency on SciJava Ops SPI is really tricky - creates a
		// circular dependency so this is the easiest way to check for an
		// OpDependency
		return e.getAnnotationMirrors().stream() //
			.anyMatch(a -> a.toString().contains("OpDependency"));
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
		// First, parse @param tags
		List<VariableElement> opDependencies = new ArrayList<>();
		Iterator<? extends VariableElement> paramItr = exSource.getParameters().iterator();

		for (String[] tag : additionalTags) {
			if (!"@param".equals(tag[0])) continue;
			if (paramIsTypeVariable(tag[1])) {
				// Ignore type variables
				continue;
			}
			VariableElement param = paramItr.next();
			if (isDependency(param)) {
				opDependencies.add(param);
			}
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
				params.add(new OpParameter( //
					name, //
					type, //
					ProcessingUtils.ioType(description), //
					description, //
					ProcessingUtils.isNullable(param, description) //
				));
			}
		}

		// We also leave the option to specify the Op type using the type tag -
		// check for it, and apply it if available.
		if (tags.containsKey("type")) {
			editIOIndex((String) tags.get("type"), params);
		}
		// Validate number of inputs
		if (opDependencies.size() + params.size() != exSource.getParameters()
			.size())
		{
			printError(exSource,
				" does not have a matching @param tag for each of its parameters!");
		}

		// Finally, parse the return
        Optional<String[]> returnTag = additionalTags.stream() //
			.filter(t -> t[0].startsWith("@return")).findFirst();
		if (returnTag.isPresent()) {
            String totalTag = String.join(" ", returnTag.get());
			totalTag = totalTag.replaceFirst("[^\\s]+\\s", "");
            String returnType = exSource.getReturnType().toString();
			params.add(new OpParameter( //
				"output", //
				returnType, //
				OpParameter.IO_TYPE.OUTPUT, //
				totalTag, //
				false //
			));
		}

		// Validate 0 or 1 outputs
		int totalOutputs = 0;
		for (OpParameter p : params) {
			if (p.ioType != OpParameter.IO_TYPE.INPUT) {
				totalOutputs++;
			}
		}
		if (totalOutputs > 1) {
			printError(exSource,
				" is only allowed to have 0 or 1 parameter outputs!");
		}

		// Validate number of outputs
		if (!(exSource.getReturnType() instanceof NoType) && !returnTag.isPresent()) {
			printError(exSource, " has a return, but no @return parameter");
		}
	}

	/**
	 * Sometimes, Op developers will choose to specify the functional type of the
	 * Op, instead of appending some tag to the I/O parameter. In this case, it's
	 * easiest to edit the appropriate {@link OpParameter} <b>after</b> we've made
	 * all of them.
	 *
	 * @param type the type hint specified in the {@code @implNote} Javadoc tag
	 * @param params the list of {@link OpParameter}s.
	 */
	private void editIOIndex(String type, List<OpParameter> params) {
		// NB the parameter index will be the discovered int, minus one.
		// e.g. "Inplace1" means to edit the first parameter
        Matcher m = COMPUTER_TYPE.matcher(type);
		if (m.find()) {
			String idx = m.group(1);
			int ioIndex = idx.isEmpty() ? params.size() - 1 : Integer.parseInt(idx) -
				1;
			params.get(ioIndex).ioType = OpParameter.IO_TYPE.CONTAINER;
			return;
		}
		m = INPLACE_TYPE.matcher(type);
		if (m.find()) {
			String idx = m.group(1);
			// Unlike for computers, Inplaces MUST have a suffix
            int ioIndex = Integer.parseInt(idx) - 1;
			params.get(ioIndex).ioType = OpParameter.IO_TYPE.MUTABLE;
		}
	}

	/**
	 * Helper function to print issues with {@code exSource} in a uniform manner.
	 *
	 * @param exSource some {@link ExecutableElement} referring to an Op written
	 *          as a method.
	 * @param msg a {@link String} describing the issue with the Op method.
	 */
	private void printError(ExecutableElement exSource, String msg) {
		Element clsElement = exSource.getEnclosingElement();
		while (clsElement.getKind() != ElementKind.CLASS) {
			clsElement = clsElement.getEnclosingElement();
		}
		env.getMessager().printMessage(Diagnostic.Kind.ERROR, clsElement + " - " +
			exSource + msg);
	}

	/**
	 * HACK to find type variable param tags For a parameter tag, returns
	 * {@code true} iff the following tag is a type variable tag. Type variable
	 * tags start with a greater than sign, and then has a string of letters, and
	 * then a less than sign.
	 *
	 * @param tag the string following an param tag
	 * @return true iff the tag is an param tag
	 */
	private boolean paramIsTypeVariable(String tag) {
		// TODO: Why doesn't Pattern.matches(".*<\\p{L}>.*", tag) work??
		if (tag.charAt(0) != '<') return false;
		for (int i = 1; i < tag.length(); i++) {
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
		List<? extends VariableElement> params = exSource.getParameters();
		sb.append("(");
		for (int i = 0; i < params.size(); i++) {
			TypeMirror d = env.getTypeUtils().erasure(params.get(i).asType());
			sb.append(d);
			if (i < params.size() - 1) {
				sb.append(",");
			}
		}
		sb.append(")");

		try {
			return "javaMethod:/" + URLEncoder.encode(sb.toString(),
					StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
