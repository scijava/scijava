/*-
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

package org.scijava.ops.engine.util;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.scijava.common3.Classes;

/**
 * Utility class for working with Lambda expressions.
 *
 * @author Gabriel Selzer
 */
public final class Lambdas {

	private Lambdas() {
		// Prevent instantiation of static utility class
	}

	public static <T> T lambdaize(Class<T> functionalInterface,
		MethodHandle methodHandle) throws Throwable
	{
		return lambdaize(functionalInterface, methodHandle, new Class[0],
			new Object[0]);
	}

	public static <T> T lambdaize(Class<T> functionalInterface,
		MethodHandle methodHandle, Class<?>[] capturedClasses,
		Object[] capturedArgs) throws Throwable
	{
        var caller = MethodHandles.lookup();

		// determine the method name used by the functionalInterface (e.g. for
		// Consumer this name is "accept").
        var invokedNames = Arrays.stream(functionalInterface
			.getDeclaredMethods()) //
			.filter(method -> Modifier.isAbstract(method.getModifiers())) //
			.map(Method::getName) //
			.toArray(String[]::new);
		if (invokedNames.length != 1) throw new IllegalArgumentException(
			"The passed class is not a functional interface");
		// see the LambdaMetafactory javadocs for explanations on these
		// MethodTypes.
        var invokedType = MethodType.methodType(functionalInterface, //
			capturedClasses //
		);
        var methodType = methodHandle.type();
		// Drop captured arguments
		methodType = methodType.dropParameterTypes(0, capturedArgs.length);
		// Box primitive parameter types
		for (var i = 0; i < methodType.parameterCount(); i++) {
            var paramType = methodType.parameterType(i);
			if (paramType.isPrimitive()) methodType = methodType.changeParameterType(
				i, Classes.box(paramType));
		}
        var rType = methodType.returnType();
		if (rType.isPrimitive() && rType != void.class) rType = Classes.box(rType);
        var samMethodType = methodType.generic() //
			.changeReturnType(rType == void.class ? rType : Object.class);
        var callSite = LambdaMetafactory.metafactory(//
			caller, //
			invokedNames[0], //
			invokedType, //
			samMethodType, //
			methodHandle, //
			methodType //
		).getTarget();
		return (T) callSite.invokeWithArguments(capturedArgs);
	}
}
