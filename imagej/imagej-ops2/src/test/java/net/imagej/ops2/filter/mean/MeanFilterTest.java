/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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
package net.imagej.ops2.filter.mean;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.integer.ByteType;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

public class MeanFilterTest extends AbstractOpTest{
	
	@Test
	public void meanFilterTest() {
		
		Img<ByteType> img = ops.op("create.img").arity2().input(new FinalInterval(5, 5), new ByteType()).outType(new Nil<Img<ByteType>>() {}).apply();
		RectangleShape shape = new RectangleShape(1, false);
		OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>> oobf = new OutOfBoundsBorderFactory<>();
		Img<ByteType> output = ops.op("create.img").arity1().input(img).outType(new Nil<Img<ByteType>>() {}).apply();
		ops.op("filter.mean").arity3().input(img, shape, oobf).output(output).compute();

		// Try with no OutOfBoundsFactory
		ops.op("filter.mean").arity2().input(img, shape).output(output).compute();
	}

	@Test
	public void rawTypeAdaptationTest() {

		Img<ByteType> img = ops.op("create.img").arity2().input(new FinalInterval(5, 5), new ByteType()).outType(new Nil<Img<ByteType>>() {}).apply();
		RectangleShape shape = new RectangleShape(1, false);
		OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>> oobf = new OutOfBoundsBorderFactory<>();
		var result = ops.op("filter.mean").arity3().input(img, shape, oobf).outType(Img.class).apply();

	}
}
