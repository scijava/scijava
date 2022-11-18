package org.scijava.ops.engine.reduce;

import org.scijava.function.Functions;

public class FunctionReducer extends AbstractInfoReducer {

	@Override
	protected boolean isReducerType(Class<?> functionalInterface) {
		return Functions.isFunction(functionalInterface);
	}

	@Override
	protected int arityOf(Class<?> rawType) {
		return Functions.arityOf(Functions.superType(rawType));
	}

	@Override
	protected Class<?> ofArity(int reducedArity) {
		return Functions.functionOfArity(reducedArity);
	}

}
