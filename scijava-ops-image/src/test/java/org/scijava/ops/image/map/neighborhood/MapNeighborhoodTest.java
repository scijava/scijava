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

package org.scijava.ops.image.map.neighborhood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;

/**
 * Test for {@link DefaultMapNeighborhood} and
 * {@link MapNeighborhoodWithCenter}.
 *
 * @author Jonathan Hale (University of Konstanz)
 */
public class MapNeighborhoodTest extends AbstractOpTest {

	private Img<ByteType> in;
	private Img<ByteType> out;

	@BeforeEach
	public void initImg() {
		in = TestImgGeneration.byteArray(true, 11, 10);
		out = TestImgGeneration.byteArray(false, 11, 10);
	}

	/**
	 * Test if every neighborhood pixel of the image was really accessed during
	 * the map operation.
	 *
	 * @see DefaultMapNeighborhood
	 */
	@Test
	public void testMapNeighborhoodsAccess() {
		ops.op("map.neighborhood").input(in, new RectangleShape(1, false),
			new CountNeighbors()).output(out).compute();

		for (final ByteType t : out) {
			assertEquals(9, t.get());
		}
	}

	/**
	 * Function which increments the output value for every pixel in the
	 * neighborhood.
	 *
	 * @author Jonathan Hale
	 */
	private static class CountNeighbors implements
		Computers.Arity1<Iterable<ByteType>, ByteType>
	{

		@Override
		public void compute(final Iterable<ByteType> input, final ByteType output) {
			for (Iterator<ByteType> iter = input.iterator(); iter.hasNext(); iter
				.next())
			{
				output.inc();
			}
		}
	}

	/**
	 * Function which increments a outputPixel for every neighboring pixel defined
	 * by the mapping.
	 *
	 * @author Jonathan Hale
	 */
	private static class CountNeighborsWithCenter implements
		Computers.Arity2<Iterable<ByteType>, ByteType, ByteType>
	{

		@Override
		public void compute(final Iterable<ByteType> neighborhood,
			final ByteType center, final ByteType output)
		{
			ByteType a = center;

			a.set((byte) 0);
			output.set((byte) 0);

			for (Iterator<ByteType> iter = neighborhood.iterator(); iter
				.hasNext(); iter.next())
			{
				output.inc();
				a.inc();
			}
		}
	}

	/**
	 * Computer which sets a outputPixel to {@code input.get() + 1}. Generally,
	 * this computer is invalid as input to neighborhood maps.
	 *
	 * @author Jonathan Hale
	 */
	private static class Increment implements
		Computers.Arity1<ByteType, ByteType>
	{

		@Override
		public void compute(final ByteType input, final ByteType output) {
			output.set((byte) (input.get() + 1));
		}
	}

}
