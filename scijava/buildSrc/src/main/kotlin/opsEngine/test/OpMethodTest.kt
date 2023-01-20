package opsEngine.test

import Generator
import license
import kotlin.math.pow

object OpMethodTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    //    arities = (0..maxArity).collect()

    val inputDoubles get() = (0..arity).map { "Double" }

    val genericDoubles get() = '<' + inputDoubles.joinToString() + '>'

    val args
        get() = when (arity) {
            0 -> ""
            1 -> "input"
            else -> (1..arity).joinToString { "input$it" }
        }

    val inputArrays get() = (0 until arity).map { "double[]" }

    val arrayGenerics get() = '<' + inputArrays.joinToString() + '>'

    val computerArrayGenerics: String
        get() {
            arity++
            return arrayGenerics.also { arity-- }
        }

    //[OpMethodTest.java]
    val computerInputs
        get() = when (arity) {
            0 -> ""
            else -> (1..arity).joinToString { "in" }
        }

    fun typeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun inplaceArgs(io: Int) = typeVarNums(io).joinToString { if (it == 'O') "io" else "in" }

    val wildcardOutputDoubles: String
        get() {
            arity--
            return inputDoubles.joinToString(prefix = "<", postfix = ", ?>").also { arity++ }
        }

    val doubleClassString: String
        get() {
            arity--
            return inputDoubles.joinToString { "$it.class" }.also { arity++ }
        }

    val arrayClassString get() = inputArrays.joinToString { "$it.class" }

    fun functionInputName(num: Int) = if (arity == 1) "input" else "input$num"

    val inplaceExpectedArray get() = "{ " + (1..3).joinToString { "" + it.toFloat().pow(arity) } + " }"

    val computerExpectedArray get() = "{ " + (1..3).joinToString { "" + arity * it } + " }"

    fun inplaceActualArg(i: Int) = if (arity == 1) "input" else "input$i"

    fun inplaceMethod(i: Int) = if (arity == 1) "inplace" else "inplace$i"

    fun mutateMethod(i: Int) = if (arity == 1) "mutate" else "mutate$i"

    override fun generate() {
        +"""
$license

package org.scijava.ops.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

/**
 * Tests the construction of {@link OpMethod}s.
 * 
 * @author Gabriel Selzer
 * @author Marcel Wiedenmann
 */
public class OpMethodTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpMethodTestOps());
	}

	// -- Functions -- //

	@Test
	public void testOpMethodProducer() {
		final Integer out = ops.op("test.multiplyNumericStrings").input()
			.outType(Integer.class).create();
		final Integer expected = Integer.valueOf(1);
		assertEquals(expected, out);
	}"""
        forEachArity(1..maxArity) {
            +"""

	@Test
	public void testOpMethodFunction$arity() {
		final String in = "2";
		final Integer out = ops.op("test.multiplyNumericStrings").input($computerInputs)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, $arity), out, 0);
	}"""
        }
        +"""

	// -- Computers -- //

	public List<Double> expected(double expected, int size) {
		List<Double> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			list.add(expected);
		}
		return list;
	}"""
        forEachArity {
            +"""

	@Test
	public void testOpMethodComputer$arity() {
		String in = "$arity";
		List<Double> out = new ArrayList<>();
		ops.op("test.doubleList").input($computerInputs)
			.output(out).compute();
		assertEquals(expected($arity, $arity), out);
	}"""
        }
        +"""

	// -- Inplaces -- //

	private boolean outputExpected(double[] array, int multiplier) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] != (i + 1) * multiplier) return false;
		}
		return true;
	}"""
        forEachArity(1..maxArity) {
            for (a in 1..arity)
                +"""

	@Test
	public void testOpMethodInplace${inplaceSuffix(a)}() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.addDoubles${inplaceSuffix(a)}").input(${inplaceArgs(a)}).mutate${if (arity == 1) "" else a}();
		assertTrue(outputExpected(io, $arity));
	}"""
        }
        +"""

	// -- Dependent Functions -- //"""
        forEachArity(1..maxArity) {
            +"""

	@Test
	public void testDependentMethodFunction$arity() {
		final String in = "2";
		final Integer out = ops.op("test.dependentMultiplyStrings").input($computerInputs)
			.outType(Integer.class).apply();
		assertEquals(Math.pow(2, $arity), out, 0);
	}"""
        }
        +"""

	// -- Dependent Computers -- //"""
        forEachArity(1..maxArity) {
            +"""

	@Test
	public void testDependentMethodComputer$arity() {
		String in = "$arity";
		List<Double> out = new ArrayList<>();
		ops.op("test.dependentDoubleList").input($computerInputs)
			.output(out).compute();
		assertEquals(expected($arity, $arity), out);
	}"""
        }
        +"""

	// -- Dependent Inplaces -- //"""
        forEachArity(1..maxArity) {
            for (a in 1..arity)
                +"""

	@Test
	public void testDependentMethodInplace${inplaceSuffix(a)}() {
		final double[] io = { 1., 2., 3. };
		final double[] in = { 1., 2., 3. };
		ops.op("test.dependentAddDoubles${inplaceSuffix(a)}").input(${inplaceArgs(a)}).mutate${if (arity == 1) "" else a}();
		assertTrue(outputExpected(io, $arity));
	}"""
        }
        +"""
}
"""
    }
}