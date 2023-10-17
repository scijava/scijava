package ops2Adapt.complexLift

import Generator
import dontEdit
import joinToStringComma
import license

object ComputersToFunctionsAndLift : Generator() {

    override val arities: IntRange
        get() = 2..maxArity
    override fun generate() {
        +"""
$license

$dontEdit

package net.imagej.ops2.adapt.complexLift;

import net.imglib2.Dimensions;
import net.imglib2.img.Img;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Producer;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ComputersToFunctionsAndLift {

	/**
	 * Lifting function for mapping Computers Ops to Functions that operate on Imgs.
	 *
	 * @param <I>
	 *            Type of the first image
	 * @param <O>
	 *            Type of the second image
	 *@implNote op names='adapt', priority='-100.'
	 */
	public static class ImgComputer1ToFunction1AndLiftViaSource<I, O>
			implements Function<Computers.Arity1<I, O>, Function<Img<I>, Img<O>>>,
			Op
	{
		@OpDependency(name = "adapt", adaptable = false)
		Function<Computers.Arity1<I, O>, Computers.Arity1<Img<I>, Img<O>>> lifter;
		@OpDependency(name = "create", adaptable = false)
		BiFunction<Dimensions, O, Img<O>> createImgFromDimsAndType;
		@OpDependency(name = "create", adaptable = false)
		Producer<O> outTypeProducer;

		/**
		 * @param computer the Computer to convert
		 * @return {@code computer} as a Function
		 */
		@Override
		public Function<Img<I>, Img<O>> apply(Computers.Arity1<I, O> computer) {
			O outType = outTypeProducer.get();
			Computers.Arity1<Img<I>, Img<O>> lifted = lifter.apply(computer);
			return (inImg) -> {
				Img<O> outImg = createImgFromDimsAndType.apply(inImg, outType);
				lifted.compute(inImg, outImg);
				return outImg;
			};
		}

	}"""
        forEachArity {
            +"""
	/**
	 * Lifting function for mapping Computers Ops to Functions that operate on Imgs.
	 *"""
            for (a in 1..arity)
                +"""
	 * @param <I$a>
	 *            Type of the ${arityName(a)} image
"""
            +"""
	 * @param <O>
	 *            Type of the output image
	 * @implNote op names='adapt', priority='-100.'
	 */
	public static class ImgComputer${arity}ToFunction${arity}AndLiftViaSource<${(1..arity).joinToStringComma { "I$it" }}O>
			implements Function<Computers.Arity$arity<${(1..arity).joinToStringComma { "I$it" }}O>, $functionName<${(1..arity).joinToStringComma { "Img<I$it>" }}Img<O>>>,
			Op
	{
		@OpDependency(name = "adapt", adaptable = false)
		Function<Computers.Arity$arity<${(1..arity).joinToStringComma { "I$it" }}O>, Computers.Arity$arity<${(1..arity).joinToStringComma { "Img<I$it>" }}Img<O>>> lifter;
		@OpDependency(name = "adapt", adaptable = false)
		Function<Computers.Arity$arity<${(1..arity).joinToStringComma { "Img<I$it>" }} Img<O>>, $functionName<${(1..arity).joinToStringComma { "Img<I$it>" }}Img<O>>> adapter;

		/**
		 * @param computer the Computer to convert
		 * @return {@code computer} as a Function
		 */
		@Override
		public $functionName<${(1..arity).joinToStringComma { "Img<I$it>" }} Img<O>> apply(Computers.Arity$arity<${(1..arity).joinToStringComma { "I$it" }} O> computer) {
			return adapter.apply(lifter.apply(computer));
		}
	}"""
        }
        +"""
}
"""
    }
}