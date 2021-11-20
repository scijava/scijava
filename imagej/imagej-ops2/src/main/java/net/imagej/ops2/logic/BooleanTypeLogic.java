/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.logic;

import net.imglib2.type.BooleanType;

import org.scijava.function.Computers;
import org.scijava.ops.OpCollection;
import org.scijava.ops.OpField;
import org.scijava.plugin.Plugin;

/**
 * Logic operations with {@link BooleanType} as output.
 * 
 * @author Leon Yang
 */
@Plugin(type = OpCollection.class)
public class BooleanTypeLogic<B extends BooleanType<B>, C extends Comparable<C>> {

	/** Performs logical and ({@literal &&}) between two {@link BooleanType}s. */
	@OpField(names = "logic.and", params = "input1, input2, output")
	public final Computers.Arity2<B, B, B> ander = (in1, in2, out) -> {
		out.set(in1);
		out.and(in2);
	};

	@OpField(names = "logic.greaterThan", params = "input1, input2, output")
	public final Computers.Arity2<C, C, B> greaterThan = (in1, in2, out) -> out.set(in1.compareTo(in2) > 0);

	@OpField(names = "logic.greaterThanOrEqual", params = "input1, input2, output")
	public final Computers.Arity2<C, C, B> greaterThanOrEqual = (in1, in2, out) -> out.set(in1.compareTo(in2) >= 0);

	@OpField(names = "logic.lessThan", params = "input1, input2, output")
	public final Computers.Arity2<C, C, B> lessThan = (in1, in2, out) -> out.set(in1.compareTo(in2) < 0);


	@OpField(names = "logic.lessThanOrEqual", params = "input1, input2, output")
	public final Computers.Arity2<C, C, B> lessThanOrEqual = (in1, in2, out) -> out.set(in1.compareTo(in2) <= 0);

	@OpField(names = "logic.not", params = "input, output")
	public final Computers.Arity1<B, B> not = (in, out) -> {
		out.set(in);
		out.not();
	};

	@OpField(names = "logic.equal", params = "input1, input2, output")
	public final Computers.Arity2<C, C, B> equals = (in1, in2, out) -> out.set(in1.equals(in2));

	@OpField(names = "logic.notEqual", params = "input1, input2, output")
	public final Computers.Arity2<C, C, B> notEquals = (in1, in2, out) -> out.set(!in1.equals(in2));

	@OpField(names = "logic.or", params = "input1, input2, output")
	public final Computers.Arity2<B, B, B> or = (in1, in2, out) -> {
		out.set(in1);
		out.or(in2);
	};

	@OpField(names = "logic.xor", params = "input1, input2, output")
	public final Computers.Arity2<B, B, B> xor = (in1, in2, out) -> {
		out.set(in1);
		out.xor(in2);
	};

}
