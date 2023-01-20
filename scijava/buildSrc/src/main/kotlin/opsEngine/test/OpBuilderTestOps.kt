package opsEngine.test

import license

object OpBuilderTestOps : OpBuilderTest() {

    //.include OpBuilderTest.list

    val argsList
        get() = when (arity) {
            0 -> emptyList()
            1 -> listOf("input")
            else -> (1..arity).map { "input$it" }
        }

    //[OpBuilderTestOps.java]

    val addDoublesOutput
        get() = when (arity) {
            0 -> "0."
            else -> argsList.joinToString(" + ")
        }

    fun typeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun inplaceArgs(io: Int) = typeVarNums(io).joinToString { if (it == 'O') "io" else "in$it" }

    fun inputOnlyInplaceArgs(io: Int) = typeVarNums(io).filter { it != 'O' }.map { "in$it" }

    val computerArgsList
        get() = when (arity) {
            0 -> listOf("output")
            else -> argsList + "output"
        }

    val computerArgs get() = computerArgsList.joinToString()

    override fun generate() {
        +"""
$license

package org.scijava.ops.engine;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpCollection;

/**
 * Helper ops for {@link OpBuilderTest}.
 *
 * @author Curtis Rueden
 */
public class OpBuilderTestOps implements OpCollection {

	/*
	 * -- FUNCTIONS --
	 * 
	 * The general procedure for these Ops: Given a set of inputs in1, in2, ...,
	 * inN, The output will be sum(in1, in2, ..., inN).
	 */
"""
        forEachArity {
            +"""
	// ARITY $arity
	@OpField(names = "test.addDoubles")
	public final $functionArity$genericDoubles addDoubles$arity = //
		($args) -> $addDoublesOutput;
"""
        }
        +"""

	/*
	 * -- INPLACES --
	 * 
	 * The general procedure for these Ops: Given a set of inputs in1, in2, ... ,
	 * io, ..., inN, the output will be io = in1 * in2 * ... * io * ... * inN.
	 * N.B. We do this in arrays since the doubles themselves are immutable. 
	 */"""
        forEachArity(1..maxArity) {
            for (a in 1..arity) {
                +"""
	// ARITY $arity
	@OpField(names = "test.mulArrays${arity}_$a")
	public final ${inplaceType(a)}$arrayGenerics powDoubles${arity}_$a = //
		(${inplaceArgs(a)}) -> {
			for (int i = 0; i < io.length; i++) { //"""
                for (i in inputOnlyInplaceArgs(a))
                    +"""
				io[i] *= $i[i];"""

                +"""
			}
		};
"""
            }
        }
        +"""

	/*
	 * -- COMPUTERS --
	 * 
	 * The general procedure: given a set of inputs in1, in2, ... , inN, the output
	 * is given as in1 * in2 * ... * inN. N.B. we use arrays here since the doubles
	 * themselves are immutable
	 */
"""
        forEachArity {
            +"""
	// ARITY $arity
	@OpField(names = "test.addArrays")
	public final $computerArity$computerArrayGenerics addArrays$arity = //
		($computerArgs) -> {
			for (int i = 0; i < output.length; i++) {
				output[i] = 0;"""
            for (i in argsList)
                +"""
				output[i] += ${i}[i];"""

            +"""
			}
		};
"""
        }
        +"""
}
"""
    }
}