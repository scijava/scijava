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

package net.imagej.ops.morphology.thin;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.features.AbstractFeatureTest;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link net.imagej.ops.Ops.Morphology} thinning ops.
 *
 * @author Kyle Harrington
 */
public class ThinningTest extends AbstractOpTest {

	private Img<BitType> in;
	private Img<BitType> target;

	@Before
	public void initialize() {
		Img<FloatType> testImg = openFloatImg(AbstractFeatureTest.class, "3d_geometric_features_testlabel.tif");
		in = (Img<BitType>) new OpBuilder(ops, "create.img").input(testImg, new BitType()).apply();
		target = (Img<BitType>) new OpBuilder(ops, "create.img").input(in, new BitType()).apply();
		new OpBuilder(ops, "convert.bit").input(testImg, in).apply();

	}

	@Test
	public void testThinGuoHall() {
		@SuppressWarnings("unchecked")
		final Img<BitType> out = (Img<BitType>) new OpBuilder(ops, "morphology.thinGuoHall").input(in).apply();
		new OpBuilder(ops, "convert.bit").input(openFloatImg(AbstractThin.class, "result_guoHall.tif"), target).apply();
		assertIterationsEqual(target, out);
	}

	@Test
	public void testThinHilditch() {
		@SuppressWarnings("unchecked")
		final Img<BitType> out = (Img<BitType>) new OpBuilder(ops, "morphology.thinHilditch").input(in).apply();
		new OpBuilder(ops, "convert.bit").input(openFloatImg(AbstractThin.class, "result_hilditch.tif"), target).apply();
		assertIterationsEqual(target, out);
	}

	@Test
	public void testMorphological() {
		@SuppressWarnings("unchecked")
		final Img<BitType> out = (Img<BitType>) new OpBuilder(ops, "morphology.thinMorphological").input(in).apply();
		new OpBuilder(ops, "convert.bit").input(openFloatImg(AbstractThin.class, "result_morphological.tif"), target).apply();
		assertIterationsEqual(target, out);
	}

	@Test
	public void testZhangSuen() {
		@SuppressWarnings("unchecked")
		final Img<BitType> out = (Img<BitType>) new OpBuilder(ops, "morphology.thinZhangSuen").input(in).apply();
		new OpBuilder(ops, "convert.bit").input(openFloatImg(AbstractThin.class, "result_zhangSuen.tif"), target).apply();
		assertIterationsEqual(target, out);
	}
}
