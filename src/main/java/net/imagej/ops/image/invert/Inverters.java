package net.imagej.ops.image.invert;

import java.math.BigInteger;

import net.imagej.types.UnboundedIntegerType;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.Unsigned128BitType;
import net.imglib2.type.numeric.integer.UnsignedLongType;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class Inverters<T extends RealType<T>, I extends IntegerType<I>> {

	@OpField(names = "image.invert")
	@Parameter(key = "input")
	@Parameter(key = "min")
	@Parameter(key = "max")
	@Parameter(key = "invertedOutput", type = ItemIO.BOTH)
	public final Computer3<IterableInterval<T>, T, T, IterableInterval<T>> delegatorInvert = (input, min, max,
			output) -> {

		// HACK: Some types are small enough that they can run the faster, double math
		// invert.
		// Others (fortunately all in this category are IntegerTypes)
		// must use the slower BigInteger inverter.
		// TODO: Think of a better solution.
		final T copy = input.firstElement().createVariable();
		boolean typeTooBig = false;
		// if the type is an integer type that can handle Long.MAX_VALUE
		// then we have to run the slow version
		if (copy instanceof IntegerType) {
			((IntegerType) copy).setInteger(Long.MAX_VALUE);
			if (((IntegerType) copy).getIntegerLong() == Long.MAX_VALUE)
				typeTooBig = true;
		}

		if (typeTooBig) {

			computeIIInteger(input, min, max, output);

		} else {

			computeII(input, min, max, output);

		}
	};

	@OpField(names = "image.invert")
	@Parameter(key = "input")
	@Parameter(key = "invertedOutput", type = ItemIO.BOTH)
	public final Computer<IterableInterval<T>, IterableInterval<T>> simpleInvert = (input, output) -> delegatorInvert
			.compute(input, minValue(input.firstElement()), maxValue(input.firstElement()), output);

	public void computeII(final IterableInterval<T> input, final T min, final T max, final IterableInterval<T> output) {
		final double minDouble = min.getRealDouble();
		final double maxDouble = max.getRealDouble();
		final double minMax = min.getRealDouble() + max.getRealDouble();

		final Cursor<T> inCursor = input.cursor();
		final Cursor<T> outCursor = output.cursor();
		while (inCursor.hasNext()) {
			T in = inCursor.next();
			T out = outCursor.next();
			if (minMax - in.getRealDouble() <= out.getMinValue()) {
				out.setReal(out.getMinValue());
			} else if (minMax - in.getRealDouble() >= out.getMaxValue()) {
				out.setReal(out.getMaxValue());
			} else
				out.setReal(minMax - in.getRealDouble());
		}
	}

	// HACK: this will only be run when our image is of a type too big for default
	// inverts.
	// TODO: Think of a better solution.
	@SuppressWarnings("unchecked")
	public void computeIIInteger(final IterableInterval<T> input, final T min, final T max,
			final @Mutable IterableInterval<T> output) {

		final BigInteger minValue = getBigInteger(min);
		final BigInteger maxValue = getBigInteger(max);
		final BigInteger minMax = minValue.add(maxValue);

		final Cursor<T> inCursor = input.cursor();
		final Cursor<T> outCursor = output.cursor();
		while (inCursor.hasNext()) {
			T in = inCursor.next();
			T out = outCursor.next();
			BigInteger inverted = minMax.subtract(getBigInteger(in));

			if (inverted.compareTo(getBigInteger(minValue(out))) <= 0)
				out.set(minValue(out));
			else if (inverted.compareTo(getBigInteger(maxValue(out))) >= 0)
				out.set(maxValue(out));
			else
				setBigInteger(out, inverted);
		}

	}

	private BigInteger getBigInteger(T in) {
		if (in instanceof IntegerType) {
			return ((IntegerType) in).getBigInteger();
		}
		return BigInteger.valueOf((long) in.getRealDouble());
	}

	private void setBigInteger(T out, BigInteger bi) {
		if (out instanceof IntegerType) {
			((IntegerType) out).setBigInteger(bi);
			return;
		}
		out.setReal(bi.doubleValue());
		return;
	}

	public static <T extends RealType<T>> T minValue(T type) {
		// TODO: Consider making minValue an op.
		final T min = type.createVariable();
		if (type instanceof UnboundedIntegerType)
			min.setReal(0);
		else
			min.setReal(min.getMinValue());
		return min;

	}

	public static <T extends RealType<T>> T maxValue(T type) {
		// TODO: Consider making maxValue an op.
		final T max = type.createVariable();
		if (max instanceof Unsigned128BitType) {
			final Unsigned128BitType t = (Unsigned128BitType) max;
			t.set(t.getMaxBigIntegerValue());
		} else if (max instanceof UnsignedLongType) {
			final UnsignedLongType t = (UnsignedLongType) max;
			t.set(t.getMaxBigIntegerValue());
		} else if (max instanceof UnboundedIntegerType) {
			max.setReal(0);
		} else {
			max.setReal(type.getMaxValue());
		}
		return max;
	}

}
