package org.scijava.ops.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers.Arity1;
import org.scijava.ops.function.Computers.Arity2;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Producer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

public class ComputersToFunctions {

	@Plugin(type = Op.class, name = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public static class ComputerToFunctionViaFunction<I, O>
			implements Function<Computers.Arity1<I, O>, Function<I, O>> {

		@OpDependency(name = "create", adaptable = false)
		Function<I, O> creator;

		@Override
		public Function<I, O> apply(Arity1<I, O> computer) {
			return (in) -> {
				O out = creator.apply(in);
				computer.compute(in, out);
				return out;
			};
		}

	}

	// TODO: move to another file
	@Plugin(type = Op.class, name = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public static class ComputerToFunctionViaSource<I, O> implements Function<Computers.Arity1<I, O>, Function<I, O>> {

		@OpDependency(name = "create", adaptable = false)
		Producer<O> creator;

		@Override
		public Function<I, O> apply(Arity1<I, O> computer) {
			return (in) -> {
				O out = creator.create();
				computer.compute(in, out);
				return out;
			};
		}

	}

	@Plugin(type = Op.class, name = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public static class BiComputerToBiFunctionViaFunction<I1, I2, O>
			implements Function<Computers.Arity2<I1, I2, O>, BiFunction<I1, I2, O>> {

		@OpDependency(name = "create", adaptable = false)
		BiFunction<I1, I2, O> creator;

		@Override
		public BiFunction<I1, I2, O> apply(Arity2<I1, I2, O> computer) {
			return (in1, in2) -> {
				O out = creator.apply(in1, in2);
				computer.compute(in1, in2, out);
				return out;
			};
		}

	}

	// TODO: move to another file
	@Plugin(type = Op.class, name = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public static class BiComputerToFunctionViaSource<I1, I2, O>
			implements Function<Computers.Arity2<I1, I2, O>, BiFunction<I1, I2, O>> {

		@OpDependency(name = "create", adaptable = false)
		Producer<O> creator;

		@Override
		public BiFunction<I1, I2, O> apply(Arity2<I1, I2, O> computer) {
			return (in1, in2) -> {
				O out = creator.create();
				computer.compute(in1, in2, out);
				return out;
			};
		}

	}

	@Plugin(type = Op.class, name = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public static class Computer3ToFunction3ViaFunction<I1, I2, I3, O>
			implements Function<Computers.Arity3<I1, I2, I3, O>, Functions.Arity3<I1, I2, I3, O>> {

		@OpDependency(name = "create", adaptable = false)
		Functions.Arity3<I1, I2, I3, O> creator;

		@Override
		public Functions.Arity3<I1, I2, I3, O> apply(Computers.Arity3<I1, I2, I3, O> computer) {
			return (in1, in2, in3) -> {
				O out = creator.apply(in1, in2, in3);
				computer.compute(in1, in2, in3, out);
				return out;
			};
		}

	}

	// TODO: move to another file
	@Plugin(type = Op.class, name = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public static class Computer3ToFunction3ViaSource<I1, I2, I3, O>
			implements Function<Computers.Arity3<I1, I2, I3, O>, Functions.Arity3<I1, I2, I3, O>> {

		@OpDependency(name = "create", adaptable = false)
		Producer<O> creator;

		@Override
		public Functions.Arity3<I1, I2, I3, O> apply(Computers.Arity3<I1, I2, I3, O> computer) {
			return (in1, in2, in3) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, out);
				return out;
			};
		}

	}
}
