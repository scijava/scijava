package opsEngine.main.functional

import Generator
import license
import dontEdit

object FunctionsToComputers : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities = 1..maxArity

    //[FunctionsToComputers.java]

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

/**
 * Collection of adaptation Ops to convert {@link Computers} into
 * {@link Functions} with the use of a {@link Computer} that copies the output
 * of the function into the preallocated argument.
 * 
 * @author Gabriel Selzer
 */
public class FunctionsToComputers {
"""
        forEachArity(0..maxArity) {
            +"""
	@OpClass(names = "adapt")
	public static class Function${arity}ToComputer${arity}$generics implements Function<$functionArity$generics, $computerArity$generics>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public $computerArity$generics apply($functionArity$generics function) {
			return ($computeArgs) -> {
				O temp = function.${if (arity == 0) "create()" else "apply($applyArgs)"};
				copyOp.compute(temp, out);
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