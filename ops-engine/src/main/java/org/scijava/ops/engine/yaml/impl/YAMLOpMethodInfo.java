/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

import org.scijava.common3.Classes;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.exceptions.impl.InstanceOpMethodException;
import org.scijava.ops.engine.exceptions.impl.PrivateOpException;
import org.scijava.ops.engine.exceptions.impl.UnreadableOpException;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.struct.MethodParameterOpDependencyMember;
import org.scijava.ops.engine.struct.SynthesizedParameterMember;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.engine.util.internal.OpMethodUtils;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpMethod;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An {@link OpInfo}, backed by some {@code public static} {@link Method},
 * described via YAML that is stored within a {@link Map}.
 *
 * @author Gabriel Selzer
 */
public class YAMLOpMethodInfo extends AbstractYAMLOpInfo implements OpInfo {

	private final Type opType;
	private final Method method;
	private final Struct struct;

	public YAMLOpMethodInfo( //
		final Map<String, Object> yaml, //
		final String identifier) throws NoSuchMethodException
	{
		super(yaml, identifier);
		this.method = parseMethod(identifier);
		this.struct = createStruct(yaml);

		this.opType = OpMethodUtils.getOpMethodType(deriveOpType(), method);
		// Validate opMethod-specific features
		checkModifiers(method);
		// Validate general OpInfo features
		Infos.validate(this);
	}

	// -- OpInfo methods --

	@Override
	public Type opType() {
		return opType;
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public String implementationName() {
		// Get generic string without modifiers and return type
        var fullyQualifiedMethod = method.toGenericString();
        var packageName = method.getDeclaringClass().getPackageName();
        var classNameIndex = fullyQualifiedMethod.indexOf(packageName);
		return fullyQualifiedMethod.substring(classNameIndex);
	}

	@Override
	public StructInstance<?> createOpInstance(final List<?> dependencies) {
		return OpMethodUtils.createOpInstance(this, method, dependencies);
	}

	/**
	 * For an {@link OpMethod}, we define the implementation as the concatenation
	 * of:
	 * <ol>
	 * <li>The fully qualified name of the class containing the method</li>
	 * <li>The method name</li>
	 * <li>The method parameters</li>
	 * <li>The version of the class containing the method, with a preceding
	 * {@code @}</li>
	 * </ol>
	 * <p>
	 * For example, for a method {@code baz(Double in1, String in2)} in class
	 * {@code com.example.foo.Bar}, you might have
	 * <p>
	 * {@code com.example.foo.Bar.baz(Double in1,String in2)@1.0.0}
	 */
	@Override
	public String id() {
		return OpInfo.IMPL_DECLARATION + implementationName() + "@" + version();
	}

	// -- Object methods --

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return method;
	}

	// -- Helper methods -- //

	private static Method parseMethod(final String identifier)
		throws NoSuchMethodException
	{
		// first, remove generics
        var rawIdentifier = sanitizeGenerics(identifier);

		// parse class
        var clsIndex = rawIdentifier.lastIndexOf('.', rawIdentifier.indexOf('('));
        var clsString = rawIdentifier.substring(0, clsIndex);
        var src = Classes.load(clsString);
		// parse method
        var methodString = rawIdentifier.substring(clsIndex + 1, rawIdentifier
			.indexOf('('));
        var paramStrings = rawIdentifier.substring(rawIdentifier.indexOf('(') +
			1, rawIdentifier.indexOf(')')).split("\\s*,\\s*");
        var paramClasses = new Class<?>[paramStrings.length];
		for (var i = 0; i < paramStrings.length; i++) {
			paramClasses[i] = deriveType(identifier, paramStrings[i]);
		}
		return src.getMethod(methodString, paramClasses);
	}

	private Class<?> deriveOpType() {
        var params = (List<Map<String, Object>>) yaml.get(
			"parameters");
		for (var i = params.size() - 1; i >= 0; i--) {
			var pMap = params.get(i);
			switch (((String) pMap.get("parameter type"))) {
				case "OUTPUT":
					return Functions.functionOfArity(params.size() - 1);
				case "MUTABLE":
					return Inplaces.inplaceOfArity(params.size(), i);
				case "CONTAINER":
					return Computers.computerOfArity(params.size() - 1, i);
			}
		}
		throw new IllegalStateException(
			"Could not determine functional type of Op " + method);
	}

	private static Class<?> deriveType(String identifier, String typeString) {
		try {
			return Classes.load(typeString, false);
		}
		catch (Throwable t) {
			if (typeString.lastIndexOf('.') > -1) {
				var lastIndex = typeString.lastIndexOf('.');
				return deriveType(identifier, typeString.substring(0, lastIndex) + '$' +
					typeString.substring(lastIndex + 1));
			}
			else {
				throw new RuntimeException("Op " + identifier +
					" could not be loaded: Could not load class " + typeString, t);
			}
		}
	}

	private static String sanitizeGenerics(String method) {
        var nested = 0;
        var sb = new StringBuilder();
		for (var i = 0; i < method.length(); i++) {
            var c = method.charAt(i);
			if (c == '<') {
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

	private static void checkModifiers(Method method) {
		// Reject all non public methods
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new PrivateOpException(method);
		}
		if (!Modifier.isStatic(method.getModifiers())) {
			// TODO: We can't properly infer the generic types of static methods at
			// the moment. This might be a Java limitation.
			throw new InstanceOpMethodException(method);
		}

		// If the Op is not in the SciJava Ops Engine module, check visibility
        var methodModule = method.getDeclaringClass().getModule();
		if (methodModule != YAMLOpMethodInfo.class.getModule()) {
            var packageName = method.getDeclaringClass().getPackageName();
			if (!methodModule.isOpen(packageName, methodModule)) {
				throw new UnreadableOpException(packageName);
			}
		}
	}

	private Struct createStruct(Map<String, Object> yaml) {
		List<Member<?>> members = new ArrayList<>();
        var params = (List<Map<String, Object>>) yaml.get(
			"parameters");
        var methodParams = method.getParameters();
		// Skip any Op dependencies
		// HACK - some components in RuntimeSafeMatchingRoutine expect dependencies
		// to come last in the struct.
		var fmts = FunctionalParameters.findFunctionalMethodTypes(OpMethodUtils
			.getOpMethodType(deriveOpType(), method));
		for (var i = 0; i < params.size(); i++) {
			var pMap = params.get(i);
			var fmt = fmts.get(i);
            var name = (String) pMap.get("name");
            var description = (String) pMap.get("description");
            var nullable = (boolean) pMap.getOrDefault("nullable", false);
			members.add(new SynthesizedParameterMember<>(fmt, name, !nullable,
				description));
		}

		for (var methodParam : methodParams) {
			if (!methodParam.isAnnotationPresent(OpDependency.class)) break;
			members.add(new MethodParameterOpDependencyMember<>( //
				methodParam.getName(), //
				methodParam.getParameterizedType(), //
				methodParam.getAnnotation(OpDependency.class) //
			));
		}

		return () -> members;
	}

}
