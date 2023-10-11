package org.scijava.ops.engine.util.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.scijava.common3.Classes;
import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.spi.OpDependency;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;
import org.scijava.types.inference.InterfaceInference;

public class OpMethodUtils {

	public static Type getOpMethodType(Class<?> opClass, Method opMethod) {
		// since type is a functional interface, it has (exactly) one abstract
		// declared method (the method that our OpMethod is emulating).
		Method abstractMethod;
		try {
			abstractMethod = InterfaceInference.singularAbstractMethod(opClass);
		} catch (IllegalArgumentException e) {
			throw new FunctionalTypeOpException(opMethod, e);

		}
		Type[] typeMethodParams = abstractMethod.getGenericParameterTypes();
		java.lang.reflect.Parameter[] opMethodParams = getOpParams(opMethod
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
			Type returnType = opMethod.getGenericReturnType();
			if (Types.raw(returnType).isPrimitive())
				returnType = Classes.box(Types.raw(returnType));
			GenericAssignability.inferTypeVariables(new Type[] { abstractMethod
				.getGenericReturnType() }, new Type[] {returnType}, typeVarAssigns);
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
			.map(param -> Types.raw(param).isPrimitive() ? Classes.box(Types.raw(param)) : param) //
			.toArray(Type[]::new);
	}

}
