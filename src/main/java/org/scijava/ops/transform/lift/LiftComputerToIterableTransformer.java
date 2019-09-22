package org.scijava.ops.transform.lift;

import org.scijava.ops.OpService;
import org.scijava.ops.core.computer.Computer;
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
	OpMapper<Computer<I, O>, Computer<Iterable<I>, Iterable<O>>>
{

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Computer<I, O>> srcClass() {
		return (Class) Computer.class;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Computer<Iterable<I>, Iterable<O>>> targetClass() {
		return (Class) Computer.class;
	}

	@Override
	public Computer<Iterable<I>, Iterable<O>> transformTypesafe(final OpService opService, final Computer<I, O> src,
		final OpRef targetRef)
	{
		return Maps.Computers.Iterables.liftBoth(src);
	}

	@Override
	public OpRef getRefTransformingTo(final OpRef targetRef) {
		return OpRefTransformUtils.unliftTransform(targetRef, Computer.class, Iterable.class, new Integer[] {0, 1},
			new Integer[] {0, 1}, new Integer[] { 0 });
	}
}
