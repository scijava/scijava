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

import org.scijava.ops.spi.OpHints;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * A set of {@code simplify} Ops dealing with boxed primitive types.
 * 
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class PrimitiveSimplifiers implements OpCollection {

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.simplify")
	public final Function<Byte, Number> byteSimplifier = b -> b;
	
	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.focus")
	public final Function<Number, Byte> numberByteFocuser = n -> n.byteValue();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.simplify")
	public final Function<Integer, Number> integerSimplifier = i -> i;
	
	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.focus")
	public final Function<Number, Integer> numberIntegerFocuser = n -> n.intValue();
	
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.simplify")
	public final Function<Short, Number> shortSimplifier = s -> s;
	
	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.focus")
	public final Function<Number, Short> numberShortFocuser = n -> n.shortValue();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.simplify")
	public final Function<Long, Number> longSimplifier = l -> l;

	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.focus")
	public final Function<Number, Long> numberLongFocuser = n -> n.longValue();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.simplify")
	public final Function<Float, Number> floatSimplifier = f -> f;

	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.focus")
	public final Function<Number, Float> numberFloatFocuser = n -> n.floatValue();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.simplify")
	public final Function<Double, Number> doubleSimplifier = d -> d;

	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "engine.focus")
	public final Function<Number, Double> numberDoubleFocuser = n -> n.doubleValue();

}
