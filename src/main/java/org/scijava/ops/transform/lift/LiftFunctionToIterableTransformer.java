package org.scijava.ops.transform.lift;

import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpMapper;
import org.scijava.ops.transform.OpRefTransformUtils;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.util.Maps;
import org.scijava.plugin.Plugin;

/**
 * @author David Kolb
 */
@Plugin(type = OpTransformer.class)
public class LiftFunctionToIterableTransformer<I, O> implements
	OpMapper<Function<I, O>, Function<Iterable<I>, Iterable<O>>>
{

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Function<I, O>> srcClass() {
		return (Class) Function.class;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Function<Iterable<I>, Iterable<O>>> targetClass() {
		return (Class) Function.class;
	}

	@Override
	public Function<Iterable<I>, Iterable<O>> transformTypesafe(final OpService opService, final Function<I, O> src,
		final OpRef targetRef)
	{
		return Maps.FunctionMaps.Iterables.liftBoth(src);
	}

	@Override
	public OpRef getRefTransformingTo(final OpRef targetRef) {
		return OpRefTransformUtils.unliftTransform(targetRef, Function.class, Iterable.class, new Integer[] {},
			new Integer[] { 0 }, new Integer[] { 0 });
	}
}
