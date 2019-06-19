package org.scijava.ops.transform.functional;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Adapt;
import org.scijava.ops.util.Functions;
import org.scijava.plugin.Plugin;

@Plugin(type = OpTransformer.class)
public class ComputerToFunctionTransformer implements FunctionalTypeTransformer {
	
	@Override
	public Object transform(OpService opService, OpRef ref, Object src) throws Exception {
		Type[] argTypes = ref.getArgs();
		Function srcOp = Functions.unary(opService, "create", Nil.of(argTypes[0]), Nil.of(ref.getOutType()));
		return Adapt.Computers.asFunction((Computer) src, srcOp);
	}

	@Override
	public Class<?> srcClass() {
		return Computer.class;
	}

	@Override
	public Class<?> targetClass() {
		return Function.class;
	}

	@Override
	public Type[] getTransformedArgTypes(OpRef toRef) {
		return TypeModUtils.insert(toRef.getArgs(), toRef.getOutType(), 1);
	}

	@Override
	public Type getTransformedOutputType(OpRef toRef) {
		return toRef.getOutType();
	}
}
