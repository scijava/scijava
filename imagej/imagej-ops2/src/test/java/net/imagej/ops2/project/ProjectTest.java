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

package net.imagej.ops2.project;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imagej.ops2.AbstractOpTest;
import net.imagej.testutil.TestImgGeneration;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.OpBuilder;
import org.scijava.types.Nil;

public class ProjectTest extends AbstractOpTest {

	private final int PROJECTION_DIM = 2;

	private Img<UnsignedByteType> in;
	private Img<UnsignedByteType> out1;
	private Img<UnsignedByteType> out2;
	private Computers.Arity1<Iterable<UnsignedByteType>, UnsignedByteType> op;

	@BeforeEach
	public void initImg() {
		in = TestImgGeneration.unsignedByteArray(false, 10, 10, 10);

		final RandomAccess<UnsignedByteType> randomAccess = in.randomAccess();

		// at each x,y,z fill with x+y
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				for (int z = 0; z < 10; z++) {
					randomAccess.setPosition(new long[] { x, y, z });
					randomAccess.get().setReal(x + y);
				}
			}
		}

		out1 = TestImgGeneration.unsignedByteArray(false, 10, 10);
		out2 = TestImgGeneration.unsignedByteArray(false, 10, 10);

		op = OpBuilder.matchComputer(ops.env(), "stats.sum", new Nil<Iterable<UnsignedByteType>>() {},
				new Nil<UnsignedByteType>() {});
	}

	@Test
	public void testProjector() {
		// TODO: uncomment when this Op is ported (assuming it will be?)
		// ops.run(DefaultProjectParallel.class, out1, in, op, PROJECTION_DIM);
		// ops.run(DefaultProjectParallel.class, out2, in, op, PROJECTION_DIM);
		// testEquality(out1, out2);

		ops.op("project").input(in, op, PROJECTION_DIM).output(out1).compute();
		ops.op("project").input(in, op, PROJECTION_DIM).output(out2).compute();
		testEquality(out1, out2);
	}

	private void testEquality(final Img<UnsignedByteType> img1, final Img<UnsignedByteType> img2) {
		final RandomAccess<UnsignedByteType> img1RandomAccess = img1.randomAccess();
		final RandomAccess<UnsignedByteType> img2RandomAccess = img2.randomAccess();

		// at each x,y position the sum projection should be (x+y) *size(z)
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				img1RandomAccess.setPosition(new long[] { x, y });
				img2RandomAccess.setPosition(new long[] { x, y });

				assertEquals(img1RandomAccess.get().get(), //
						in.dimension(PROJECTION_DIM) * (x + y));
				assertEquals(img2RandomAccess.get().get(), //
						in.dimension(PROJECTION_DIM) * (x + y));
			}
		}
	}
}
