package net.imagej.ops.types.transform;

import net.imagej.ops.types.transform.util.Maps;
import net.imglib2.RandomAccessibleInterval;

import org.scijava.ops.OpService;
import org.scijava.ops.function.Computers;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpMapper;
import org.scijava.ops.transform.OpRefTransformUtils;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.plugin.Plugin;

/**
 * @author David Kolb
 */
@Plugin(type = OpTransformer.class)
public class LiftComputer2ToRAITransformer<I1, I2, O> implements
	OpMapper<Computers.Arity2<I1, I2, O>, Computers.Arity2<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>>>
{

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Computers.Arity2<I1, I2, O>> srcClass() {
		return (Class) Computers.Arity2.class;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Computers.Arity2<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>>> targetClass() {
		return (Class) Computers.Arity2.class;
	}

	@Override
	public Computers.Arity2<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>> transformTypesafe(final OpService opService, final Computers.Arity2<I1, I2, O> src,
		final OpRef targetRef)
	{
		return Maps.ComputerMaps.RAIs.liftBoth(src);
	}

	@Override
	public OpRef getRefTransformingTo(final OpRef targetRef) {
		return OpRefTransformUtils.unliftTransform(targetRef, Computers.Arity2.class, RandomAccessibleInterval.class, new Integer[] {0, 1, 2},
			new Integer[] { 0, 1, 2 }, new Integer[] { 0 });
	}
}
