package org.scijava.ops.transform.lift;

import org.scijava.ops.OpService;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpRefTransformUtils;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.util.Maps;
import org.scijava.plugin.Plugin;

@Plugin(type = OpTransformer.class)
public class LiftComputerToIterableTransformer implements OpTransformer {

	@Override
	public Object transform(OpService opService, OpRef fromRef, Object src) {
		return Maps.Computers.Iterables.liftBoth((Computer) src);
	}

	@Override
	public OpRef getRefTransformingTo(OpRef toRef) {
		return OpRefTransformUtils.unliftTransform(toRef, Computer.class, 
				Iterable.class, new Integer[]{}, new Integer[]{0 , 1}, new Integer[]{ 0 });
	}

	@Override
	public Class<?> srcClass() {
		return Computer.class;
	}

}
