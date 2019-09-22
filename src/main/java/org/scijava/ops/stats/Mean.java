package org.scijava.ops.stats;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Mean {

	@Plugin(type = Op.class, name = "stats.mean")
	@Parameter(key = "iterable")
	@Parameter(key = "mean", itemIO = ItemIO.OUTPUT)
	public static class MeanFunction <N, O> implements Function<Iterable<N>, O>{

		@OpDependency(name = "math.add")
		Function<Iterable<N>, O> sumFunc;

		@OpDependency(name = "stats.size")
		Function<Iterable<N>, O> sizeFunc;
		
		@OpDependency(name = "math.div")
		BiFunction<O, O, O> divFunc;

		@Override
		public O apply(Iterable<N> iterable) {
			return divFunc.apply(sumFunc.apply(iterable), sizeFunc.apply(iterable));
		}
	}

}
