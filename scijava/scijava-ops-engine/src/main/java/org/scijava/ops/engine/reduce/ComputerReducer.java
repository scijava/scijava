package org.scijava.ops.engine.reduce;

import org.scijava.function.Computers;

public class ComputerReducer extends AbstractInfoReducer {

	@Override
	protected boolean isReducerType(Class<?> functionalInterface) {
		return Computers.isComputer(functionalInterface);
	}

	@Override
	protected int arityOf(Class<?> rawType) {
		return Computers.arityOf(rawType);
	}

	@Override
	protected Class<?> ofArity(int reducedArity) {
		return Computers.computerOfArity(reducedArity);
	}
}
