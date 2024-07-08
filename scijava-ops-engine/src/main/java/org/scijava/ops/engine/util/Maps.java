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

package org.scijava.ops.engine.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.function.Computers;

public final class Maps {

	private Maps() {
		// NB: Prevent instantiation of utility class.
	}

	public interface FunctionMaps {

		public interface Iterables {

			public static <I, O> Function<Iterable<I>, Iterable<O>> liftBoth(
				final Function<I, O> function)
			{
				return iter -> Streams.stream(iter).map(function).collect(Collectors
					.toList());
			}

			public static <I, O> Function<Iterable<I>, Iterable<O>> bothFlat(
				final Function<I, Iterable<O>> function)
			{
				return iter -> Streams.stream(iter).flatMap(i -> Streams.stream(function
					.apply(i))).collect(Collectors.toList());
			}
		}

		public interface Arrays {

			@SuppressWarnings("unchecked")
			public static <I, O> Function<I[], O[]> liftBoth(
				final Function<I, O> function, Class<O> cls)
			{
				return is -> java.util.Arrays.stream(is).map(function).toArray(
					size -> (O[]) Array.newInstance(cls, size));
			}
		}

		public interface Lists {

			public static <I, O> Function<List<I>, List<O>> liftBoth(
				final Function<I, O> function)
			{
				return iter -> iter.stream().map(function).collect(Collectors.toList());
			}
		}
	}

	public interface ComputerMaps {

		public interface Iterables {

			public static <I, O> Computers.Arity1<Iterable<I>, Iterable<O>> liftBoth(
				final Computers.Arity1<I, O> computer)
			{
				return (iter1, iter2) -> {
                    var i1 = iter1.iterator();
                    var i2 = iter2.iterator();
					while (i1.hasNext() && i2.hasNext()) {
						computer.compute(i1.next(), i2.next());
					}
				};
			}
		}
	}
}
