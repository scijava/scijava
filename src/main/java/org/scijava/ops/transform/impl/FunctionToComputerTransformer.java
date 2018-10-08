package org.scijava.ops.transform.impl;

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
public class FunctionToComputerTransformer implements OpTransformer {

	@Override
	public Object transform(OpService opService, OpRef ref, Object src) {
		if (src instanceof Function) {
			// TODO what happens if we actually have several?
			Type[] outTypes = ref.getOutTypes();
			try {
				Computer copy = Computers.unary(opService, "copy", Nil.of(outTypes[0]), Nil.of(outTypes[0]));
				return Adapt.Functions.asComputer((Function) src, copy);
			} catch (Exception e) {
				//TODO
			}
		}
		return null;
	}

	@Override
	public OpRef getFromTransformTo(OpRef toRef) {
		Type[] refTypes = toRef.getTypes();
		boolean hit = TypeModUtils.replaceRawTypes(refTypes, Computer.class, Function.class);
		if (hit) {
			// The computer has a ItemIO.BOTH as second functional parameter of type output.
			// This is not the case for a Function, hence we remove it.
			Type[] functionArgs = TypeModUtils.remove(toRef.getArgs(), 1);
			return OpRef.fromTypes(toRef.getName(), refTypes, toRef.getOutTypes(), functionArgs);
		}
		return null;
	}

}
