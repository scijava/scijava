package opsEngine.main.util

import Generator
import license
import dontEdit
import joinToStringComma

object FunctionUtils : Generator() {

    //.include Globals.list
    override val arities get() = 3..maxArity

    //[FunctionUtils.java]
    val genericsO2 get() = '<' + (1..arity).joinToStringComma { "I$it" } + "O2>"

    val matchParams: String
        get() {
            val generics = genericParamTypes
            val names = genericsNamesList
            return (0..arity).joinToString { "final Nil<${generics[it]}> ${names[it]}Type" }
        }

    val nilArgs: String
        get() {
            val names = genericsNamesList as ArrayList
            val out = names.last()
            names.add(0, out)
            return names.dropLast(1).joinToString { "${it}Type" }
        }

    val objectGenerics get() = '<' + (1..arity).joinToString { "Object" } + ", O>"

    val insArgs get() = (0 until arity).joinToString { "ins[$it]" }

    override fun generate() {
        +"""
$dontEdit

package org.scijava.ops.engine.util;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Functions;
import org.scijava.function.Producer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.Hints;
import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * Utility class designed to match {@code Function}s of various arities.
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class FunctionUtils {

	private FunctionUtils() {
		// NB: Prevent instantiation of utility class.
	}

	@SuppressWarnings({ "unchecked" })
	public static <O, T> Functions.ArityN<O> matchN(final OpEnvironment env,
		final String opName, final Nil<O> outType, final Nil<?>... inTypes)
	{
		Map.Entry<Integer, Class<?>> c = Functions.ALL_FUNCTIONS //
			.entrySet().stream() //
			.filter(e -> e.getKey() == inTypes.length) //
			.findAny().get();
		Object op = matchHelper(env, opName, c.getValue(), outType, inTypes);
		if (op instanceof Producer) {
			return Functions.nary((Producer<O>) op);
		}"""
        forEachArity(1 until maxArity) {
            +"""
		else if (op instanceof $functionArity) {
			return Functions.nary(($functionArity$objectGenerics) op);
		}"""
        }
        arity = maxArity
        +"""
		return Functions.nary(($functionArity$objectGenerics) op);
	}

	@SuppressWarnings({ "unchecked" })
	private static <T> T matchHelper(final OpEnvironment env, final String opName,
		final Class<T> opClass, final Nil<?> outType, final Nil<?>... inTypes)
	{
		final Type[] types = new Type[inTypes.length + 1];
		for (int i = 0; i < inTypes.length; i++)
			types[i] = inTypes[i].getType();
		types[types.length - 1] = outType.getType();
		final Type specialType = Types.parameterize(opClass, types);
		return (T) env.op(opName, Nil.of(specialType), inTypes, outType);
	}
}
"""
    }
}