package opsEngine.main.lift

import Generator
import license
import dontEdit

object ComputerToArrays : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[ComputerToArrays.java]

    val arrayGenerics get() = '<' + genericParamTypes.joinToString { it + "[]" } + '>'

    val classGenerics get() = "<I, " + genericParamTypes.joinToString() + '>'

    val arraysAtPosI
        get() = when (arity) {
            0 -> "out[i]"
            1 -> "in[i], out[i]"
            else -> (1..arity).joinToString { "in$it[i]" } + ", out[i]"
        }

    override fun generate() {
        arity = maxArity
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.lift;

import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpCollection;

/**
 * Collection of ops designed to lift {@link Computers} to operate
 * on arrays. TODO: multi-threading support
 * 
 * @author Gabriel Selzer
 */
public class ComputerToArrays$classGenerics implements OpCollection {

	private int minLength(Object[]... arrays) {
		int minLength = Integer.MAX_VALUE;
		for (Object[] array : arrays)
			if (array.length < minLength) minLength = array.length;
		return minLength;
	}
"""
        forEachArity(0..maxArity) {
            +"""
	@OpField(names = "adapt", params = "fromOp, toOp")
	public final Function<$computerArity$generics, $computerArity$arrayGenerics> liftComputer$arity =
		(computer) -> {
			return ($computeArgs) -> {
				int max = minLength($computeArgs);
				for (int i = 0; i < max; i++) {
					computer.compute($arraysAtPosI);
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