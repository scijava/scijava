package opsEngine.test.functional

import Generator
import license
import dontEdit

object FunctionToComputerAdaptTestOps : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[FunctionToComputerAdaptTestOps.java]

    val doubleGenerics get() = '<' + (1..arity + 1).joinToString { "double[]" } + '>'

    val genericsNamesListWithoutOutput get() = genericsNamesList.take(arity)

    val expectedValue get() = "{" + arity * 2 + ", " + arity * 4 + '}'

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpCollection;

public class FunctionToComputerAdaptTestOps implements OpCollection {"""
        forEachArity {
            +"""

	@OpField(names = "test.FtC")
	public static final $functionArity$doubleGenerics toComp$arity = ($applyArgs) -> {
		double[] out = new double[in${if (arity == 1) "" else "1"}.length];
		for(int i = 0; i < in${if (arity == 1) "" else "1"}.length; i++) {"""
            for (input in genericsNamesListWithoutOutput)
                +"""
			out[i] += $input[i];"""

            +"""
		}
		return out;
	};"""
        }
        +"""
}
"""
    }
}