
package org.scijava.ops.engine.reduce;

import java.lang.reflect.Type;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.features.BaseOpHints;
import org.scijava.ops.engine.OpUtils;
import org.scijava.types.Types;

public abstract class AbstractInfoReducer implements InfoReducer {

	@Override
	public boolean canReduce(OpInfo info) {
		boolean isReducerType = isReducerType(OpUtils.findFunctionalInterface(Types
			.raw(info.opType())));
		boolean canReduce = info.declaredHints().containsNone(
			BaseOpHints.Reduction.FORBIDDEN);
		return isReducerType && canReduce;
	}

	@Override
	public ReducedOpInfo reduce(OpInfo info, int numReductions) {
		Type opType = info.opType();
		Class<?> rawType = OpUtils.findFunctionalInterface(Types.raw(opType));
		int originalArity = arityOf(rawType);
		int reducedArity = originalArity - numReductions;
		Class<?> reducedRawType = ofArity(reducedArity);
		Type[] inputTypes = info.inputTypes().toArray(Type[]::new);
		Type outputType = info.output().getType();
		Type[] newTypes = new Type[reducedArity + 1];
		if (reducedArity >= 0) System.arraycopy(inputTypes, 0, newTypes, 0,
			reducedArity);
		newTypes[newTypes.length - 1] = outputType;
		Type reducedOpType = Types.parameterize(reducedRawType, newTypes);
		return new ReducedOpInfo(info, reducedOpType, originalArity - reducedArity);
	}

	protected abstract boolean isReducerType(Class<?> functionalInterface);

	protected abstract int arityOf(Class<?> rawType);

	protected abstract Class<?> ofArity(int reducedArity);
}
