
package org.scijava.ops.engine.stats;

import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.scijava.ops.spi.Op;
import org.scijava.plugin.Plugin;

public class Size {

	@Plugin(type = Op.class, name = "stats.size")
	public static class StatsSizeFunction<T> implements Function<Iterable<T>, Long>{

		/**
		 * @param iterable the data to operate over
		 * @return the size of the dataset
		 */
			/**
		 * TODO
		 * 
		 * @param iterable
		 * @param size
		 */
	@Override
		public Long apply(Iterable<T> iterable) {
			return StreamSupport.stream(iterable.spliterator(), false).count();
		}
	}

	   @Plugin(type = Op.class, name = "stats.size")
	    public static class StatsSizeFunctionDouble<T> implements Function<Iterable<T>, Double>{

	       	        /**
	         * TODO
	         * 
	         * @param iterable
	         * @param size
	         */
 @Override
	        public Double apply(Iterable<T> iterable) {
	            return (double) StreamSupport.stream(iterable.spliterator(), false).count();
	        }
	    }
}
