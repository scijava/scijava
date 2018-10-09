package org.scijava.ops.transform.functional;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Computer;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.util.Adapt;
import org.scijava.ops.util.Computers;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

@Plugin(type = OpTransformer.class)
public class FunctionToComputerTransformer implements FunctionalTypeTransformer {

	@Override
	public Object transform(OpService opService, OpRef ref, Object src) {
		Type[] outTypes = ref.getOutTypes();
		Computer copy = Computers.unary(opService, "copy", Nil.of(outTypes[0]), Nil.of(outTypes[0]));
		return Adapt.Functions.asComputer((Function) src, copy);
	}

	@Override
	public Class<?> srcClass() {
		return Function.class;
	}

	@Override
	public Class<?> targetClass() {
		return Computer.class;
	}

	@Override
	public Type[] getTransformedArgTypes(OpRef toRef) {
		return TypeModUtils.remove(toRef.getArgs(), 1);
	}

	@Override
	public Type[] getTransformedOutputTypes(OpRef toRef) {
		return toRef.getOutTypes();
	}
}
