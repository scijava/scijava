package opsEngine.test

import Generator
import license
import dontEdit
import kotlin.math.pow

open class OpBuilderTest : Generator() {

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

    //[OpBuilderTest.java]
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

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.ops.api.OpBuilder;

/**
 * Tests {@link OpBuilder}. For each arity, we test the following matches and
 * run the following commands based on the information scenarios.
 * <p>
 * Input TYPES are given (IT):
 * <p>
 * <ol>
 * <li>The output is unspecified (OU):</li>
 * <ol type="a">
 * <li>match: Function, Inplace</li>
 * <li>run: none</li>
 * </ol>
 * <li>The output type is given (OT):</li>
 * <ol type="a">
 * <li>match: Function, Computer</li>
 * <li>run: none</li>
 * </ol>
 * </ol>
 * Input VALUES are given (IV) (N.B. this case applies for Arity0):
 * <p>
 * <ol>
 * <li>The output is unspecified (OU):</li>
 * <ol type="a">
 * <li>match: Function, Inplace</li>
 * <li>run: apply, mutate</li>
 * </ol>
 * <li>The output type is given (OT):</li>
 * <ol type="a">
 * <li>match: Function, Computer</li>
 * <li>run: apply</li>
 * </ol>
 * <li>The output value is given (OV):</li>
 * <ol type="a">
 * <li>match: Computer</li>
 * <li>run: compute</li>
 * </ol>
 * </ol>
 * 
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public class OpBuilderTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new OpBuilderTestOps());
	}

	final double[] halves = new double[10];
	{
		for (int i = 0; i < halves.length; i++)
			halves[i] = i / 2.;
	}

	// -- 0-ary --

	/** Matches a nullary function in a vacuum. */
	@Test
	public void testArity0_OU_matchF() {
		final Producer<?> op = name("test.addDoubles").input().producer();
		final Object result = op.create();
		assertEquals(0., result);
	}

	/** Runs a nullary function in a vacuum. */
	@Test
	public void testArity0_OU_runF() {
		final Object result = name("test.addDoubles").input().create();
		assertEquals(0., result);
	}

	/** Matches a nullary function with a given output type. */
	@Test
	public void testArity0_OT_matchF() {
		final Double expected = 0.;
		final Producer<Double> op = //
			name("test.addDoubles").input().outType(Double.class).producer();
		final Object result = op.create();
		assertEquals(result, expected);
	}

