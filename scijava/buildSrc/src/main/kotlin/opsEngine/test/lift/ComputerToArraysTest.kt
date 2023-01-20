package opsEngine.test.lift

import Generator
import license
import dontEdit

object ComputerToArraysTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities = 1..maxArity

    //[ComputerToArraysTest.java]

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * Tests the adaptation of {@link Computers} running on a type into
 * {@link Computers} running on arrays of that type.
 * 
 * @author Gabriel Selzer
 */
public class ComputerToArraysTest extends AbstractTestEnvironment implements OpCollection{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new ComputerToArraysTest());
		ops.register(new ComputerToArrays());
	}
	
	/**
	 * @author Gabriel Selzer
	 */
	private class NumericalThing {

		private int number;

		public NumericalThing() {
			number = -1;
		}

		public NumericalThing(int num) {
			number = num;
		}

		public void setNumber(int newNum) {
			number = newNum;
		}

		public int getNumber() {
			return number;
		}
	}
"""
        forEachArity(0..maxArity) {
            +"""
	@OpField(names = "test.liftArrayC")
	public final $computerArity$numericalGenerics alterThing$arity = ($computeArgs) -> {out.setNumber($getNumbers);};

	@Test
	public void testComputer${arity}ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.op("test.liftArrayC").input($inputArgs).output(output).compute();

		for(int i = 0; i < output.length; i++) {
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