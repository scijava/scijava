package opsEngine.test.lift

import Generator
import license
import dontEdit

object FunctionToArraysTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[FunctionToArraysTest.java]

    val numericalGenerics get() = '<' + (1..arity + 1).joinToString { "NumericalThing" } + '>'

    val getNumbers
        get() = when (arity) {
            0 -> "0"
            else -> genericsNamesList.dropLast(1).joinToString(" + ") { "$it.getNumber()" }
        }

    val inputArgs
        get() = when (arity) {
            0 -> ""
            else -> (1..arity).joinToString { "input" }
        }

    fun functionInputName(num: Int) = if (arity == 1) "input" else "input$num"

    val inputList get() = (1..arity).joinToString { "in" }

    val expectedList get() = "{ " + (1..3).joinToString { "" + it * arity + '.' } + " }"

    val args
        get() = when (arity) {
            0 -> ""
            1 -> "input"
            else -> (1..arity).joinToString { "input$it" }
        }

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.lift;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

/**
 * Tests the adaptation of {@link Functions} running on a type into
 * {@link Functions} running on arrays of that type.
 * 
 * @author Gabriel Selzer
 */
public class FunctionToArraysTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new FunctionToArraysTest());
		ops.register(new FunctionToArrays());
	}

	/**
	 * @author Gabriel Selzer
	 */
	private class NumericalThing {

		private int number;

		public NumericalThing(int num) {
			number = num;
		}

		public int getNumber() {
			return number;
		}
	}
"""
        forEachArity {
            +"""
	@OpField(names = "test.liftArrayF")
	public final $functionArity$numericalGenerics alterThing$arity = (
		$applyArgs) -> new NumericalThing($getNumbers);

	@Test
	public void testFunction${arity}ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input($inputArgs) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals($arity * i, output[i].getNumber());
		}
	}
"""
        }
        +"""
}
"""
    }
}