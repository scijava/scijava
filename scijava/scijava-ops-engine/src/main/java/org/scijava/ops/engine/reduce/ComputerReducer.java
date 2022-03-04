package org.scijava.ops.engine.reduce;

import org.scijava.function.Computers;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpUtils;
import org.scijava.types.Types;

import java.lang.reflect.Type;

public class ComputerReducer implements InfoReducer{

	@Override
	public boolean canReduce(OpInfo info) {
		return Computers.isComputer(OpUtils.findFunctionalInterface(Types.raw(info.opType())));
	}

	@Override
	public ReducedOpInfo reduce(OpInfo info, int numReductions) {
		Type opType = info.opType();
		Class<?> rawType = OpUtils.findFunctionalInterface(Types.raw(opType));
		int originalArity = Computers.arityOf(rawType);
		int reducedArity = originalArity - numReductions;
		Class<?> reducedRawType = Computers.computerOfArity(reducedArity);
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
