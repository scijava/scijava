package opsEngine.test

import Generator
import license
import joinToStringComma

object OpMethodTestOps : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list

    val argsList
        get() = when (arity) {
            0 -> emptyList()
            1 -> listOf("input")
            else -> (1..arity).map { "input$it" }
        }

    //[OpMethodTestOps.java]

    val opArgs get() = genericsNamesList.dropLast(1)

    val stringInputs get() = genericsNamesList.dropLast(1).joinToString { "String $it" }

    val addDoublesOutput
        get() = when (arity) {
            0 -> "0."
            else -> argsList.joinToString(" + ")
        }

    fun typeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun inplaceArgs(io: Int) = typeVarNums(io).map { if (it == 'O') "io" else "in$it" }

    fun inputOnlyInplaceArgs(io: Int) = typeVarNums(io).filter { it != 'O' }.map { "in$it" }

    fun doubleInputs(io: Int) = inplaceArgs(io).joinToString { "double[] $it" }

    fun inplaceArgsString(io: Int) = inplaceArgs(io).joinToString()

    override val arities get() = 1..maxArity

    override fun generate() {
        +"""
$license

package org.scijava.ops.engine;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.ops.spi.OpCollection;

public class OpMethodTestOps implements OpCollection {

	// -- Functions -- //
	@OpMethod(names = "test.multiplyNumericStrings", type = Producer.class)
	public static Integer multiplyNumericStringsProducer() {
		return Integer.valueOf(1);
	}"""
        forEachArity {
            +"""

	@OpMethod(names = "test.multiplyNumericStrings", type = $functionArity.class)
	public static Integer multiplyNumericStringsFunction$arity($stringInputs)
	{
		return multiplyNumericStringsFunction$arity($applyArgs, parseInt);
	}"""
        }
        +"""

	// -- Computers -- //
	
	@OpMethod(names = "test.doubleList", type = Computers.Arity0.class)
	public static void doublesToList0(List<Double> output) {
		output.clear();
	}"""
        forEachArity {
            +"""

	@OpMethod(names = "test.doubleList", type = $computerArity.class)
	public static void doublesToList$arity($stringInputs${if (arity > 0) "," else ""} List<Double> output) {
		doublesToListWithOp$arity($applyArgs, output, appendDouble);
	}"""
        }
        +"""

	// -- Inplaces -- //
"""
        forEachArity {
            for (a in 1..arity)
                +"""

	@OpMethod(names = "test.addDoubles${inplaceSuffix(a)}", type = ${inplaceType(a)}.class)
	public static void addDoubles${inplaceSuffix(a)}(${doubleInputs(a)}) {
		dependentAddDoubles${inplaceSuffix(a)}(${inplaceArgsString(a)}, addArrays);
	}"""
        }
        +"""

	// -- Helper Op -- //

	@OpField(names = "test.parseInt")
	public static final Function<String, Integer> parseInt = in -> Integer
		.parseInt(in);

	@OpField(names = "test.appendDouble")
	public static final Inplaces.Arity2_1<List<Double>, String> appendDouble = (
		list, element) -> list.add(Double.parseDouble(element));

	@OpField(names = "test.addArrays")
	public static final Inplaces.Arity2_1<double[], double[]> addArrays = (io,
		in2) -> {
		for (int i = 0; i < io.length; i++)
			io[i] += in2[i];
	};

	// -- Dependent Functions -- //"""
        forEachArity {
            +"""

	@OpMethod(names = "test.dependentMultiplyStrings", type = $functionArity.class)
	public static Integer multiplyNumericStringsFunction$arity($stringInputs,
		@OpDependency(name = "test.parseInt") Function<String, Integer> op)
	{
		Integer out = Integer.valueOf(1);
"""
            for (a in opArgs)
                +"""
		out *= op.apply($a);"""

            +"""

		return out;
	}"""
        }
        +"""

	// -- Dependent Computers -- //"""
        forEachArity {
            +"""

	@OpMethod(names = "test.dependentDoubleList", type = $computerArity.class)
	public static void doublesToListWithOp$arity($stringInputs,
		List<Double> output,
		@OpDependency(name = "test.appendDouble") Inplaces.Arity2_1<List<Double>, String> op)
	{
		output.clear();"""
            for (a in opArgs)
                +"""
		op.mutate(output, $a);"""

            +"""
	}"""
        }
        +"""

	// -- Dependent Inplaces -- //
"""
        forEachArity {
            for (a in 1..arity) {
                +"""

	@OpMethod(names = "test.dependentAddDoubles${inplaceSuffix(a)}", type = ${inplaceType(a)}.class)
	public static void dependentAddDoubles${inplaceSuffix(a)}(${doubleInputs(a)}, @OpDependency(name = "test.addArrays") Inplaces.Arity2_1<double[], double[]> op) {"""
                for (input in inputOnlyInplaceArgs(a))
                    +"""
			op.mutate(io, $input);"""

                +"""
	}"""
            }
        }
        +"""
}
"""
    }
}