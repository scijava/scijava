package org.scijava.ops.transform.functional;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.scijava.ops.OpService;
import org.scijava.ops.core.BiComputer;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.util.Adapt;
import org.scijava.ops.util.Functions;
import org.scijava.plugin.Plugin;
import org.scijava.ops.types.Nil;

@Plugin(type = OpTransformer.class)
public class BiComputerToBiFunctionTransformer implements FunctionalTypeTransformer {

	@Override
	public Object transform(OpService opService, OpRef ref, Object src) throws Exception {
		Type[] outTypes = ref.getOutTypes();
		Type[] argTypes = ref.getArgs();
		BiFunction srcOp = Functions.binary(opService, "create", Nil.of(argTypes[0]), Nil.of(argTypes[1]),
				Nil.of(outTypes[0]));
		return Adapt.Computers.asBiFunction((BiComputer) src, srcOp);
	}

	@Override
	public Class<?> srcClass() {
		return BiComputer.class;
	}

	@Override
	public Class<?> targetClass() {
		return BiFunction.class;
	}

	@Override
	public Type[] getTransformedArgTypes(OpRef toRef) {
		return TypeModUtils.insert(toRef.getArgs(), toRef.getOutTypes()[0], 2);
	}

	@Override
	public Type[] getTransformedOutputTypes(OpRef toRef) {
		return toRef.getOutTypes();
	}
}
