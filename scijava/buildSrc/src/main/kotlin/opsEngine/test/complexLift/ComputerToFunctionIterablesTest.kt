package opsEngine.test.complexLift

import Generator
import license
import dontEdit

object ComputerToFunctionIterablesTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[ComputerToFunctionIterablesTest.java]

    fun functionInputName(num: Int) = if (arity == 1) "input" else "input$num"

    val inputList get() = (1..arity).joinToString { "in" }

    val expectedList get() = '{' + (1..3).joinToString { "" + it * arity + '.' } + " }"

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

package org.scijava.ops.engine.adapt.complexLift;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.OpBuilderTestOps;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction;
import org.scijava.ops.engine.adapt.lift.FunctionToIterables;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.types.Nil;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests the adaptation of {@link Computers} running on a type into
 * {@link Computers} running on an {@link Iterable} of that type.
 * 
 * @author Gabriel Selzer
 *
 */
public class ComputerToFunctionIterablesTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		Object[] lifters = objsFromNoArgConstructors(ComputersToFunctionsAndLift.class.getDeclaredClasses());
		ops.register(lifters);
		ops.register(new FunctionToIterables());
		Object[] adapters = objsFromNoArgConstructors(ComputersToFunctionsViaFunction.class.getDeclaredClasses());
		ops.register(adapters);
		ops.register(new CreateOpCollection());
		ops.register(new OpBuilderTestOps());
	}
"""
        forEachArity {
            +"""
	@Test
	public void testComputer${arity}ToIterables() {
		final List<double[]> in = Arrays.asList(new double[] { 1, 2, 3 });
		final List<double[]> expected = Arrays.asList(new double[] $expectedList);
		final Iterable<double[]> out = ops.op("test.addArrays").input($inputList).outType(new Nil<Iterable<double[]>>(){}).apply();
		assertArrayEquals(out.iterator().next(), expected.get(0), 0);
	}
"""
        }
        +"""
}
"""
    }
}