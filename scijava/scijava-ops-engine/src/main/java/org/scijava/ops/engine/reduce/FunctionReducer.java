package org.scijava.ops.engine.reduce;

import java.lang.reflect.Type;

import org.scijava.function.Functions;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.api.features.BaseOpHints;
import org.scijava.types.Types;

public class FunctionReducer implements InfoReducer{

	@Override
	public boolean canReduce(OpInfo info) {
		boolean isFunction = Functions.isFunction(OpUtils.findFunctionalInterface(Types.raw(info.opType())));
		boolean canReduce = info.declaredHints().containsNone(
				BaseOpHints.Reduction.FORBIDDEN);
		return isFunction && canReduce;
	}

	@Override
	public ReducedOpInfo reduce(OpInfo info, int numReductions) {
		Type opType = info.opType();
		Class<?> rawType = OpUtils.findFunctionalInterface(Types.raw(opType));
		int originalArity = Functions.arityOf(Functions.superType(rawType));
		int reducedArity = originalArity - numReductions;
		Class<?> reducedRawType = Functions.functionOfArity(reducedArity);
		Type[] inputTypes = OpUtils.inputTypes(info.struct());
		Type outputType = info.output().getType();
		Type[] newTypes = new Type[reducedArity + 1];
		if (reducedArity >= 0)
			System.arraycopy(inputTypes, 0, newTypes, 0, reducedArity);
		newTypes[newTypes.length - 1] = outputType;
		Type reducedOpType = Types.parameterize(reducedRawType, newTypes);
		return new ReducedOpInfo(info, reducedOpType, originalArity - reducedArity);
	}

}
