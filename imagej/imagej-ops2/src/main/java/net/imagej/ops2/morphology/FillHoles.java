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

package net.imagej.ops2.morphology;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.BooleanType;
import net.imglib2.view.Views;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Fills the holes of a BooleanType image.
 * 
 * @author Martin Horn (University of Konstanz)
 * @author Daniel Seebacher (University of Konstanz)
 */
@Plugin(type = Op.class, name = "morphology.fillHoles")
@Parameter(key = "input")
@Parameter(key = "structElement")
@Parameter(key = "output")
public class FillHoles<T extends BooleanType<T>> implements
	Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>>
{

//	@Parameter(required = false)
//	private Shape structElement = new RectangleShape(1, false);

	@OpDependency(name = "morphology.floodFill")
	private Computers.Arity3<RandomAccessibleInterval<T>, Localizable, Shape, RandomAccessibleInterval<T>> floodFillComp;

	@Override
	public void compute(final RandomAccessibleInterval<T> op,
		final Shape structElement, final RandomAccessibleInterval<T> r)
	{
		final IterableInterval<T> iterOp = Views.flatIterable(op);
		final IterableInterval<T> iterR = Views.flatIterable(r);

		long[] dim = new long[r.numDimensions()];
		r.dimensions(dim);
		Cursor<T> rc = iterR.cursor();
		Cursor<T> opc = iterOp.localizingCursor();
		// Fill with non background marker
		while (rc.hasNext()) {
			rc.next().setOne();
		}

		rc.reset();
		boolean border;
		// Flood fill from every background border voxel
		while (rc.hasNext()) {
			rc.next();
			opc.next();
			if (rc.get().get() && !opc.get().get()) {
				border = false;
				for (int i = 0; i < r.numDimensions(); i++) {
					if (rc.getLongPosition(i) == 0 || rc.getLongPosition(i) == dim[i] -
						1)
					{
						border = true;
						break;
					}
				}
				if (border) {
					floodFillComp.compute(op, rc, structElement, r);
				}
			}
		}
	}

}
