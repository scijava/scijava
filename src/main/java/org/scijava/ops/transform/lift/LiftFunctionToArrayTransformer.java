package org.scijava.ops.transform.lift;

import java.lang.reflect.Array;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpRefTransformUtils;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.util.Maps;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

@Plugin(type = OpTransformer.class)
public class LiftFunctionToArrayTransformer implements OpTransformer {

	@Override
	public Object transform(OpService opService, OpRef targetRef, Object src) {
		Class<?> outRaw = Types.raw(getRefTransformingTo(targetRef).getOutTypes()[0]);
		return Maps.Functions.Arrays.liftBoth((Function) src, outRaw);
	}

	@Override
	public OpRef getRefTransformingTo(OpRef toRef) {
		return OpRefTransformUtils.unliftTransform(toRef, Function.class, Array.class, new Integer[] {},
				new Integer[] { 0 }, new Integer[] { 0 });
	}

	@Override
	public Class<?> srcClass() {
		return Function.class;
	}
}
