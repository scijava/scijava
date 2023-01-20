package opsEngine.test.lift

import Generator
import license
import dontEdit

object InplaceToArraysTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[InplaceToArraysTest.java]
    fun typeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun genericsList(io: Int) = typeVarNums(io).map { "I$it" }

    val numericalGenerics get() = '<' + (1..arity).joinToString { "NumericalThing" } + '>'

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

    fun mutateValues(io: Int) = genericsList(io).map { if (it.substring(1) == "O") "io" else "in${it.substring(1)}" }

    val expectedList get() = "{ " + (1..3).joinToString { "" + it * arity + '.' } + " }"

    val args
        get() = when (arity) {
            0 -> ""
            1 -> "input"
            else -> (1..arity).joinToString { "input$it" }
        }

    fun inputList(io: Int) = genericsList(io).joinToString { if (it.substring(1) == "O") "io" else "input" }

    fun mutateParams(io: Int) = mutateValues(io).joinToString()

    fun mutateMethod(i: Int) = if (arity == 1) "mutate" else "mutate$i"

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.lift;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Inplaces;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * Tests the adaptation of {@link Inplaces} running on a type into
 * {@link Inplaces} running on arrays of that type.
 * 
 * @author Gabriel Selzer
 */
public class InplaceToArraysTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new InplaceToArraysTest());
		ops.register(new InplaceToArrays());
	}

	/**
	 * @author Gabriel Selzer
	 */
	private class NumericalThing {

		private int number;

		public NumericalThing(int num) {
			number = num;
		}
		
		public void addNumber(int other) {
			number += other;
		}

		public int getNumber() {
			return number;
		}
	}"""
        forEachArity {
            for (a in 1..arity) {
                +"""

	@OpField(names = "test.liftArrayI$a")
	public final ${inplaceType(a)}$numericalGenerics alterThing${inplaceSuffix(a)} = (
		${mutateParams(a)}) -> {"""
                for (param in mutateValues(a))
                    if (arity == 1)
                        +"""
		io.addNumber(0);"""
                    else if (param != "io")
                        +"""
		io.addNumber(${param}.getNumber());"""
                +"""
	};

	@Test
	public void testInplace${inplaceSuffix(a)}ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] io = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
			ops.op("test.liftArrayI$a") //
			.input(${inputList(a)}) //
			.${mutateMethod(a)}();

		for (int i = 0; i < input.length; i++) {
			Assertions.assertEquals($arity * i, io[i].getNumber());
		}
	}"""
            }
        }
        +"""

}
"""
    }
}