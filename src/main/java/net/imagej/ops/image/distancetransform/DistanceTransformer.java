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
package net.imagej.ops.image.distancetransform;

import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Passes an input image of any dimension off to the correct Distance Transform
 * algorithm. Before doing so, it also ensures that the output
 * {@link RandomAccessibleInterval} is of a suitable {@link RealType} in order
 * to be able to contain the entire range of the output.
 * 
 * @author Gabriel Selzer
 *
 * @param <B> - the {@link BooleanType} of the input image
 * @param <T> - the {@link RealType} of the output image
 */
@Plugin(type = Op.class, name = "image.distanceTransform")
@Parameter(key = "binaryInput")
@Parameter(key = "executorService")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class DistanceTransformer<B extends BooleanType<B>, T extends RealType<T>>
		implements BiComputer<RandomAccessibleInterval<B>, ExecutorService, RandomAccessibleInterval<T>> {

	@Override
	public void compute(RandomAccessibleInterval<B> in, ExecutorService es, RandomAccessibleInterval<T> out) {
		// make sure that the output type is suitable to be able to hold the maximum
		// possible distance (replaces Conforms)
		long max_dist = 0;
		for (int i = 0; i < in.numDimensions(); i++)
			max_dist += in.dimension(i) * in.dimension(i);
		if (max_dist > Views.iterable(out).firstElement().getMaxValue())
			throw new IllegalArgumentException(
					"The type of the output image is too small to calculate the Distance Transform on this image!");
		switch (in.numDimensions()) {
		case 2: {
			DistanceTransform2D.compute(in, es, out);
			break;
		}
		case 3: {
			DistanceTransform3D.compute(in, es, out);
			break;
		}
		default: {
			DefaultDistanceTransform.compute(in, es, out);
			break;
		}
		}

	}

}
