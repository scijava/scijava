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

package net.imagej.ops.math;

import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.NullaryComputer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Nullary Ops of the {@code math} namespace which operate on
 * {@link NumericType}s.
 * 
 * @author Leon Yang
 */
@Plugin(type = OpCollection.class)
public class NullaryNumericTypeMath <T extends Type<T>, N extends NumericType<N>>{

	private NullaryNumericTypeMath() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Sets the output to a constant.
	 */
	@OpField(names = "math.assign")
	@Parameter(key = "constant")
	@Parameter(key = "output", type = ItemIO.BOTH)
	public final Computer<T, T> assigner = (constant, output) -> output.set(constant);

	/**
	 * Sets the output to zero.
	 */
	@OpField(names = "math.zero")
	@Parameter(key = "output", type = ItemIO.BOTH)
	public final NullaryComputer<N> zeroer = (output) -> output.setZero();

}
