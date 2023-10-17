package ops2Adapt

import Generator
import dontEdit
import joinToStringComma
import license

object LiftFunctionsToImg : Generator() {

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
import net.imglib2.util.Util;
import org.scijava.function.Functions;

import java.util.function.BiFunction;
import java.util.function.Function;

public class LiftFunctionsToImg<${arities.joinToStringComma { "I$it" }}O extends Type<O>> {"""
        forEachArity {
            +"""

	/**
	 * @implNote op names='adapt', priority='100.'
	 */
	public final Function<$functionName<${(1..arity).joinToStringComma { "I$it" }}O>, $functionName<${(1..arity).joinToStringComma { "Img<I$it>" }}Img<O>>> lift$arity =
			(function) -> {
				return (${(1..arity).joinToStringComma(lastComma = false) { "raiInput$it" }}) -> {
					O outType = function.apply(${(1..arity).joinToStringComma(lastComma = false) { "Util.getTypeFromInterval(raiInput$it)" }});
					Img<O> outImg = Util.getSuitableImgFactory(raiInput1, outType).create(
							raiInput1);
					LoopBuilder.setImages(${(1..arity).joinToStringComma { "raiInput$it" }}outImg).multiThreaded()
					    .forEachPixel((${(1..arity).joinToStringComma { "in$it" }}out) -> out.set(function.apply(${(1..arity).joinToStringComma(lastComma = false) { "in$it" }})));
					return outImg;
				};
			};"""
        }
        +"""
}
"""
    }
}