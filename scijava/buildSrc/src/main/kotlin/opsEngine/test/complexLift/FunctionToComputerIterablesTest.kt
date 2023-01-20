package opsEngine.test.complexLift

import Generator
import license
import dontEdit

object FunctionToComputerIterablesTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[FunctionToComputerIterablesTest.java]

    val inList get() = (1..arity).joinToString{ "in"}

    val expectedValue get() = "{" + arity * 2 + ", " + arity * 4 + '}'

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.complexLift;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.FunctionToComputerAdaptTestOps;
import org.scijava.ops.engine.adapt.functional.FunctionsToComputers;
import org.scijava.ops.engine.adapt.lift.ComputerToIterables;
import org.scijava.ops.engine.copy.CopyOpCollection;

public class FunctionToComputerIterablesTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		Object[] lifterOps = objsFromNoArgConstructors(FunctionsToComputersAndLift.class.getDeclaredClasses());
		ops.register(lifterOps);
		ops.register(new ComputerToIterables());
		Object[] adapterOps = objsFromNoArgConstructors(FunctionsToComputers.class.getDeclaredClasses());
		ops.register(adapterOps);
		ops.register(new CopyOpCollection());
		ops.register(new FunctionToComputerAdaptTestOps());
	}"""
        forEachArity {
            +"""

	@Test
	public void testFunction${arity}ToComputer$arity() {
		List<double[]> in = Arrays.asList(new double[] { 2, 4 });
		List<double[]> output = Arrays.asList(new double[] { 0, 0 });
		ops.op("test.FtC").input($inList).output(output).compute();
		Assertions.assertArrayEquals(new double[] $expectedValue, output.get(0), 0);
	}"""
        }
        +"""
}

"""
    }
}