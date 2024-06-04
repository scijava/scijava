/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.describe;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import org.scijava.types.Nil;

import java.util.Random;
import java.util.function.Function;

/**
 * {@code engine.describe} Ops pertaining to ImgLib2 types.
 * <p>
 * Note the heavy use of wildcards which provides extensibility, as it allows
 * e.g. {@link #raiDesc(Nil)} to be used for e.g.
 * {@link net.imglib2.img.array.ArrayImg}s
 * </p>
 *
 * @author Gabriel Selzer
 */
public class ImgLib2Descriptors {

	/**
	 * @param inType the type (some {@link IterableInterval}) subclass to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='-100.'
	 */
	public static String iiDesc( //
		Nil<? extends IterableInterval<?>> inType //
	) {
		return "image";
	}

	/**
	 * @param inType the type (some {@link RandomAccessibleInterval} subclass) to
	 *          describe
	 * @return the description
	 * @implNote op name="engine.describe"
	 */
	public static String raiDesc( //
		Nil<? extends RandomAccessibleInterval<?>> inType //
	) {
		return "image";
	}

	/**
	 * @param inType the type (some {@link ImgLabeling} subclass) to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='100.'
	 */
	public static String labelDesc(Nil<? extends ImgLabeling<?, ?>> inType) {
		return "labeling";
	}

	/**
	 * @param inType the type (some {@link RealType} subclass) to describe
	 * @return the description
	 * @implNote op name="engine.describe"
	 */
	public static String realTypeDesc( //
		Nil<? extends RealType<?>> inType //
	) {
		return "number";
	}

	/**
	 * @param inType the type (some {@link ComplexType} subclass) to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='-100.'
	 */
	public static String complexTypeDesc( //
		Nil<? extends ComplexType<?>> inType //
	) {
		return "complex-number";
	}
}
