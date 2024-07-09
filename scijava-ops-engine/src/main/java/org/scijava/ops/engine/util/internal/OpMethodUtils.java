/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.util.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.common3.Classes;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.engine.util.Lambdas;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.Member;
import org.scijava.struct.StructInstance;
import org.scijava.common3.Types;
import org.scijava.types.infer.FunctionalInterfaces;
import org.scijava.types.infer.GenericAssignability;

/**
 * Common code used by Ops backed by {@link Method}s.
 *
 * @author Gabriel Selzer
 */
public final class OpMethodUtils {

	private OpMethodUtils() {
		// Prevent instantiation of static utility class
	}

	public static Type getOpMethodType(Class<?> opClass, Method opMethod) {
		// since type is a functional interface, it has (exactly) one abstract
		// declared method (the method that our OpMethod is emulating).
		Method abstractMethod;
		try {
			abstractMethod = FunctionalInterfaces.functionalMethodOf(opClass);
		}
		catch (IllegalArgumentException e) {
			throw new FunctionalTypeOpException(opMethod, e);

		}
        var typeMethodParams = abstractMethod.getGenericParameterTypes();
        var opMethodParams = getOpParams(opMethod
			.getParameters());

		if (typeMethodParams.length != opMethodParams.length) {
			throw new FunctionalTypeOpException(opMethod, opClass);
		}
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();

		// map params of OpMethod to type variables of abstract method of functional
		// interface (along with return type if applicable)
		// TODO: not sure how this handles when there are type variables.
		GenericAssignability.inferTypeVariables(typeMethodParams, getOpParamTypes(
			opMethodParams), typeVarAssigns);
		if (abstractMethod.getReturnType() != void.class) {
            var returnType = opMethod.getGenericReturnType();
			if (Types.raw(returnType).isPrimitive()) returnType = Classes.box(Types
				.raw(returnType));
			GenericAssignability.inferTypeVariables(new Type[] { abstractMethod
				.getGenericReturnType() }, new Type[] { returnType }, typeVarAssigns);
		}

		// parameterize opClass
		return Types.parameterize(opClass, typeVarAssigns);
	}

	public static java.lang.reflect.Parameter[] getOpParams(
		java.lang.reflect.Parameter[] methodParams)
	{
		return Arrays //
			.stream(methodParams) //
			.filter(param -> param.getAnnotation(OpDependency.class) == null) //
			.toArray(java.lang.reflect.Parameter[]::new);
	}

	public static Type[] getOpParamTypes(
		java.lang.reflect.Parameter[] methodParams)
	{
		return Arrays //
			.stream(methodParams) //
			.filter(param -> param.getAnnotation(OpDependency.class) == null) //
			.map(Parameter::getParameterizedType) //
			.map(param -> Types.raw(param).isPrimitive() ? Classes.box(Types.raw(
				param)) : param) //
			.toArray(Type[]::new);
	}

	/**
	 * Converts an {@link OpInfo} backed by a {@link Method} reference into an Op,
	 * given a list of its dependencies.
	 *
	 * @param info the {@link OpInfo}
	 * @param method the {@link Method} containing the Op code
	 * @param dependencies all Op dependencies required to execute the Op
	 * @return a {@link StructInstance}
	 */
	public static StructInstance<?> createOpInstance(final OpInfo info,
		final Method method, final List<?> dependencies)
	{
		// NB LambdaMetaFactory only works if this Module (org.scijava.ops.engine)
		// can read the Module containing the Op. So we also have to check that.
        var methodModule = method.getDeclaringClass().getModule();
        var opsEngine = OpMethodUtils.class.getModule();

		opsEngine.addReads(methodModule);
		try {
			method.setAccessible(true);
            var handle = MethodHandles.lookup().unreflect(method);
            var op = Lambdas.lambdaize( //
				Types.raw(info.opType()), //
				handle, //
				Infos.dependencies(info).stream().map(Member::rawType).toArray(
					Class[]::new), dependencies.toArray() //
			);
			return info.struct().createInstance(op);
		}
		catch (Throwable exc) {
			throw new IllegalStateException("Failed to invoke Op method: " + method,
				exc);
		}
	}

}
