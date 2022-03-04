//package org.scijava.ops.engine.reduce;
//
//import java.lang.reflect.Type;
//
//import org.scijava.ops.OpInfo;
//import org.scijava.ops.OpUtils;
//import org.scijava.ops.function.Functions;
//import org.scijava.param.ParameterStructs;
//import org.scijava.plugin.Plugin;
//import org.scijava.types.Types;
//
//@Plugin(type = InfoReducer.class)
//public class BiFunctionWithOptionalReducer implements InfoReducer {
//
//	@Override
//	public boolean canReduce(OpInfo info) {
//		return ParameterStructs.findFunctionalInterface(Types.raw(info.opType())) == BiFunctionWithOptional.class;
//	}
//
//	@Override
//	public ReducedOpInfo reduce(OpInfo info, int numReductions) {
//		if (numReductions != 1) throw new UnsupportedOperationException();
//		int reducedArity = 3 - numReductions;
//		Class<?> reducedRawType = Functions.ALL_FUNCTIONS.get(reducedArity);
//		Type[] inputTypes = OpUtils.inputTypes(info.struct());
//		Type outputType = OpUtils.outputType(info.struct());
//		Type[] newTypes = new Type[reducedArity + 1];
//		for(int i = 0; i < reducedArity; i++) {
//			newTypes[i] = inputTypes[i];
//		}
//		newTypes[newTypes.length - 1] = outputType;
//		Type reducedOpType = Types.parameterize(reducedRawType, newTypes);
//		return new ReducedOpInfo(info, reducedOpType, numReductions);
//	}
//
//}
