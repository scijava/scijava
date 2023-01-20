package opsEngine.test.functional

import Generator
import license
import dontEdit

object ComputerToFunctionAdaptTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[ComputerToFunctionAdaptTest.java]

    val inList get() = (1..arity).joinToString { "in" }

    val expectedValue get() = "{" + arity * 2 + ", " + arity * 4 + '}'

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.functional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.create.CreateOpCollection;

public class ComputerToFunctionAdaptTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new ComputerToFunctionAdaptTestOps());
		ops.register(new CreateOpCollection());
		Object[] objects = objsFromNoArgConstructors(ComputersToFunctionsViaFunction.class.getDeclaredClasses());
		ops.register(objects);
	}"""
        forEachArity {
            +"""

	@Test
	public void testComputer${arity}ToFunction$arity() {
		double[] in = {2, 4};
		double[] output = ops.op("test.CtF").input($inList).outType(double[].class).apply();
		double[] expected = $expectedValue; 
		Assertions.assertArrayEquals(expected, output, 0);
	}"""
        }
        +"""
}

"""
    }
}