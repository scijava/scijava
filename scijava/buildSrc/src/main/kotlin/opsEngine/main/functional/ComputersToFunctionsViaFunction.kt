package opsEngine.main.functional

import Generator
import license
import dontEdit

object ComputersToFunctionsViaFunction : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities = 1..maxArity

    //    [ComputersToFunctionsViaFunction.java]

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

/**
 * Collection of adaptation Ops to convert {@link Computers} into
 * {@link Functions} with the use of a {@link Function} that creates the output
 * using the first input as a model.
 * 
 * @author Gabriel Selzer
 */
public class ComputersToFunctionsViaFunction {
"""
        forEachArity {
            +"""
	@OpClass(names = "adapt")
	public static class Computer${arity}ToFunction${arity}ViaFunction$generics
			implements Function<$computerArity$generics, $functionArity$generics>,
			Op
		 {

		@OpDependency(name = "create", adaptable = false)
		Function<I${if (arity == 1) "" else "1"}, O> creator;

		/**
		 * @param computer the Computer to adapt
		 * @return computer, adapted into a Function
		 */
		@Override
		public $functionArity$generics apply($computerArity$generics computer) {
			return ($applyArgs) -> {
				O out = creator.apply(in${if (arity == 1) "" else "1"});
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