	/** Matches a nullary computer with a given output type. */
	@Test
	public void testArity0_OT_matchC() {
		final double[] actual = { 1, 2, 3 };
		final double[] expected = { 0, 0, 0 };
		final Computers.Arity0<double[]> op = //
			name("test.addArrays").input().outType(double[].class).computer();
		op.compute(actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a nullary function with a given output type. */
	@Test
	public void testArity0_OT_runF() {
		final Double result = name("test.addDoubles").input().outType(Double.class)
			.create();
		assert (0. == result);
	}

	/** Matches a nullary computer with a given output value. */
	@Test
	public void testArity0_OV_matchC() {
		double[] result = { 1, 2, 3 };
		double[] expected = { 0, 0, 0 };
		final Computers.Arity0<double[]> op = //
			name("test.addArrays").input().output(result).computer();
		op.compute(result);
		assertTrue(Arrays.equals(expected, result));
	}

	/** Runs a nullary computer with a given output value. */
	@Test
	public void testArity0_OV_runC() {
		double[] result = { 1, 2, 3 };
		double[] expected = { 0, 0, 0 };
		name("test.addArrays").input().output(result).compute();
		assertTrue(Arrays.equals(expected, result));
	}"""
        forEachArity(1..maxArity) {
            +"""

	// -- $arity-ary --

	/** Matches a $arity-arity function using input types only. */
	@Test
	public void testArity${arity}_IT_OU_matchF() {"""
            for (a in 1..arity)
                +"""
		final Double ${functionInputName(a)} = 1.;"""

            +"""
		final Double expected = $arity.;
		final $functionArity$wildcardOutputDoubles op = //
			name("test.addDoubles") //
				.inType($doubleClassString) //
				.function();
		final Object actual = op.apply($args);
		assertEquals(actual, expected);
	}
"""
            for (i in 1..arity) {
                +"""
	/** Matches a $arity-arity inplace$i with a given output type. */
	@Test
	public void testArity${arity}_IT_OU_matchI$i() {"""
                for (a in 1..arity)
                    +"""
		final double[] ${functionInputName(a)} = { 1, 2, 3 };"""

                +"""
		final double[] expected = $inplaceExpectedArray;
		final ${inplaceType(i)}$arrayGenerics op = //
			name("test.mulArrays${arity}_$i") //
				.inType($arrayClassString) //
				.${inplaceMethod(i)}();
		op.mutate($args);
		assertTrue(Arrays.equals(${inplaceActualArg(i)}, expected));
	}
"""
            }
            +"""
	/** Matches a $arity-arity function using input types + output type. */
	@Test
	public void testArity${arity}_IT_OT_matchF() {"""
            for (a in 1..arity)
                +"""
		final Double ${functionInputName(a)} = 1.;"""

            +"""
		final Double expected = $arity.;
		final $functionArity$genericDoubles op = //
			name("test.addDoubles").inType($doubleClassString) //
				.outType(Double.class).function();
		final double actual = op.apply($args);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a $arity-arity computer using input types + output type. */
	@Test
	public void testArity${arity}_IT_OT_matchC() {"""
            for (a in 1..arity)
                +"""
		final double[] ${functionInputName(a)} = { 1, 2, 3 };"""

            +"""
		double[] actual = { 0, 0, 0 };
		double[] expected = $computerExpectedArray;
		final $computerArity$computerArrayGenerics op = //
			name("test.addArrays") //
				.inType($arrayClassString) //
				.outType(double[].class).computer();
		op.compute($args, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Matches a $arity-arity function using input value only. */
	@Test
	public void testArity${arity}_IV_OU_matchF() {"""
            for (a in 1..arity)
                +"""
		final Double ${functionInputName(a)} = 1.;"""

            +"""
		final Double expected = $arity.;
		final $functionArity$wildcardOutputDoubles op = //
			name("test.addDoubles") //
				.input($args) //
				.function();
		final Object actual = op.apply($args);
		assertEquals(actual, expected);
	}
"""
            for (i in 1..arity) {
                +"""
	/** Matches a $arity-arity inplace$i with a given output value. */
	@Test
	public void testArity${arity}_IV_OU_matchI$i() {"""
                for (a in 1..arity)
                    +"""
		final double[] ${functionInputName(a)} = { 1, 2, 3 };"""

                +"""
		double[] expected = $inplaceExpectedArray;
		final ${inplaceType(i)}$arrayGenerics op = //
			name("test.mulArrays${arity}_$i").input($args) //
				.${inplaceMethod(i)}();
		op.mutate($args);
		assertTrue(Arrays.equals(expected, ${functionInputName(i)}));
	}
"""
            }
            +"""
	/** Runs a $arity-arity function using input value only. */
	@Test
	public void testArity${arity}_IV_OU_runF() {"""
            for (a in 1..arity)
                +"""
		final Double ${functionInputName(a)} = 1.;"""

            +"""
		final Double expected = $arity.;
		final Object actual = name("test.addDoubles") //
			.input($args) //
			.apply();
		assertEquals(actual, expected);
	}
"""
            for (i in 1..arity) {
                +"""
	/** Runs a $arity-arity inplace${i} with a given output value. */
	@Test
	public void testArity${arity}_IV_OU_runI$i() {"""
                for (a in 1..arity)
                    +"""
		final double[] ${functionInputName(a)} = { 1, 2, 3 };"""

                +"""
		final double[] expected = $inplaceExpectedArray;
		name("test.mulArrays${arity}_$i") //
			.input($args) //
			.${mutateMethod(i)}();
		assertTrue(Arrays.equals(${functionInputName(i)}, expected));
	}
"""
            }
            +"""
	/** Matches a $arity-arity function using input value + output type. */
	@Test
	public void testArity${arity}_IV_OT_matchF() {"""
            for (a in 1..arity)
                +"""
		final Double ${functionInputName(a)} = 1.;"""

            +"""
		final Double expected = $arity.;
		final $functionArity$genericDoubles op = //
			name("test.addDoubles").input($args) //
				.outType(Double.class).function();
		final double actual = op.apply($args);
		assertEquals(actual, expected, 0.);
	}

	/** Matches a unary computer using input types + output type. */
	@Test
	public void testArity${arity}_IV_OT_matchC() {"""
            for (a in 1..arity)
                +"""
		double[] ${functionInputName(a)} = { 1, 2, 3 };"""

            +"""
		double[] actual = { 0, 0, 0 };
		double[] expected = $computerExpectedArray;
		final $computerArity$computerArrayGenerics op = //
			name("test.addArrays") //
				.input($args) //
				.output(actual).computer();
		op.compute($args, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary function using input value + output type. */
	@Test
	public void testArity${arity}_IV_OT_runF() {"""
            for (a in 1..arity)
                +"""
		final Double ${functionInputName(a)} = 1.;"""

            +"""
		final Double expected = $arity.;
		final Object actual = name("test.addDoubles") //
			.input($args) //
			.outType(Double.class).apply();
		assertEquals(actual, expected);
	}

	/** Matches a unary computer using input value + output value. */
	@Test
	public void testArity${arity}_IV_OV_matchC() {"""
            for (a in 1..arity)
                +"""
		double[] ${functionInputName(a)} = { 1, 2, 3 };"""

            +"""
		double[] actual = { 0, 0, 0 };
		double[] expected = $computerExpectedArray;
		final $computerArity$computerArrayGenerics op = //
			name("test.addArrays") //
				.input($args) //
				.output(actual).computer();
		op.compute($args, actual);
		assertTrue(Arrays.equals(actual, expected));
	}

	/** Runs a unary computer using input value + output value. */
	@Test
	public void testArity${arity}_IV_OV_runC() {"""
            for (a in 1..arity)
                +"""
		double[] ${functionInputName(a)} = { 1, 2, 3 };"""

            +"""
		double[] actual = { 0, 0, 0 };
		double[] expected = $computerExpectedArray;
		name("test.addArrays") //
			.input($args) //
			.output(actual).compute();
		assertTrue(Arrays.equals(actual, expected));
	}"""
        }
        +"""

	// -- Helper methods --

	private OpBuilder name(String opName) {
		return ops.op(opName);
	}
}
"""
    }
}