package org.scijava.ops.adapt;

import java.util.function.Function;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers.Arity1;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

public class FunctionsToComputers {

	@Plugin(type = Op.class, name = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public static class FunctionToComputer<I, O> implements Function<Function<I, O>, Computers.Arity1<I, O>> {

		@OpDependency(name = "copy", adaptable = false)
		Arity1<O, O> copyOp;

		@Override
		public Arity1<I, O> apply(Function<I, O> function) {
			return (in, out) -> {
				O temp = function.apply(in);
				copyOp.compute(temp, out);
			};
		}

	}
}
