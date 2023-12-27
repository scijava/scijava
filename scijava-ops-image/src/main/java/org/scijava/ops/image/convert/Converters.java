/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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
package org.scijava.ops.image.convert;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.type.Type;

import org.scijava.function.Functions;

public class Converters<I, O extends Type<O>> {

	/**
	 * @input inputRAI
	 * @input converter
	 * @input outputType
	 * @output outputII
	 * @implNote op names='convert'
	 */
	public final Functions.Arity3<RandomAccessible<I>, Converter<? super I, ? super O>, O, RandomAccessible<O>> generalConverterRA =
			net.imglib2.converter.Converters::convert;

	/**
	 * @input inputII
	 * @input converter
	 * @input outputType
	 * @output outputII
	 * @implNote op names='convert'
	 */
	public final Functions.Arity3<IterableInterval<I>, Converter<? super I, ? super O>, O, IterableInterval<O>> generalConverterII =
			net.imglib2.converter.Converters::convert;

}
