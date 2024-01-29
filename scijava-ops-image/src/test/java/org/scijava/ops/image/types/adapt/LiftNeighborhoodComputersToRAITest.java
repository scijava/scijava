/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.types.adapt;

import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

/**
 * Tests that {@link Computers} written on {@link Neighborhood}s can be lifted
 * to {@link net.imglib2.RandomAccessibleInterval}s.
 */
public class LiftNeighborhoodComputersToRAITest extends AbstractOpTest {

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Computers.Arity1<Neighborhood<UnsignedByteType>, UnsignedByteType> testOp =
		(in, out) -> {
			var cursor = in.cursor();
			long sum = 0;
			while (cursor.hasNext())
				sum += cursor.next().get();

			out.setInteger(sum);
		};

	@Test
	public void liftArity1() {
		// Define parameters
		var shape = new RectangleShape(1, false);
		var inImg = TestImgGeneration.unsignedByteArray(true, 10, 10);
		var actual = ArrayImgs.unsignedBytes(10, 10);

		// Call the above OpField through Ops, ensuring it is lifted
		ops.op("test.liftImg").arity2() //
			.input(inImg, shape) //
			.output(actual) //
			.compute();

		// Assert correctness by performing the same lifting manually
		var extended = Views.extend(inImg, new OutOfBoundsMirrorFactory<>(
			OutOfBoundsMirrorFactory.Boundary.SINGLE));
		var neighborhoods = shape.neighborhoodsRandomAccessibleSafe(extended);
		var intervaled = Views.interval(neighborhoods, inImg);
		var expected = ArrayImgs.unsignedBytes(10, 10);

		var cursor = inImg.cursor();
		var neighborhoodRA = intervaled.randomAccess();
		var actualRA = actual.randomAccess();
		var expectedRA = expected.randomAccess();

		while (cursor.hasNext()) {
			cursor.next();
			cursor.localize(neighborhoodRA);
			cursor.localize(actualRA);
			cursor.localize(expectedRA);
			testOp.compute(neighborhoodRA.get(), expectedRA.get());
			Assertions.assertEquals(actualRA.get().get(), expectedRA.get().get());
		}

	}

	@Test
	public void liftArity1WithOOBF() {
		// Define parameters
		var shape = new RectangleShape(1, false);
		var oobf = new OutOfBoundsConstantValueFactory<>(new UnsignedByteType(0));
		var inImg = TestImgGeneration.unsignedByteArray(true, 10, 10);
		var actual = ArrayImgs.unsignedBytes(10, 10);

		// Call the above OpField through Ops, ensuring it is lifted
		ops.op("test.liftImg").arity3() //
			.input(inImg, shape, oobf) //
			.output(actual) //
			.compute();

		// Assert correctness by performing the same lifting manually
		var extended = Views.extend(inImg, oobf);
		var neighborhoods = shape.neighborhoodsRandomAccessibleSafe(extended);
		var intervaled = Views.interval(neighborhoods, inImg);
		var expected = ArrayImgs.unsignedBytes(10, 10);

		var cursor = inImg.cursor();
		var neighborhoodRA = intervaled.randomAccess();
		var actualRA = actual.randomAccess();
		var expectedRA = expected.randomAccess();

		while (cursor.hasNext()) {
			cursor.next();
			cursor.localize(neighborhoodRA);
			cursor.localize(actualRA);
			cursor.localize(expectedRA);
			testOp.compute(neighborhoodRA.get(), expectedRA.get());
			Assertions.assertEquals(actualRA.get().get(), expectedRA.get().get());
		}

	}

}
