package org.scijava.ops.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Inplaces;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class InplacesToFunctions<IO, I1, I2, I3> {

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Inplaces.Arity1<IO>, Function<IO, IO>> inplace1ToFunction1 = (inplace) -> {
		return (io) -> {
			inplace.mutate(io);
			return io;
		};
	};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Inplaces.Arity2_1<IO, I2>, BiFunction<IO, I2, IO>> inplace2_1ToFunction2 = (inplace) -> {
		return (io, in2) -> {
			inplace.mutate(io, in2);
			return io;
		};
	};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Inplaces.Arity2_2<I1, IO>, BiFunction<I1, IO, IO>> inplace2_2ToFunction2 = (inplace) -> {
		return (in1, io) -> {
			inplace.mutate(in1, io);
			return io;
		};
	};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Inplaces.Arity3_1<IO, I2, I3>, Functions.Arity3<IO, I2, I3, IO>> inplace3_1ToFunction2 = (
			inplace) -> {
		return (io, in2, in3) -> {
			inplace.mutate(io, in2, in3);
			return io;
		};
	};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Inplaces.Arity3_2<I1, IO, I3>, Functions.Arity3<I1, IO, I3, IO>> inplace3_2ToFunction2 = (
			inplace) -> {
		return (in1, io, in3) -> {
			inplace.mutate(in1, io, in3);
			return io;
		};
	};

	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Inplaces.Arity3_3<I1, I2, IO>, Functions.Arity3<I1, I2, IO, IO>> inplace3_3ToFunction2 = (
			inplace) -> {
		return (in1, in2, io) -> {
			inplace.mutate(in1, in2, io);
			return io;
		};
	};

}
