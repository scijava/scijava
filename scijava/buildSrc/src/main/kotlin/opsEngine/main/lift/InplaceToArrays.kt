package opsEngine.main.lift

import Generator
import license
import dontEdit

object InplaceToArrays : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[InplaceToArrays.java]
    // TODO move into Generator
    fun typeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun genericsList(io: Int) = typeVarNums(io).map { "I$it" }

    fun generics(io: Int) = '<' + genericsList(io).joinToString() + '>'

    fun arrayGenerics(io: Int) = '<' + genericsList(io).joinToString { "$it[]" } + '>'

    val classGenerics get() = "<IO, " + genericParamTypes.dropLast(1).joinToString() + '>'

    fun arraysAtPosI(io: Int) = when (arity) {
        1 -> "io[i]"
        else -> genericsList(io).joinToString { if (it.substring(1) == "O") "io[i]" else "in${it.substring(1)}[i]" }
    }

    fun mutateArgs(io: Int) = genericsList(io).joinToString { if (it.substring(1) == "O") "IO io" else "$it in${it.substring(1)}" }

    fun mutateParams(io: Int) = genericsList(io).joinToString { if (it.substring(1) == "O") "io" else "in${it.substring(1)}" }

    override fun generate() {
        arity = maxArity
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.lift;

import java.util.function.Function;

import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpCollection;

/**
 * Collection of ops designed to lift {@link Inplaces} to operate
 * on arrays. TODO: multi-threading support
 * 
 * @author Gabriel Selzer
 */
public class InplaceToArrays$classGenerics implements OpCollection {

	private int minLength(Object[]... arrays) {
		int minLength = Integer.MAX_VALUE;
		for (Object[] array : arrays)
			if (array.length < minLength) minLength = array.length;
		return minLength;
	}
"""
        forEachArity {
            for(a in 1..arity)
            +"""
	@OpField(names = "adapt", params = "fromOp, toOp")
	public final Function<${inplaceType(a)}${generics(a)}, ${inplaceType(a)}${arrayGenerics(a)}> liftInplace${inplaceSuffix(a)} =
		(inplace) -> {
			return (${mutateParams(a)}) -> {
				int max = minLength(${mutateParams(a)});
				for (int i = 0; i < max; i++) {
					inplace.mutate(${arraysAtPosI(a)});
				}
			};
		};
"""
        }
        +"""
}
"""
    }
}