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

package org.scijava.ops.image.morphology;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.BooleanType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

import java.util.ArrayList;

/**
 * Fills the holes of a BooleanType image.
 *
 * @author Martin Horn (University of Konstanz)
 * @author Daniel Seebacher (University of Konstanz)
 * @implNote op names='morphology.fillHoles'
 */
public class FillHoles<T extends BooleanType<T>> implements
	Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>>
{

//	private Shape structElement = new RectangleShape(1, false);

	@OpDependency(name = "morphology.floodFill")
	private Computers.Arity3<RandomAccessibleInterval<T>, Localizable, Shape, RandomAccessibleInterval<T>> floodFillComp;

	@OpDependency(name = "image.fill")
	private Computers.Arity1<T, RandomAccessibleInterval<T>> fillConstant;

	/**
	 * Fills holes in {@code input} by:
	 * <ol>
	 * <li>Setting the entire output to be foreground</li>
	 * <li>Iteratively finding all pixels reachable from the edges, setting them to background in the output</li>
	 * </ol>
	 *
	 * @param input the input image
	 * @param shape a {@link Shape} defining the set of neighbors around each pixel.
	 * @param output a pre-allocated output buffer.
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape shape, final RandomAccessibleInterval<T> output)
	{
		var dim = new long[output.numDimensions()];
		output.dimensions(dim);

		// Set entire output to foreground
		T one = Util.getTypeFromInterval(output).copy();
		one.setOne();
		fillConstant.compute(one, output);

		// Iterate over each face
		for (var i = 0; i < dim.length; i++) {
			// top slice
			fillHolesInSlice(input, i, 0, shape, output);
			// bottom slice
			fillHolesInSlice(input, i, dim[i] - 1, shape, output);
		}

	}

	private void fillHolesInSlice(RandomAccessibleInterval<T> input, int i, long pos, Shape shape, RandomAccessibleInterval<T> output) {
		var cursor = Views.hyperSlice(input, i, pos).cursor();
		var raOut = output.randomAccess();
		raOut.setPosition(pos, i);
		int dims = input.numDimensions();
		while (cursor.hasNext()) {
			cursor.next();
			if (cursor.get().get()){
				// this pixel is not a background pixel
				continue;
			}
			// FIXME This is ugly
			for(int j = 0; j < dims - 1; j++) {
				raOut.setPosition(cursor.getIntPosition(j), j >= i ? j + 1: j);
			}
			if (!raOut.get().get()){
				// a flood fill already took care of this pixel
				continue;
			}
			floodFillComp.compute(input, raOut, shape, output);
		}

	}
}
