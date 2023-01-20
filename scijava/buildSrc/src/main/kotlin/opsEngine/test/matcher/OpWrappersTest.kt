package opsEngine.test.matcher

import Generator
import license
import dontEdit

object OpWrappersTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    //    arities = (0..maxArity).collect()

    //    [OpWrappersTest.java]

    val doubleTypeParams get() = '<' + (0..arity).joinToString { "Double" } + '>'

    val doubleArrayTypeParams get() = '<' + (0..arity).joinToString { "double[]" } + '>'

    val nilDoubleList get() = (0..arity).joinToString { "nilDouble" }

    fun matchName(num: Int) = if (arity == 1) "matchInplace" else "matchInplace$num"

    override fun generate() {
        +"""
$dontEdit

package org.scijava.ops.engine.matcher;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.OpBuilderTestOps;
import org.scijava.types.GenericTyped;
import org.scijava.types.Nil;

/**
 * Tests whether the following Op types are wrapped by the Op matcher. We do
 * this by checking to make sure that they are a {@link GenericTyped}.
 * 
 * @author Gabriel Selzer
 */
public class OpWrappersTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new OpBuilderTestOps());
	}

	@Test
	public void testWrapProducer() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Producer<Double> op = OpBuilder.matchFunction(ops, "test.addDoubles", nilDouble);
		assertTrue(op instanceof GenericTyped);
	}
"""
        forEachArity(1..maxArity) {
            +"""
	@Test
	public void testWrapFunction$arity() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		$functionArity$doubleTypeParams op = //
			OpBuilder.matchFunction(ops, "test.addDoubles", //
				$nilDoubleList);
		assertTrue(op instanceof GenericTyped);
	}
"""
        }
        forEachArity(1..maxArity) {
            +"""
	@Test
	public void testWrapComputer$arity() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		$computerArity$doubleArrayTypeParams op = //
			OpBuilder.matchComputer(ops, "test.addArrays", $nilDoubleList);
		assertTrue(op instanceof GenericTyped);
	}
"""
        }
        forEachArity(1..maxArity) {
            for (a in 1..arity) {
                val inplaceSuffix = inplaceSuffix(a)
                val inplaceType = inplaceType(a)
                val matchName = matchName(a)
                arity--
                +"""
	@Test
	public void testWrapInplace$inplaceSuffix() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		$inplaceType$doubleArrayTypeParams op = //
			OpBuilder.$matchName(ops, "test.mulArrays${arity + 1}_$a", $nilDoubleList);
		assertTrue(op instanceof GenericTyped);
	}
"""
                arity++
            }
        }
        +"""
}
"""
    }
}