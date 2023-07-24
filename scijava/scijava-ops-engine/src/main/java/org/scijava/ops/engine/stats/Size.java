
package org.scijava.ops.engine.stats;

import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

public class Size implements OpCollection {

	/**
	 * @param iterable the data to operate over
	 * @return the size of the dataset, as as {@link Long}
	 */
	@OpMethod(names = "stats.size", type = Function.class)
	public static <T> Long sizeAsLong(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false).count();
	}

	/**
	 * @param iterable the data to operate over
	 * @return the size of the dataset, as as {@link Double}
	 */
	@OpMethod(names = "stats.size", type = Function.class)
	public static <T> Double sizeAsDouble(Iterable<T> iterable) {
		return (double) StreamSupport.stream(iterable.spliterator(), false).count();
	}
}
