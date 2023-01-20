package opsEngine.main.lift

import Generator
import license
import dontEdit

object FunctionToArrays : Generator(){

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[FunctionToArrays.java]

    val arrayGenerics get() = '<' + genericParamTypes.joinToString{"$it[]"} + '>'

    val classGenerics get() = "<I, " + genericParamTypes.joinToString() + '>'

    val arraysAtPosI get() = when(arity) {
        0 -> ""
        1 -> "in[i]"
        else -> (1..arity).joinToString{ "in$it[i]" }
    }

    val arraysAtPos0 get() = when(arity) {
        0 -> ""
        1 -> "in[0]"
        else -> (1..arity).joinToString{ "in$it[0]" }
    }

    override fun generate() {
        arity = maxArity
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.lift;

import java.lang.reflect.Array;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpCollection;

/**
 * Converts {@link Functions} operating on single types to {@link Functions}
 * that operate on arrays of types. N.B. it is the user's responsibility to pass
 * arrays of the same length (otherwise the Op will stop when one of the arrays
 * runs out of {@link Object}s).
 * 
 * @author Gabriel Selzer
 */
public class FunctionToArrays$classGenerics implements OpCollection{

	// TODO: extract logic to a utility class
	private int minLength(Object[]... arrays) {
		int minLength = Integer.MAX_VALUE;
		for (Object[] array : arrays)
			if (array.length < minLength) minLength = array.length;
		return minLength;
	}
	
	// NOTE: we cannot convert Producers since there is no way to determine the
	// length of the output array
"""
forEachArity {
    +"""
	@OpField(names = "adapt", params = "fromOp, toOp")
	public final Function<$functionArity$generics, $functionArity$arrayGenerics> liftFunction$arity =
		(function) -> {
			return ($applyArgs) -> {
				int len = minLength($applyArgs);
				if (len == 0) throw new UnsupportedOperationException("Unable to create an empty output array.");
				O component = function.apply($arraysAtPos0);
				@SuppressWarnings("unchecked")
				O[] out = (O[]) Array.newInstance(component.getClass(), len);
				
				for (int i = 0; i < len; i++) {
					out[i] = function.apply($arraysAtPosI);
				}
				return out;
			};
		};
"""
}
+"""
}
"""
    }
}