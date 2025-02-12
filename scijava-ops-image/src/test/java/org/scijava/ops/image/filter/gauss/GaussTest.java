/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.filter.gauss;

import net.imglib2.Cursor;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import org.scijava.types.Nil;

/**
 * Tests Gaussian convolution.
 *
 * @author Martin Horn (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 */
public class GaussTest extends AbstractOpTest {

	@Test
	public void testGauss() {
		final Img<ByteType> in = TestImgGeneration.byteArray(true, new long[] { 10,
			10 });
		final Img<ByteType> out1 = ops.op("create.img").input(in, Util
			.getTypeFromInterval(in)).outType(new Nil<Img<ByteType>>()
		{}).apply();
		final double sigma = 5;
		final Img<ByteType> out2 = ops.op("create.img").input(in, Util
			.getTypeFromInterval(in)).outType(new Nil<Img<ByteType>>()
		{}).apply();

		ops.op("filter.gauss").input(in, sigma).output(out1).compute();
		try {
			Gauss3.gauss(sigma, Views.extendMirrorSingle(in), out2);
		}
		catch (IncompatibleTypeException e) {
			throw new RuntimeException(e);
		}

		// compare outputs
		final Cursor<ByteType> c1 = out1.cursor();
		final Cursor<ByteType> c2 = out2.cursor();

		while (c1.hasNext()) {
			Assertions.assertEquals(c1.next().getRealDouble(), c2.next()
				.getRealDouble(), 0);
		}
	}
}
