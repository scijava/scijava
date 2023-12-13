/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.yaml.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

import org.scijava.common3.Classes;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.engine.yaml.AbstractYAMLOpInfoCreator;
import org.scijava.ops.engine.yaml.YAMLOpInfoCreator;
import org.scijava.ops.spi.OpDependency;

/**
 * A {@link YAMLOpInfoCreator} specialized for Java {@link Method}s.
 *
 * @author Gabriel Selzer
 */
public class JavaMethodYAMLInfoCreator extends AbstractYAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(URI identifier) {
		return identifier.getScheme().startsWith("javaMethod");
	}

	@Override
	protected OpInfo create(String identifier, String[] names, double priority,
		String version, Map<String, Object> yaml) throws Exception
	{
		// first, remove generics
		String rawIdentifier = sanitizeGenerics(identifier);

		// parse class
		int clsIndex = rawIdentifier.lastIndexOf('.', rawIdentifier.indexOf('('));
		String clsString = rawIdentifier.substring(0, clsIndex);
		Class<?> src = Classes.load(clsString);
		// parse method
		String methodString = rawIdentifier.substring(clsIndex + 1, rawIdentifier.indexOf(
			'('));
		String[] paramStrings = rawIdentifier.substring(rawIdentifier.indexOf('(') + 1,
			rawIdentifier.indexOf(')')).split("\\s*,\\s*");
		Class<?>[] paramClasses = new Class<?>[paramStrings.length];
		for (int i = 0; i < paramStrings.length; i++) {
			paramClasses[i] = deriveType(identifier, paramStrings[i]);
		}
		Method method = src.getMethod(methodString, paramClasses);
		// parse op type
		Class<?> opType;
		Map<String, Object> tags = ((Map<String, Object>) yaml.get("tags"));
		String typeString = (String) tags.getOrDefault("type", "");
		opType = deriveOpType(identifier, typeString, method);

		return new OpMethodInfo(method, opType, new Hints(), priority, names);
	}

	private Class<?> deriveOpType(String identifier, String typeString, Method method) {
		int parameterCount = method.getParameterCount();
		for(Parameter p: method.getParameters()) {
			if (p.isAnnotationPresent(OpDependency.class)) {
				parameterCount--;
			}
		}
		// Handle pure inference
		if (typeString.isBlank()) {
			if (method.getReturnType() != void.class) {
				return Functions.functionOfArity(parameterCount);
			}
			else {
				throw new RuntimeException(
						"Op " + identifier + " could not be loaded: Computers and Inplaces must declare their Op type in their @implNote annotation For example, if your Inplace is designed to mutate the first argument, please write \"type='Inplace1'\"");
			}
		}
		// Handle op type inference
		if (Pattern.matches("^[Ii]nplace\\s*[0-9]*$", typeString)) {
			try {
				int ioIndex = Integer.parseInt(typeString.replaceAll("[^0-9]", "")) - 1;
				return Inplaces.inplaceOfArity(parameterCount, ioIndex);
			} catch(NumberFormatException e) {
				throw new RuntimeException(
						"Op " + identifier + " could not be loaded: Inplaces must declare the index of the mutable parameter. For example, if your Inplace is designed to mutate the first argument, please write \"Inplace1\"");
			}
		}
		else if (Pattern.matches("^[Cc]omputer\\s*[0-9]*$", typeString)) {
			return Computers.computerOfArity(parameterCount - 1);
		}
		else if (Pattern.matches("^[Ff]unction\\s*[0-9]*$", typeString)) {
			return Functions.functionOfArity(parameterCount);
		}
		// Finally, pass off to the class loader function.
		return deriveType(identifier, typeString);
	}

	private Class<?> deriveType(String identifier, String typeString){
		try {
			return Classes.load(typeString, false);
		} catch (Throwable t) {
			if (typeString.lastIndexOf('.') > -1) {
				var lastIndex = typeString.lastIndexOf('.');
				return deriveType(identifier, typeString.substring(0, lastIndex) + '$' + typeString.substring(lastIndex + 1));
			}
			else {
				throw new RuntimeException(
						"Op " + identifier + " could not be loaded: Could not load class " +
								typeString, t);
			}
		}
	}


	private static String sanitizeGenerics(String method) {
		int nested = 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < method.length(); i++) {
			char c = method.charAt(i);
			if(c == '<') {
				nested++;
			}
			if (nested == 0) {
				sb.append(c);
			}
			if (c == '>' && nested > 0) {
				nested--;
			}
		}
		return sb.toString();
	}
}
