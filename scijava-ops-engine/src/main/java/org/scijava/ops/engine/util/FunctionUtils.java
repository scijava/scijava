/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
/*
 * This is autogenerated source code -- DO NOT EDIT. Instead, edit the
 * corresponding template in templates/ and rerun bin/generate.groovy.
 */

package org.scijava.ops.engine.util;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Functions;
import org.scijava.function.Producer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.types.Nil;
import org.scijava.common3.Types;

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
		}
		else if (op instanceof Function) {
			return Functions.nary((Function<Object, O>) op);
		}
		else if (op instanceof BiFunction) {
			return Functions.nary((BiFunction<Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity3) {
			return Functions.nary((Functions.Arity3<Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity4) {
			return Functions.nary(
				(Functions.Arity4<Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity5) {
			return Functions.nary(
				(Functions.Arity5<Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity6) {
			return Functions.nary(
				(Functions.Arity6<Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity7) {
			return Functions.nary(
				(Functions.Arity7<Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity8) {
			return Functions.nary(
				(Functions.Arity8<Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity9) {
			return Functions.nary(
				(Functions.Arity9<Object, Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity10) {
			return Functions.nary(
				(Functions.Arity10<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity11) {
			return Functions.nary(
				(Functions.Arity11<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity12) {
			return Functions.nary(
				(Functions.Arity12<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity13) {
			return Functions.nary(
				(Functions.Arity13<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity14) {
			return Functions.nary(
				(Functions.Arity14<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		else if (op instanceof Functions.Arity15) {
			return Functions.nary(
				(Functions.Arity15<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
		}
		return Functions.nary(
			(Functions.Arity16<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, O>) op);
	}

	@SuppressWarnings({ "unchecked" })
	private static <T> T matchHelper(final OpEnvironment env, final String opName,
		final Class<T> opClass, final Nil<?> outType, final Nil<?>... inTypes)
	{
		final Type[] types = new Type[inTypes.length + 1];
		for (int i = 0; i < inTypes.length; i++)
			types[i] = inTypes[i].type();
		types[types.length - 1] = outType.type();
		final Type specialType = Types.parameterize(opClass, types);
		return (T) env.op(opName, Nil.of(specialType), inTypes, outType);
	}
}
