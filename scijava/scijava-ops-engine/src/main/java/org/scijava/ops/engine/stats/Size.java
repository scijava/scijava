
package org.scijava.ops.engine.stats;

import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

public class Size {

	@OpClass(names = "stats.size")
	public static class StatsSizeFunction<T> implements
		Function<Iterable<T>, Long>, Op
	{

		/**
		 * @param iterable the data to operate over
		 * @return the size of the dataset
		 */
		@Override
		public Long apply(Iterable<T> iterable) {
			return StreamSupport.stream(iterable.spliterator(), false).count();
		}
	}

	@OpClass(names = "stats.size")
	public static class StatsSizeFunctionDouble<T> implements
		Function<Iterable<T>, Double>, Op
	{

		/**
		 * @param iterable the data to operate over
		 * @return the size of the dataset
		 */
		@Override
		public Double apply(Iterable<T> iterable) {
			return (double) StreamSupport.stream(iterable.spliterator(), false)
				.count();
		}
	}
}
