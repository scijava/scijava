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

	private Lambdas() {}

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
		MethodHandles.Lookup caller = MethodHandles.lookup();

		// determine the method name used by the functionalInterface (e.g. for
		// Consumer this name is "accept").
		String[] invokedNames =
				Arrays.stream(functionalInterface.getDeclaredMethods()) //
						.filter(method -> Modifier.isAbstract(method.getModifiers())) //
						.map(Method::getName) //
						.toArray(String[]::new);
		if (invokedNames.length != 1) throw new IllegalArgumentException(
				"The passed class is not a functional interface");
		// see the LambdaMetafactory javadocs for explanations on these
		// MethodTypes.
		MethodType invokedType = MethodType.methodType(functionalInterface, //
				capturedClasses //
		);
		MethodType methodType = methodHandle.type();
		// Drop captured arguments
		methodType = methodType.dropParameterTypes(0, capturedArgs.length);
		// Box primitive parameter types
		for (int i = 0; i < methodType.parameterCount(); i++) {
			Class<?> paramType = methodType.parameterType(i);
			if (paramType.isPrimitive())
				methodType = methodType.changeParameterType(i, Classes.box(paramType));
		}
		Class<?> rType = methodType.returnType();
		if (rType.isPrimitive() && rType != void.class) rType = Classes.box(rType);
		MethodType samMethodType = methodType.generic() //
				.changeReturnType(rType == void.class ? rType : Object.class);
		MethodHandle callSite = LambdaMetafactory.metafactory(//
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
