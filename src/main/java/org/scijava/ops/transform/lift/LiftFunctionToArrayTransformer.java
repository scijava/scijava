package org.scijava.ops.transform.lift;

import java.lang.reflect.Array;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpMapper;
import org.scijava.ops.transform.OpRefTransformUtils;
import org.scijava.ops.transform.OpTransformationException;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.util.Maps;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * @author David Kolb
 */
@Plugin(type = OpTransformer.class)
public class LiftFunctionToArrayTransformer<I, O> implements OpMapper<Function<I, O>, Function<I[], O[]>> {

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Function<I, O>> srcClass() {
		return (Class) Function.class;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Function<I[], O[]>> targetClass() {
		return (Class) Function.class;
	}

	@Override
	public Function<I[], O[]> transformTypesafe(final OpService opService, final Function<I, O> src,
		final OpRef targetRef) throws OpTransformationException
	{
		final Class<O> outRaw = (Class<O>) Types.raw(getRefTransformingTo(targetRef).getOutType());
		return Maps.FunctionMaps.Arrays.liftBoth(src, outRaw);
	}

	@Override
	public OpRef getRefTransformingTo(final OpRef targetRef) {
		return OpRefTransformUtils.unliftTransform(targetRef, Function.class, Array.class, new Integer[] {}, new Integer[] {
			0 }, new Integer[] { 0 });
	}
}
