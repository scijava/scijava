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

package net.imagej.ops.create;

import static org.junit.Assert.assertEquals;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imagej.ImgPlus;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.types.Nil;
import org.scijava.ops.function.Functions;

/**
 * Tests several ways to create an image
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */

public class CreateImgPlusTest extends AbstractTestEnvironment {

	@Test
	public void createImgPlusTest() {
		BiFunction<Dimensions, DoubleType, Img<DoubleType>> createImgFunc = Functions.match(ops, "create.img", new Nil<Dimensions>() {}, new Nil<DoubleType>() {}, new Nil<Img<DoubleType>>() {});
		Function<Img<DoubleType>, ImgPlus<DoubleType>> imgPlusFunction = Functions.match(ops, "create.imgPlus", new Nil<Img<DoubleType>>() {}, new Nil<ImgPlus<DoubleType>>() {});
		assertEquals(imgPlusFunction.apply(createImgFunc.apply(new FinalDimensions(new long[] { 10, 9, 8 }), new DoubleType())).getClass(), ImgPlus.class);
	}
}
