package org.scijava.ops.transform.lift;

import org.scijava.ops.OpService;
import org.scijava.ops.function.Computers;
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
public class LiftComputerToIterableTransformer<I, O> implements
	OpMapper<Computers.Arity1<I, O>, Computers.Arity1<Iterable<I>, Iterable<O>>>
{

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Computers.Arity1<I, O>> srcClass() {
		return (Class) Computers.Arity1.class;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Computers.Arity1<Iterable<I>, Iterable<O>>> targetClass() {
		return (Class) Computers.Arity1.class;
	}

	@Override
	public Computers.Arity1<Iterable<I>, Iterable<O>> transformTypesafe(final OpService opService, final Computers.Arity1<I, O> src,
		final OpRef targetRef)
	{
		return Maps.ComputerMaps.Iterables.liftBoth(src);
	}

	@Override
	public OpRef getRefTransformingTo(final OpRef targetRef) {
		return OpRefTransformUtils.unliftTransform(targetRef, Computers.Arity1.class, Iterable.class, new Integer[] {0, 1},
			new Integer[] {0, 1}, new Integer[] { 0 });
	}
}
