package org.scijava.types.inference;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class InterfaceInference {

	public static Method singularAbstractMethod(Class<?> functionalInterface) {
		Method[] typeMethods = Arrays.stream(functionalInterface
			.getMethods()).filter(method -> Modifier.isAbstract(method
				.getModifiers())).toArray(Method[]::new);
		if (typeMethods.length != 1) {
			throw new IllegalArgumentException(functionalInterface +
				" should be a FunctionalInterface, however it has " +
				typeMethods.length + " abstract declared methods");
		}

		return typeMethods[0];
	}

}
