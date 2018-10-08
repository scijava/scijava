package org.scijava.ops.transform.impl;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Computer;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.util.Adapt;
import org.scijava.ops.util.Functions;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

@Plugin(type = OpTransformer.class)
public class ComputerToFunctionTransformer implements OpTransformer {

	@Override
	public Object transform(OpService opService, OpRef ref, Object src) {
		if (src instanceof Computer) {
			// TODO what happens if we actually have several?
			Type[] outTypes = ref.getOutTypes();
			Type[] argTypes = ref.getArgs();
			try {
				Function srcOp = Functions.unary(opService, "create", Nil.of(argTypes[0]), Nil.of(outTypes[0]));
				return Adapt.Computers.asFunction((Computer) src, srcOp);
			} catch (Exception e) {
				//TODO
			}
		}
		return null;
	}

	@Override
	public OpRef getFromTransformTo(OpRef toRef) {
		Type[] refTypes = toRef.getTypes();
		boolean hit = TypeModUtils.replaceRawTypes(refTypes, Function.class, Computer.class);
		if (hit) {
			// The computer has a ItemIO.BOTH as second functional parameter of type functional output.
			// Hence, we need to add it after the first functional input.
			Type[] computerArgs = TypeModUtils.insert(toRef.getArgs(), toRef.getOutTypes()[0], 1);
			return OpRef.fromTypes(toRef.getName(), refTypes, toRef.getOutTypes(), computerArgs);
		}
		return null;
	}

}
