/*
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

package org.scijava.ops.image.morphology.thin;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.img.Img;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests for morphology thinning ops.
 *
 * @author Kyle Harrington
 */
public class ThinningTest extends AbstractOpTest {

	private Img<BitType> in;
	private Img<BitType> target;

	@BeforeEach
	public void initialize() {
		Img<FloatType> testImg = openRelativeFloatImg(AbstractOpTest.class,
			"features/3d_geometric_features_testlabel.tif");
		in = ops.op("create.img").input(testImg, new BitType()).outType(
			new Nil<Img<BitType>>()
			{}).apply();
		target = ops.op("create.img").input(in, new BitType()).outType(
			new Nil<Img<BitType>>()
			{}).apply();
		ops.op("convert.bit").input(testImg).output(in).compute();

	}

	@Test
	public void testThinGuoHall() {
		final Img<BitType> out = ops.op("morphology.thinGuoHall").input(in).outType(
			new Nil<Img<BitType>>()
			{}).apply();
		ops.op("convert.bit").input(openRelativeFloatImg(AbstractThin.class,
			"result_guoHall.tif")).output(target).compute();
		ImgLib2Assert.assertImageEquals(target, out);
	}

	@Test
	public void testThinHilditch() {
		final Img<BitType> out = ops.op("morphology.thinHilditch").input(in)
			.outType(new Nil<Img<BitType>>()
			{}).apply();
		ops.op("convert.bit").input(openRelativeFloatImg(AbstractThin.class,
			"result_hilditch.tif")).output(target).compute();
		ImgLib2Assert.assertImageEquals(target, out);
	}

	@Test
	public void testMorphological() {
		final Img<BitType> out = ops.op("morphology.thinMorphological").input(in)
			.outType(new Nil<Img<BitType>>()
			{}).apply();
		ops.op("convert.bit").input(openRelativeFloatImg(AbstractThin.class,
			"result_morphological.tif")).output(target).compute();
		ImgLib2Assert.assertImageEquals(target, out);
	}

	@Test
	public void testZhangSuen() {
		final Img<BitType> out = ops.op("morphology.thinZhangSuen").input(in)
			.outType(new Nil<Img<BitType>>()
			{}).apply();
		ops.op("convert.bit").input(openRelativeFloatImg(AbstractThin.class,
			"result_zhangSuen.tif")).output(target).compute();
		ImgLib2Assert.assertImageEquals(target, out);
	}
}
