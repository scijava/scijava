package opsEngine.main.lift

import Generator
import license
import dontEdit

object ComputerToIterables : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[ComputerToIterables.java]

    val iterableGenerics get() = '<' + genericParamTypes.joinToString { "Iterable<$it>" } + '>'

    val classGenerics get() = "<I, " + genericParamTypes.joinToString() + '>'

    val iteratorsHaveNext
        get() = when (arity) {
            0 -> "itrout.hasNext()"
            1 -> "itrin.hasNext() && itrout.hasNext()"
            else -> (1..arity).joinToString(" && ") { "itrin" + it + ".hasNext()" } + " && itrout.hasNext()"
        }

    val iteratorsNext
        get() = when (arity) {
            0 -> "itrout.next()"
            1 -> "itrin.next(), itrout.next()"
            else -> (1..arity).joinToString { "itrin" + it + ".next()" } + ", itrout.next()"
        }

    val iteratorInputs get() = (1..arity).joinToString { "in$it" }

    val iteratorCtor
        get() = when (arity) {
            0 -> emptyList()
            1 -> listOf("Iterator<I> itrin = in.iterator();")
            else -> (1..arity).map { "Iterator<I$it> itrin$it = in$it.iterator();" }
        } + "Iterator<O> itrout = out.iterator();"

    override fun generate() {
        arity = maxArity
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.lift;

import java.util.Iterator;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpCollection;

/**
 * Converts {@link Computers} operating on single types to {@link Computers}
 * that operate on {@link Iterable}s of types. N.B. it is the user's
 * responsibility to pass {@link Iterable}s of the same length (otherwise the Op
 * will stop when one of the {@link Iterable}s runs out of {@link Object}s).
 * 
 * @author Gabriel Selzer
 */
public class ComputerToIterables$classGenerics implements OpCollection {
"""
        forEachArity(0..maxArity) {
            +"""
	@OpField(names = "adapt")
	public final Function<$computerArity$generics, $computerArity$iterableGenerics> liftComputer$arity = 
		(computer) -> {
			return ($computeArgs) -> {"""
            for (a in iteratorCtor)
                +"""
				$a"""
            +"""
				while ($iteratorsHaveNext) {
					computer.compute($iteratorsNext);
				}
			};
		};
"""
        }
        +"""
}

"""
    }
}