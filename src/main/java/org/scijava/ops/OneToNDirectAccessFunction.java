
package org.scijava.ops;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.view.Views;

import org.scijava.plugin.Parameter;

/**
 * This demonstrates an idea for a function mapping 1 object to N objects. It
 * uses {@link RandomAccessibleInterval} to achieve {@code N>Integer.MAX_VALUE}.
 * The elements of the directly-accessible output are generated on demand by a
 * lambda BiConsumer operating on the current position plus input.
 *
 * @author Curtis Rueden
 */
public class OneToNDirectAccessFunction {

	/** Our input object. */
	public static class BigComplicatedThing {

		public int scale;

		public BigComplicatedThing(final int scale) {
			this.scale = scale;
		}
	}

	/** Our output element. */
	public static class LotsOfStats {

		@Parameter
		public double sqrt;
		@Parameter
		public double sqr;

		@Override
		public String toString() {
			return "sqrt=" + sqrt + ", sqr=" + sqr;
		}
	}

	public static void main(final String... args) {
		final BigComplicatedThing thing = new BigComplicatedThing(1);
		final RandomAccessibleInterval<LotsOfStats> result =
			new OneToNDirectAccessFunction().apply(thing);
		final Cursor<LotsOfStats> cursor = Views.iterable(result)
			.localizingCursor();
		final long[] pos = new long[cursor.numDimensions()];
		while (cursor.hasNext()) {
			final LotsOfStats stats = cursor.next();
			cursor.localize(pos);
			System.out.println(Arrays.toString(pos) + " = " + stats);
		}
	}

	/** The function itself. */
	public RandomAccessibleInterval<LotsOfStats> apply(
		final BigComplicatedThing input)
	{

		final FunctionRandomAccessible<LotsOfStats> function =
			new FunctionRandomAccessible<>(1, (pos, stats) -> {
				final long index = pos.getLongPosition(0);
				stats.sqrt = input.scale * Math.sqrt(index);
				stats.sqr = input.scale * index * index;
			}, LotsOfStats::new);

		return Views.interval(function, new FinalInterval(5));
	}

}
