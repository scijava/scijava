package opsEngine.test.functional

import Generator
import license
import dontEdit

object FunctionToComputerAdaptTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[FunctionToComputerAdaptTest.java]

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
import org.scijava.ops.engine.copy.CopyOpCollection;

public class FunctionToComputerAdaptTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new FunctionToComputerAdaptTestOps());
		ops.register(new CopyOpCollection());
		ops.register(objsFromNoArgConstructors(FunctionsToComputers.class.getDeclaredClasses()));
	}"""
        forEachArity {
            +"""

	@Test
	public void testFunction${arity}ToComputer$arity() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input($inList).output(output).compute();
		Assertions.assertArrayEquals(new double[] $expectedValue, output, 0);
	}"""
        }
        +"""
}

"""
    }
}