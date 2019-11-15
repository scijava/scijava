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

package net.imagej.ops.logic;

import net.imglib2.type.BooleanType;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Logic operations with {@link BooleanType} as output.
 * 
 * @author Leon Yang
 */
@Plugin(type = OpCollection.class)
public class BooleanTypeLogic<B extends BooleanType<B>, C extends Comparable<C>> {

	/** Performs logical and ({@literal &&}) between two {@link BooleanType}s. */
	@OpField(names = "logic.and")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<B, B, B> ander = (in1, in2, out) -> {
		out.set(in1);
		out.and(in2);
	};

	@OpField(names = "logic.greaterThan")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<C, C, B> greaterThan = (in1, in2, out) -> out.set(in1.compareTo(in2) > 0);

	@OpField(names = "logic.greaterThanOrEqual")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<C, C, B> greaterThanOrEqual = (in1, in2, out) -> out.set(in1.compareTo(in2) >= 0);

	@OpField(names = "logic.lessThan")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<C, C, B> lessThan = (in1, in2, out) -> out.set(in1.compareTo(in2) < 0);


	@OpField(names = "logic.lessThanOrEqual")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<C, C, B> lessThanOrEqual = (in1, in2, out) -> out.set(in1.compareTo(in2) <= 0);

	@OpField(names = "logic.not")
	@Parameter(key = "input")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity1<B, B> not = (in, out) -> {
		out.set(in);
		out.not();
	};

	@OpField(names = "logic.equal")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<C, C, B> equals = (in1, in2, out) -> out.set(in1.equals(in2));

	@OpField(names = "logic.notEqual")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<C, C, B> notEquals = (in1, in2, out) -> out.set(!in1.equals(in2));

	@OpField(names = "logic.or")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<B, B, B> or = (in1, in2, out) -> {
		out.set(in1);
		out.or(in2);
	};

	@OpField(names = "logic.xor")
	@Parameter(key = "input1")
	@Parameter(key = "input2")
	@Parameter(key = "output", itemIO = ItemIO.BOTH)
	public final Computers.Arity2<B, B, B> xor = (in1, in2, out) -> {
		out.set(in1);
		out.xor(in2);
	};

}
