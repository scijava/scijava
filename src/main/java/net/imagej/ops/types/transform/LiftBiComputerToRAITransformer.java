package net.imagej.ops.types.transform;

import net.imagej.ops.types.transform.util.Maps;
import net.imglib2.RandomAccessibleInterval;

import org.scijava.ops.OpService;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpMapper;
import org.scijava.ops.transform.OpRefTransformUtils;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.plugin.Plugin;

/**
 * @author David Kolb
 */
@Plugin(type = OpTransformer.class)
public class LiftBiComputerToRAITransformer<I1, I2, O> implements
	OpMapper<BiComputer<I1, I2, O>, BiComputer<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>>>
{

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<BiComputer<I1, I2, O>> srcClass() {
		return (Class) BiComputer.class;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<BiComputer<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>>> targetClass() {
		return (Class) BiComputer.class;
	}

	@Override
	public BiComputer<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>> transformTypesafe(final OpService opService, final BiComputer<I1, I2, O> src,
		final OpRef targetRef)
	{
		return Maps.Computers.RAIs.liftBoth(src);
	}

	@Override
	public OpRef getRefTransformingTo(final OpRef targetRef) {
		return OpRefTransformUtils.unliftTransform(targetRef, BiComputer.class, RandomAccessibleInterval.class, new Integer[] {0, 1, 2},
			new Integer[] { 0, 1, 2 }, new Integer[] { 0 });
	}
}
