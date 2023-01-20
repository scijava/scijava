package opsEngine.test.lift

import Generator
import license
import dontEdit

object ComputerToIterablesTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[ComputerToIterablesTest.java]

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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.OpBuilderTestOps;

/**
 * Tests the adaptation of {@link Computers} running on a type into
 * {@link Computers} running on an {@link Iterable} of that type.
 * 
 * @author Gabriel Selzer
 */
public class ComputerToIterablesTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new ComputerToIterables());
		ops.register(new OpBuilderTestOps());
	}
"""
        forEachArity {
            +"""
	@Test
	public void testComputer${arity}ToIterables() {
		final List<double[]> in = Arrays.asList(new double[] { 1, 2, 3 });
		final List<double[]> out = Arrays.asList(new double[] { 0, 0, 0 });
		final List<double[]> expected = //
			Arrays.asList(new double[] $expectedList);
		ops.op("test.addArrays") //
			.input($inputList) //
			.output(out).compute();
		assertArrayEquals(out.toArray(), expected.toArray());
	}
"""
        }
        +"""
}
"""
    }
}