package org.scijava.ops.stats;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

public class Mean {

	@Plugin(type = Op.class, name = "stats.mean")
	public static class MeanFunction <N, O> implements Function<Iterable<N>, O>{

		@OpDependency(name = "math.add")
		Function<Iterable<N>, O> sumFunc;

		@OpDependency(name = "stats.size")
		Function<Iterable<N>, O> sizeFunc;
		
		@OpDependency(name = "math.div")
		BiFunction<O, O, O> divFunc;

		/**
		 * @param iterable the set of data to operate on
		 * @return the mean of the data
		 */
			/**
		 * TODO
		 * 
		 * @param iterable
		 */
	@Override
		public O apply(Iterable<N> iterable) {
			return divFunc.apply(sumFunc.apply(iterable), sizeFunc.apply(iterable));
		}
	}

}
