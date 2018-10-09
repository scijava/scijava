package org.scijava.ops.transform.lift;

import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpRefTransformUtils;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.util.Maps;
import org.scijava.plugin.Plugin;

@Plugin(type = OpTransformer.class)
public class LiftFunctionToIterableTransformer implements OpTransformer {

	@Override
	public Object transform(OpService opService, OpRef fromRef, Object src) {
		if (src instanceof Function) {
			try {
				return Maps.Lift.Functions.iterable((Function) src);
			} catch (Exception e) {
				// TODO
			}
		}
		return null;
	}

	@Override
	public OpRef getRefTransformingTo(OpRef toRef) {
		return OpRefTransformUtils.unliftTransform(toRef, Function.class, Iterable.class);
	}

}
