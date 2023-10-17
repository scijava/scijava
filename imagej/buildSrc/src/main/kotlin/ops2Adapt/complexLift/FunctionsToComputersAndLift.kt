package ops2Adapt.complexLift

import Generator
import dontEdit
import joinToStringComma
import license

object FunctionsToComputersAndLift : Generator() {

    //    .include templates/main/java/net/imagej/ops2/adapt/complexLift/Globals.list

    override val arities: IntRange
        get() = 1..maxArity

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

public class FunctionsToComputersAndLift {"""
        forEachArity {
            +"""
	/**
	 * Lifting function for mapping Functions Ops to Computers that operate on Imgs.
	 * Adapts first, then lifts.
	 *"""
            for (a in 1..arity)
                +"""
	 * @param <I$a>
	 *            Type of the ${arityName(a)} image
"""
            +"""
	 * @param <O>
	 *            The image return type
	 *@implNote op names='adapt', priority='-100.'
	 */
	public static class ImgFunction${arity}ToComputer${arity}AndLiftAfter<${(1..arity).joinToStringComma { "I$it" }}O>
			implements Function<$functionName<${(1..arity).joinToStringComma { "I$it" }}O>, Computers.Arity$arity<${(1..arity).joinToStringComma { "Img<I$it>" }}Img<O>>>,
			Op
	{
		@OpDependency(name = "adapt", adaptable = false)
		Function<$functionName<${(1..arity).joinToStringComma { "I$it" }}O>, Computers.Arity$arity<${(1..arity).joinToStringComma { "I$it" }}O>> adapter;
		@OpDependency(name = "adapt", adaptable = false)
		Function<Computers.Arity$arity<${(1..arity).joinToStringComma { "I$it" }}O>, Computers.Arity$arity<${(1..arity).joinToStringComma { "Img<I$it>" }}Img<O>>> lifter;

		/**
		 * @param function - the Function to convert
		 * @return {@code function} as a computer
		 */
		@Override
		public Computers.Arity$arity<${(1..arity).joinToStringComma { "Img<I$it>" }}Img<O>> apply($functionName<${(1..arity).joinToStringComma { "I$it" }}O> function) {
			return lifter.apply(adapter.apply(function));
		}
	}

	/**
	 * Lifting function for mapping Functions Ops to Computers that operate on Imgs.
	 * Lifts first, then adapts.
	 *"""
            for (a in 1..arity)
                +"""
	 * @param <I$a>
	 *            Type of the ${arityName(a)} image"""
            +"""
	 * @param <O>
	 *            The image return type
	 *@implNote op names='adapt', priority='-99.'
	 */
	public static class ImgFunction${arity}ToComputer${arity}AndLiftBefore<${(1..arity).joinToStringComma { "I$it" }}O>
			implements Function<$functionName<${(1..arity).joinToStringComma{"I$it"}}O>, Computers.Arity$arity<${(1..arity).joinToStringComma{"Img<I$it>"}}Img<O>>>,
			Op
	{
		@OpDependency(name = "adapt", adaptable = false)
		Function<$functionName<${(1..arity).joinToStringComma{"I$it"}}O>, $functionName<${(1..arity).joinToStringComma{"Img<I$it>"}}Img<O>>> lifter;
		@OpDependency(name = "adapt", adaptable = false)
		Function<$functionName<${(1..arity).joinToStringComma{"Img<I$it>"}}Img<O>>, Computers.Arity$arity<${(1..arity).joinToStringComma{"Img<I$it>"}}Img<O>>> adapter;

		/**
		 * @param function - the Function to convert
		 * @return {@code function} as a Computer
		 */
		@Override
		public Computers.Arity$arity<${(1..arity).joinToStringComma{"Img<I$it>"}}Img<O>> apply($functionName<${(1..arity).joinToStringComma{"I$it"}}O> function) {
			return adapter.apply(lifter.apply(function));
		}
	}
"""
        }
        +"""
}
"""
    }
}