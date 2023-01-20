package opsEngine.test.lift

import Generator
import license
import dontEdit

object FunctionToIterablesTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[FunctionToIterablesTest.java]

    fun functionInputName(num: Int) = if (arity == 1) "input" else "input$num"

    val inputList get() = (1..arity).joinToString { "in" }

    val inplaceExpectedList get() = "Arrays.asList(" + (1..3).joinToString { "" + it * arity + '.' } + ");"

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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.OpBuilderTestOps;
import org.scijava.types.Nil;

/**
 * Tests the adaptation of {@link Functions} running on a type into
 * {@link Functions} running on an {@link Iterable} of that type.
 * 
 * @author Gabriel Selzer
 */
public class FunctionToIterablesTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new FunctionToIterables());
		ops.register(new OpBuilderTestOps());
	}
"""
        forEachArity {
            +"""
	@Test
	public void testFunction${arity}ToIterables() {
		List<Double> in = Arrays.asList(1., 2., 3.);
		List<Double> expected = $inplaceExpectedList
		Iterable<Double> output = ops.op("test.addDoubles") //
			.input($inputList) //
			.outType(new Nil<Iterable<Double>>()
			{}).apply();
		assertIterationsEqual(expected, output);
	}
"""
        }
        +"""
}
"""
    }
}