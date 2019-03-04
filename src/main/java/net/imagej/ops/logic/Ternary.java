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
import net.imglib2.type.Type;

import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * An Op that works the same way as the <code>?:</code> operator. It sets the
 * output to the <code>ifTrueVal</code> if the input is evaluated as true, or
 * <code>ifFalseVal</code> otherwise.
 * 
 * @author Leon Yang
 */
@Plugin(type = Op.class, name = "logic.ternary")
@Parameter(key = "input")
@Parameter(key = "ifTrueVal")
@Parameter(key = "ifFalseVal")
@Parameter(key = "output", type = ItemIO.BOTH)
public class Ternary<I extends BooleanType<I>, O extends Type<O>> implements Computer3<I, O, O, O>

{

	@Override
	public void compute(final I input, final O ifTrueVal, final O ifFalseVal, final O output) {
		output.set(input.get() ? ifTrueVal : ifFalseVal);
	}

}