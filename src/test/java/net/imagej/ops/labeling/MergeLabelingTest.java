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

package net.imagej.ops.labeling;

import static org.junit.Assert.assertTrue;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.ByteType;

import org.junit.Before;
import org.junit.Test;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.ops.types.Nil;

/**
 * Tests {@link MergeLabeling}.
 *
 * @author Stefan Helfrich (University of Konstanz)
 */
public class MergeLabelingTest extends AbstractOpTest {

	private ImgLabeling<Integer, ByteType> in1;
	private ImgLabeling<Integer, ByteType> in2;
	private ImgLabeling<Integer, ByteType> out;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		in1 = op("create.imgLabeling").input(new FinalInterval(2, 2), new ByteType())
				.outType(new Nil<ImgLabeling<Integer, ByteType>>() {}).apply();
		RandomAccess<LabelingType<Integer>> randomAccess = in1.randomAccess();
		randomAccess.setPosition(new int[] { 0, 0 });
		randomAccess.get().add(0);
		randomAccess.setPosition(new int[] { 0, 1 });
		randomAccess.get().add(1);
		randomAccess.setPosition(new int[] { 1, 0 });
		randomAccess.get().add(2);
		randomAccess.setPosition(new int[] { 1, 1 });
		randomAccess.get().add(3);

		in2 = op("create.imgLabeling").input(new FinalInterval(2, 2), new ByteType())
				.outType(new Nil<ImgLabeling<Integer, ByteType>>() {}).apply();
		randomAccess = in2.randomAccess();
		randomAccess.setPosition(new int[] { 0, 0 });
		randomAccess.get().add(10);
		randomAccess.setPosition(new int[] { 0, 1 });
		randomAccess.get().add(11);
		randomAccess.setPosition(new int[] { 1, 0 });
		randomAccess.get().add(12);
		randomAccess.setPosition(new int[] { 1, 1 });
		randomAccess.get().add(13);

		out = op("create.imgLabeling").input(new FinalInterval(2, 2), new ByteType())
				.outType(new Nil<ImgLabeling<Integer, ByteType>>() {}).apply();
	}

	@Test
	public void testMerging() {
		final ImgLabeling<Integer, ByteType> run = op("labeling.merge").input(in1, in2)
				.outType(new Nil<ImgLabeling<Integer, ByteType>>() {}).apply();
		assertTrue(run.firstElement().contains(0));
		assertTrue(run.firstElement().contains(10));
		assertTrue(!run.firstElement().contains(3));
	}

	@Test
	public void testMask() {
		final Img<BitType> mask = op("create.img").input(in1, new BitType())
				.outType(new Nil<Img<BitType>>() {}).apply();
		final RandomAccess<BitType> maskRA = mask.randomAccess();
		maskRA.setPosition(new int[] { 0, 0 });
		maskRA.get().set(true);
		maskRA.setPosition(new int[] { 1, 1 });
		maskRA.get().set(true);
		out = op("labeling.merge").input(in1, in2, mask)
				.outType(new Nil<ImgLabeling<Integer, ByteType>>() {}).apply();
		final RandomAccess<LabelingType<Integer>> outRA = out.randomAccess();
		outRA.setPosition(new int[] { 0, 0 });
		assertTrue(outRA.get().contains(0));
		assertTrue(outRA.get().contains(10));
		outRA.setPosition(new int[] { 0, 1 });
		assertTrue(outRA.get().isEmpty());
	}

}
