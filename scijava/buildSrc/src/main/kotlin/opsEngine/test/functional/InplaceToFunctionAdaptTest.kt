package opsEngine.test.functional

import Generator
import license
import dontEdit
import kotlin.math.pow

object InplaceToFunctionAdaptTest : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[InplaceToFunctionAdaptTest.java]

    fun functionInputName(num: Int) = if (arity == 1) "input" else "input$num"

    val inplaceExpectedArray get() = "{ " + (1..3).joinToString { "" + it.toFloat().pow(arity) } + " }"

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


package org.scijava.ops.engine.adapt.functional;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.OpBuilderTestOps;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.types.Nil;

public class InplaceToFunctionAdaptTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new OpBuilderTestOps());
		ops.register(new CreateOpCollection());
		ops.register(new CopyOpCollection());
		Object[] adapters = objsFromNoArgConstructors(InplacesToFunctions.class.getDeclaredClasses());
		ops.register(adapters);
	}

"""
        forEachArity {
            for (a in 1..arity) {
                +"""
	/** Matches a ${arity}-arity inplace${a} as a function${arity} */
	@Test
	public void testInplace${arity}_${a}ToFunction${arity}() {"""
                for(a in 1..arity)
                +"""
		final double[] ${functionInputName(a)} = { 1, 2, 3 };"""
                +"""
		double[] expected = $inplaceExpectedArray;
		final double[] output = ops.op("test.mulArrays${arity}_$a") //
			.input($args) //
			.outType(new Nil<double[]>()
			{}).apply();
		assertTrue(Arrays.equals(output, expected));
	}
"""
            }
        }
        +"""
	private OpBuilder name(String opName) {
		return ops.op(opName);
	}
}
"""
    }
}