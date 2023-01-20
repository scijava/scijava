package opsEngine.main.functional

import Generator
import license
import dontEdit

object ComputersToFunctionsViaSource : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[ComputersToFunctionsViaSource.java]

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Producer;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.priority.Priority;

/**
 * Collection of adaptation Ops to convert {@link Computers} into
 * {@link Functions} with the use of a {@link Producer} that creates the output
 * using the first input as a model.
 * 
 * @author Gabriel Selzer
 */
public class ComputersToFunctionsViaSource {
"""
        forEachArity(0..maxArity) {
            +"""
	@OpClass(names = "adapt", priority = Priority.LOW)
	public static class Computer${arity}ToFunction${arity}ViaSource$generics
			implements Function<$computerArity$generics, $functionArity$generics>, 
			Op
		{

		@OpDependency(name = "create", adaptable = false)
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public $functionArity$generics apply($computerArity$generics computer) {
			return (${if (arity == 0) "" else applyArgs}) -> {
				O out = creator.create();
				computer.compute($computeArgs);
				return out;
			};
		}

	}
"""
        }
        +"""
}
"""
    }
}