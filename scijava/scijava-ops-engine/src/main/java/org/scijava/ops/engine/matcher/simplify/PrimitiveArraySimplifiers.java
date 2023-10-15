/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.matcher.simplify;

import java.util.function.Function;

import org.scijava.collections.ObjectArray;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * A collection of Ops for simplifying and focusing primitive arrays
 *
 * @author Gabriel Selzer
 */
public class PrimitiveArraySimplifiers implements OpCollection {

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Byte[], ObjectArray<Number>> byteArrSimplifier =
		b -> new ObjectArray<>(b);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Byte[]> byteArrFocuser = o -> o
		.stream().map(b -> b == null ? null : b.byteValue()).toArray(Byte[]::new);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Integer[], ObjectArray<Number>> intArrSimplifier =
		i -> new ObjectArray<>(i);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Integer[]> intArrFocuser = o -> o
		.stream().map(i -> i == null ? null : i.intValue()).toArray(Integer[]::new);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Short[], ObjectArray<Number>> shortArrSimplifier =
		s -> new ObjectArray<>(s);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Short[]> shortArrFocuser = o -> o
		.stream().map(s -> s == null ? null : s.shortValue()).toArray(Short[]::new);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Long[], ObjectArray<Number>> longArrSimplifier =
		l -> new ObjectArray<>(l);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Long[]> longArrFocuser = o -> o
		.stream().map(l -> l == null ? null : l.longValue()).toArray(Long[]::new);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Float[], ObjectArray<Number>> floatArrSimplifier =
		f -> new ObjectArray<>(f);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Float[]> floatArrFocuser = o -> o
		.stream().map(f -> f == null ? null : f.floatValue()).toArray(Float[]::new);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Double[], ObjectArray<Number>> doubleArrSimplifier =
		d -> new ObjectArray<>(d);

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Double[]> doubleArrFocuser = o -> o
		.stream().map(d -> d == null ? null : d.doubleValue()).toArray(Double[]::new);
}
