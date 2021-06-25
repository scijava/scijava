
package net.imagej.ops2.types.adapt;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Lifts {@link Computers} operating on some types {@code I1, I2, ..., In},
 * {@code O extends Type<O>} to a Computer operating on
 * {@link RandomAccessibleInterval}s of those types. The
 * {@Computer}{@code <I, O>} is then applied iteratively over each pixel of the
 * input image(s). NOTE: It is assumed that the input {@code RAI}s are the same
 * size. If they are not, the lifted {@code Computer} will only iteratively
 * process the images until one image runs out of pixels to iterate over. NB:
 * These adapt Ops are of high priority since they are faster than the Iterable
 * lifters of scijava-ops TODO: Autogenerate Include higher arities
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class LiftComputersToRAI<I1, I2, I3, I4, I5, I6, O> {

	@OpField(names = "adapt", priority = Priority.HIGH)
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<Computers.Arity1<I1, O>, Computers.Arity1<RandomAccessibleInterval<I1>, RandomAccessibleInterval<O>>> lift1 =
		(computer) -> {
			return (raiInput, raiOutput) -> {
				LoopBuilder.setImages(raiInput, raiOutput).multiThreaded().forEachPixel(
					(in, out) -> computer.compute(in, out));
			};
		};

	@OpField(names = "adapt", priority = Priority.HIGH)
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<Computers.Arity2<I1, I2, O>, Computers.Arity2<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>>> lift2 =
		(computer) -> {
			return (raiInput1, raiInput2, raiOutput) -> {
				LoopBuilder.setImages(raiInput1, raiInput2, raiOutput).multiThreaded()
					.forEachPixel((in1, in2, out) -> computer.compute(in1, in2, out));
			};
		};

	@OpField(names = "adapt", priority = Priority.HIGH)
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<Computers.Arity3<I1, I2, I3, O>, Computers.Arity3<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<I3>, RandomAccessibleInterval<O>>> lift3 =
		(computer) -> {
			return (raiInput1, raiInput2, raiInput3, raiOutput) -> {
				LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiOutput)
					.multiThreaded().forEachPixel((in1, in2, in3, out) -> computer
						.compute(in1, in2, in3, out));
			};
		};

	@OpField(names = "adapt", priority = Priority.HIGH)
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<Computers.Arity4<I1, I2, I3, I4, O>, Computers.Arity4<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<I3>, RandomAccessibleInterval<I4>, RandomAccessibleInterval<O>>> lift4 =
		(computer) -> {
			return (raiInput1, raiInput2, raiInput3, raiInput4, raiOutput) -> {
				LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4,
					raiOutput).multiThreaded().forEachPixel((in1, in2, in3, in4,
						out) -> computer.compute(in1, in2, in3, in4, out));
			};
		};

	@OpField(names = "adapt", priority = Priority.HIGH)
	@Parameter(key = "from")
	@Parameter(key = "to")
	public final Function<Computers.Arity5<I1, I2, I3, I4, I5, O>, Computers.Arity5<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<I3>, RandomAccessibleInterval<I4>, RandomAccessibleInterval<I5>, RandomAccessibleInterval<O>>> lift5 =
		(computer) -> {
			return (raiInput1, raiInput2, raiInput3, raiInput4, raiInput5,
				raiOutput) -> {
				LoopBuilder.setImages(raiInput1, raiInput2, raiInput3, raiInput4,
					raiInput5, raiOutput).multiThreaded().forEachPixel((in1, in2, in3,
						in4, in5, out) -> computer.compute(in1, in2, in3, in4, in5, out));
			};
		};

}
