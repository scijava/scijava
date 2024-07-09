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

package org.scijava.ops.image.features.lbp2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.BiFunction;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

/**
 * Default implementation of 2d local binary patterns
 *
 * @author Andreas Graumann (University of Konstanz)
 * @param <I>
 * @implNote op names='features.lbp2d'
 */
public class DefaultLBP2D<I extends RealType<I>> implements
	Functions.Arity3<RandomAccessibleInterval<I>, Integer, Integer, ArrayList<LongType>>
{

	@OpDependency(name = "image.histogram")
	private BiFunction<ArrayList<LongType>, Integer, Histogram1d<LongType>> histOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param distance
	 * @param histogramSize
	 * @return the output
	 */
	@Override
	public ArrayList<LongType> apply(RandomAccessibleInterval<I> input,
		Integer distance, Integer histogramSize)
	{
        var output = new ArrayList<LongType>();

		if (input.numDimensions() != 2) throw new IllegalArgumentException(
			"Only 2 dimensional images allowed!");
        var numberList = new ArrayList<LongType>();
		RandomAccess<I> raInput = Views.extendZero(input).randomAccess();
		final var cInput = Views.flatIterable(input).cursor();
		final var cNeigh =
			new ClockwiseDistanceNeighborhoodIterator<I>(raInput, distance);

		while (cInput.hasNext()) {
			cInput.next();
            var centerValue = cInput.get().getRealDouble();

            var resultBinaryValue = 0;

			cNeigh.reset();
			while (cNeigh.hasNext()) {
                var nValue = cNeigh.next().getRealDouble();
                var pos = cNeigh.getIndex();
				if (nValue >= centerValue) {
					resultBinaryValue |= 1 << pos;
				}
			}
			numberList.add(new LongType(resultBinaryValue));
		}

        var hist = histOp.apply(numberList, histogramSize);
        var c = hist.iterator();
		while (c.hasNext()) {
			output.add(new LongType(c.next().get()));
		}

		return output;

	}

	final class ClockwiseDistanceNeighborhoodIterator<T extends Type<T>>
		implements java.util.Iterator<T>
	{

		final private RandomAccess<T> m_ra;

		final private int m_distance;

		final private int[][] CLOCKWISE_OFFSETS = { { 0, -1 }, { 1, 0 }, { 1, 0 }, {
			0, 1 }, { 0, 1 }, { -1, 0 }, { -1, 0 }, { 0, -1 } };

		// index of offset to be executed at next next() call.
		private int m_curOffset = 0;

		private int m_startIndex = 8;

		public ClockwiseDistanceNeighborhoodIterator(final RandomAccess<T> ra,
			final int distance)
		{
			m_ra = ra;
			m_distance = distance;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final boolean hasNext() {
			return m_curOffset != m_startIndex;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final T next() {
			m_ra.move(CLOCKWISE_OFFSETS[m_curOffset][0] * m_distance, 0);
			m_ra.move(CLOCKWISE_OFFSETS[m_curOffset][1] * m_distance, 1);

			m_curOffset++;// = (m_curOffset + 1) & 7; //<=> (m_curOffset+1) % 8

			return m_ra.get();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void remove() {
			throw new UnsupportedOperationException();
		}

		public final int getIndex() {
			return m_curOffset;
		}

		/**
		 * Reset the current offset index. This does not influence the RandomAccess.
		 */
		public final void reset() {
			m_curOffset = 0;
			m_startIndex = 8;
		}

	}

}
