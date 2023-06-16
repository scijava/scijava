/*
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
package net.imagej.ops2.copy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.scijava.util.MersenneTwisterFast;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Test copying of various Image types
 * 
 * @author Christian Dietz (University of Konstanz)
 */
public class CopyImgsTest extends AbstractOpTest {

	private <T extends RealType<T>> void populateData(Img<T> data) {
		final MersenneTwisterFast r = new MersenneTwisterFast(System.currentTimeMillis());
		final Cursor<T> inc = data.cursor();
		while (inc.hasNext()) {
			inc.next().setReal(r.nextDouble() * 255);
		}
	}

	@Test
	public void copyArrayImgNoOutputTest() {
		var data = new ArrayImgFactory<>(new UnsignedByteType()).create(10, 10);
		populateData(data);
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<UnsignedByteType> output =
			(RandomAccessibleInterval<UnsignedByteType>) ops.op("copy.img").arity1().input(
				data).apply();

		final Cursor<UnsignedByteType> inc = data.localizingCursor();
		final RandomAccess<UnsignedByteType> outRA = output.randomAccess();

		while (inc.hasNext()) {
			inc.fwd();
			outRA.setPosition(inc);
			assertEquals(inc.get().get(), outRA.get().get());
		}
	}

	@Test
	public void copyArrayImgWithOutputTest() {
		var data = new ArrayImgFactory<>(new UnsignedByteType()).create(10, 10);
		populateData(data);
		final Img<UnsignedByteType> output = data.factory().create(data.dimensionsAsLongArray());

		ops.op("copy.img").arity1().input(data).output(output).compute();

		final Cursor<UnsignedByteType> inc = data.cursor();
		final Cursor<UnsignedByteType> outc = output.cursor();

		while (inc.hasNext()) {
			assertEquals(outc.next(), inc.next());
		}
	}

	@Test
	public void copyPlanarImgNoOutputTest() {
		var data = new PlanarImgFactory<>(new UnsignedByteType()).create(10, 10);
		populateData(data);
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<UnsignedByteType> output =
				(RandomAccessibleInterval<UnsignedByteType>) ops.op("copy.img").arity1().input(
						data).apply();

		final Cursor<UnsignedByteType> inc = data.localizingCursor();
		final RandomAccess<UnsignedByteType> outRA = output.randomAccess();

		while (inc.hasNext()) {
			inc.fwd();
			outRA.setPosition(inc);
			assertEquals(inc.get().get(), outRA.get().get());
		}
	}

	@Test
	public void copyPlanarImgWithOutputTest() {
		var data = new PlanarImgFactory<>(new UnsignedByteType()).create(10, 10);
		populateData(data);
		final Img<UnsignedByteType> output = data.factory().create(data.dimensionsAsLongArray());

		ops.op("copy.img").arity1().input(data).output(output).compute();

		final Cursor<UnsignedByteType> inc = data.cursor();
		final Cursor<UnsignedByteType> outc = output.cursor();

		while (inc.hasNext()) {
			assertEquals(outc.next(), inc.next());
		}
	}

	@Test
	public void copyCellImgNoOutputTest() {
		var data = new CellImgFactory<>(new UnsignedByteType()).create(10, 10);
		populateData(data);
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<UnsignedByteType> output =
				(RandomAccessibleInterval<UnsignedByteType>) ops.op("copy.img").arity1().input(
						data).apply();

		final Cursor<UnsignedByteType> inc = data.localizingCursor();
		final RandomAccess<UnsignedByteType> outRA = output.randomAccess();

		while (inc.hasNext()) {
			inc.fwd();
			outRA.setPosition(inc);
			assertEquals(inc.get().get(), outRA.get().get());
		}
	}

	@Test
	public void copyCellImgWithOutputTest() {
		var data = new CellImgFactory<>(new UnsignedByteType()).create(10, 10);
		populateData(data);
		final Img<UnsignedByteType> output = data.factory().create(data.dimensionsAsLongArray());

		ops.op("copy.img").arity1().input(data).output(output).compute();

		final Cursor<UnsignedByteType> inc = data.cursor();
		final Cursor<UnsignedByteType> outc = output.cursor();

		while (inc.hasNext()) {
			assertEquals(outc.next(), inc.next());
		}
	}
}
