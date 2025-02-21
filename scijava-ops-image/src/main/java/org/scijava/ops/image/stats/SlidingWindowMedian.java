/*-
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

package org.scijava.ops.image.stats;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Container;

/**
 * @author Edward Evans
 * @author Gabriel Selzer
 * @implNote op names='filter.median', priority='100.'
 */

public class SlidingWindowMedian
	implements Computers.Arity2<RandomAccessible<FloatType>, Integer, RandomAccessibleInterval<FloatType>> {

	/**
	 * Sliding window median filter
	 *
	 * @param input The input image.
	 * @param span The span value used to create a square kernel. The kernel
	 * 	shape is (size, size) where size is (span * 2 + 1). For example,
	 * 	a span value of 1 produces a (3, 3) kernel.
	 * @param output The output image/container.
	 */
	@Override
	public void compute(RandomAccessible<FloatType> input,
			final Integer span, @Container RandomAccessibleInterval<FloatType> output) {
		// prep input image
		var oobf = new OutOfBoundsMirrorFactory<FloatType, RandomAccessibleInterval<FloatType>>(Boundary.SINGLE);
		var extended = Views.extend((RandomAccessibleInterval<FloatType>) input, oobf);

		// create sliding window buffer
		final int size = span * 2 + 1;
		float[][]  window = new float[size][size];
		float[] buffer = new float[size * size];
		int bufLen = 0;
		int medianIndex = 0;

		// get random access
		var raIn = extended.randomAccess();
		var raOut = output.randomAccess();

		// slide window across the image
		// here 'i' and 'j' are 'x' and 'y' on the output image
		int oY = 0;
		int wX = 0;
		int wY = 0;
		long[] dims = output.dimensionsAsLongArray();
		for (int i = 0; i < dims[0]; i++) {
			// read a full window at the top of the image
			// here 'k' and 'l' are 'x' and 'y' of the window
			for (int l = -span + oY; l < size + (-span + oY); l++) {
				// set window y-axis (row)
				raIn.setPosition(l, 1);
				for (int k = -span + i; k < size + (-span + i); k++) {
					// set window x-axis (col)
					raIn.setPosition(k, 0);
					var v = raIn.get().get();
					window[wY][wX] = v;
					insertSorted(buffer, v, bufLen++);
					wX++;
				}
				wX = 0;
				wY++;
			}
			wY = 0;
			// compute median and store the output
			medianIndex = buffer.length / 2;
			raOut.setPositionAndGet(i, oY).set(buffer[medianIndex]);
			// advance the window column wise (down, +y) remove old row, fill new row
			oY++;
			for (int j = size; oY < dims[1]; j++) {
				var row = j % size;
				// remove old data from the row from window and buffer
				for (int w = 0; w < window[row].length; w++) {
					removeSorted(buffer, window[row][w]);
					bufLen--;
				}
				// set random access to new row y-axis pos
				raIn.setPosition(j, 1);
				for (int e = -span + i; e < size + (-span + i); e++) {
					// set random acces to new row x-axis pos
					raIn.setPosition(e, 0);
					var v = raIn.get().get();
					window[row][wX] = v;
					insertSorted(buffer, v, bufLen++);
					wX++;
				}
				wX = 0;
				// compute new median and store in output
				medianIndex = buffer.length / 2;
				raOut.setPositionAndGet(i, oY).set(buffer[medianIndex]);
				oY++;
			}
			oY = 0;
			bufLen = 0;
		}
	}

	public static void insertSorted(float[] arr, float value, int len) {
		// find the sorted position the value should be inserted
		int insertPos = 0;
		while (insertPos < len && arr[insertPos] < value && arr[insertPos] != 0.0f) {
			insertPos++;
		}

		// If we're not inserting at the end, shift elements to make space
		if (insertPos < len) {
			// Find the last non-zero element
			//int lastIndex = len - 1;
			//while (lastIndex > 0 && arr[lastIndex] == 0.0f) {
			//	lastIndex--;
			//}

			// Shift elements to the right
			for (int i = len; i >= insertPos + 1; i--) {
				arr[i] = arr[i - 1];
			}
		}

		// Insert the value
		arr[insertPos] = value;
	}

	public static void removeSorted(float[] arr, float value) {
		// Find the position where value should be inserted
		int insertPos = 0;
		while (insertPos < arr.length && arr[insertPos] != value) {
			insertPos++;
		}

		// Shift elements to the right
		for (int i = insertPos; i < arr.length - 1; i++) {
			arr[i] = arr[i + 1];
		}
	}

}
