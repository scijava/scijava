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
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.BooleanType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpDependency;

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
	 * TODO
	 *
	 * @param input
	 * @param structElement
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape structElement, final RandomAccessibleInterval<T> output)
	{
		var dim = new long[output.numDimensions()];
		output.dimensions(dim);

		// Fill with non background marker
		T one = Util.getTypeFromInterval(output).copy();
		one.setOne();
		fillConstant.compute(one, output);

		// Flood fill background along the edges, to exclude holes bordering an edge.
		// What is left will be the shape with interior holes filled in.
		var raIn = input.randomAccess();
		var raOut = output.randomAccess();

		[5, 4, 3]
		[0, ..., ...]
		[l_i-1, ..., ...]
		[..., 0, ...]
		[..., l_i-1, ...]
		[..., ..., 0]
		[..., ..., l_i-1]
		for (var i = 0; i < dim.length; i++) {
			var topSlice = Views.hyperSlice(input, i, 0);
			var bottomSlice = Views.hyperSlice(input, i, dim[i] - 1);
			LoopBuilder.setImages(topSlice).forEachPixel(t -> {
				floodFillComp.compute(topSlice, , structElement, output);
			});
		}

		while (rc.hasNext()) {
			rc.next();
			opc.next();
			if (!opc.get().get()) continue; // this pixel is not a background pixel
			if (rc.get().get()) continue; // a flood fill already took care of this pixel
			boolean border = false;
			for (var i = 0; i < output.numDimensions(); i++) {
				if (rc.getLongPosition(i) == 0 || rc.getLongPosition(i) == dim[i] -
					1)
				{
					border = true;
					break;
				}
			}
			if (border) {
				floodFillComp.compute(input, rc, structElement, output);
			}
		}
	}
}
