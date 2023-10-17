package ops2Adapt

import Generator
import dontEdit
import joinToStringComma
import license

object LiftComputersToImg : Generator() {

    init {
        maxArity = 5
    }

    override val arities: IntRange
        get() = 1..maxArity

    override fun generate() {
        +"""
$license

$dontEdit

package net.imagej.ops2.adapt;


import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.Type;
import org.scijava.function.Computers;

import java.util.function.Function;

public class LiftComputersToImg<${arities.joinToStringComma { "I$it" }}O extends Type<O>> {"""
        forEachArity {
            +"""

	/**
	 * @implNote op names='adapt', priority='100.'
	 */
	public final Function<Computers.Arity$arity<${(1..arity).joinToStringComma { "I$it" }}O>, Computers.Arity$arity<${(1..arity).joinToStringComma { "Img<I$it>" }}Img<O>>> lift$arity =
			(computer) -> {
				return (${(1..arity).joinToStringComma { "raiInput$it" }}raiOutput) -> {
					LoopBuilder.setImages(${(1..arity).joinToStringComma { "raiInput$it" }}raiOutput).multiThreaded()
					    .forEachPixel((${(1..arity).joinToStringComma { "in$it" }}out) -> computer.compute(${(1..arity).joinToStringComma { "in$it" }}out));
				};
			};"""
        }
        +"""
}
"""
    }
}