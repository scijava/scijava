package org.scijava.ops.transform.lift;

import java.util.List;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpRefTransformUtils;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.util.Maps;
import org.scijava.plugin.Plugin;

@Plugin(type = OpTransformer.class)
public class LiftFunctionToListTransformer implements OpTransformer {

	@Override
	public Object transform(OpService opService, OpRef fromRef, Object src) {
		return Maps.Lift.Functions.list((Function) src);
	}

	@Override
	public OpRef getRefTransformingTo(OpRef toRef) {
		return OpRefTransformUtils.unliftTransform(toRef, Function.class, List.class);
	}
	
	@Override
	public Class<?> srcClass() {
		return Function.class;
	}
}
