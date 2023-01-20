package opsEngine.main.complexLift

import Generator
import license
import dontEdit

object FunctionsToComputersAndLift : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities = 1..maxArity

    //[FunctionsToComputersAndLift.java]

    val iterableGenerics get() = '<' + genericParamTypes.joinToString { "Iterable<$it>" } + '>'

    val classGenerics get() = "<I, " + genericParamTypes.joinToString() + '>'

    val iteratorsHaveNext
        get() = when (arity) {
            0 -> "itrout.hasNext()"
            1 -> "itrin.hasNext() && itrout.hasNext()"
            else -> (1..arity).joinToString(" && ") { "itrin$it.hasNext()" } + " && itrout.hasNext()"
        }

    val iteratorsNext
        get() = when (arity) {
            0 -> "itrout.next()"
            1 -> "itrin.next(), itrout.next()"
            else -> (1..arity).joinToString { "itrin$it.next()" } + ", itrout.next()"
        }

    val inputArgs get() = (1..arity).joinToString { "in$it" }

    val iteratorCtor
        get() = when (arity) {
            0 -> emptyList()
            1 -> listOf("Iterator<I> itrin = in.iterator();")
            else -> (1..arity).map { "Iterator<I$it> itrin$it = in$it.iterator();" }
        } + "Iterator<O> itrout = out.iterator();"

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.complexLift;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.priority.Priority;

/**
 * Collection of adaptation Ops to convert {@link Functions} into
 * {@link Computers} with the use of a {@link Function} that copies the output
 * using the first input as a model.
 * 
 * @author Gabriel Selzer
 */
public class FunctionsToComputersAndLift {"""
        forEachArity {
            +"""
	@OpClass(names = "adapt", priority = Priority.LOW)
	public static class Function${arity}ToComputer${arity}AndLiftAfter$generics implements
		Function<$functionArity$generics, $computerArity$iterableGenerics>,
		Op
	{

		@OpDependency(name = "adapt", adaptable = false)
		Function<$functionArity$generics, $computerArity$generics> adaptor;
		@OpDependency(name = "adapt", adaptable = false)
		Function<$computerArity$generics, $computerArity$iterableGenerics> lifter;

		/**
		 * @param function - the Function to convert
		 * @return the function as a Computer
		 */
		@Override
		public $computerArity$iterableGenerics apply(
			$functionArity$generics function)
		{
			return lifter.apply(adaptor.apply(function));
		}

	}

	@OpClass(names = "adapt", priority = Priority.LOW + 1)
	public static class Function${arity}ToComputer${arity}AndLiftBefore$generics implements
		Function<$functionArity$generics, $computerArity$iterableGenerics>,
		Op
	{

		@OpDependency(name = "adapt", adaptable = false)
		Function<$functionArity$iterableGenerics, $computerArity$iterableGenerics> adaptor;
		@OpDependency(name = "adapt", adaptable = false)
		Function<$functionArity$generics, $functionArity$iterableGenerics> lifter;

		/**
		 * @param function - the Function to convert
		 * @return the function as a Computer
		 */
		@Override
		public $computerArity$iterableGenerics apply(
			$functionArity$generics function)
		{
			return adaptor.apply(lifter.apply(function));
		}

	}
"""
        }
        +"""
}
"""
    }
}