package opsEngine.test.functional

import Generator
import license
import dontEdit

object ComputerToFunctionAdaptTestOps : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[ComputerToFunctionAdaptTestOps.java]

    val doubleGenerics get() = '<' + (1..arity + 1).joinToString { "double[]" } + '>'

    val genericsNamesListWithoutOutput get() = genericsNamesList.take(arity - 1)

    val expectedValue get() = "{" + arity * 2 + ", " + arity * 4 + '}'

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.functional;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpCollection;

public class ComputerToFunctionAdaptTestOps implements OpCollection {"""
        forEachArity {
            +"""
	
	@OpField(names = "test.CtF")
	public static final $computerArity$doubleGenerics toFunc$arity = ($computeArgs) -> {
		for(int i = 0; i < in${if (arity == 1) "" else "1"}.length; i++) {
			out[i] = 0.0;"""
            for (input in genericsNamesListWithoutOutput)
                +"""
			out[i] += $input[i];"""

            +"""
		}
	};"""
        }
        +"""
}
"""
    }
}