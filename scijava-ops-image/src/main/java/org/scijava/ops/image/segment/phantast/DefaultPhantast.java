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

package org.scijava.ops.image.segment.phantast;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * PHANTAST (PHase contrast microscopy segmentation toolbox) segmentation of
 * cells in phase contrast microscopy images, e.g. for measuring the confluency
 * of adherent cell cultures.
 * <p>
 * The algorithm proceeds in four steps:
 * </p>
 * <ol>
 * <li>Compute the local contrast image, i.e. the Gaussian-weighted local
 * coefficient of variation (see {@code filter.localCoefficientOfVariation}).</li>
 * <li>Threshold it: pixels with local contrast greater than {@code epsilon}
 * are foreground.</li>
 * <li>Remove foreground objects smaller than {@code minObjectSize} pixels,
 * then fill background regions smaller than {@code minHoleSize} pixels (both
 * 8-connected, i.e. fully connected in n-D).</li>
 * <li>Optionally (2D only) correct for the bright halo artifacts that surround
 * cells in phase contrast images: starting from the object outlines, pixels
 * are iteratively removed by walking along the local intensity gradient
 * direction (estimated with the eight Kirsch compass kernels) towards the
 * brighter halo, as long as the walk remains inside the object.</li>
 * </ol>
 * <p>
 * The confluency (fraction of the image covered by cells) is the mean of the
 * resulting mask, e.g. via {@code stats.mean}.
 * </p>
 * <p>
 * Translated from the PHANTAST plugin for Fiji (<a href=
 * "https://github.com/nicjac/PHANTAST-FIJI">PHANTAST-FIJI</a>), Copyright (c)
 * 2013-2014, Nicolas Jaccard, Department of Biochemical Engineering and CoMPLEX,
 * University College London, with modifications by Olivier Burri (BIOP, EPFL),
 * used under the BSD 3-Clause license. Reference: N. Jaccard, L. D. Griffin,
 * A. Keser, R. J. Macown, A. Super, F. S. Veraitch and N. Szita, "Automated
 * method for the rapid and precise estimation of adherent cell culture
 * characteristics from phase contrast microscopy images", Biotechnology and
 * Bioengineering 111(3):504-517, 2014.
 * </p>
 * <p>
 * Differences from the plugin:
 * </p>
 * <ul>
 * <li>The plugin performs the small object removal and hole filling with
 * ImageJ's particle analyzer, whose interpretation of foreground depends on
 * the global "black background" setting. With that setting off, the plugin
 * actually fills holes smaller than 100 pixels and removes objects smaller
 * than 25 pixels, while its code comments state the opposite intent (remove
 * objects smaller than 100 pixels, fill holes smaller than 25 pixels); with
 * the setting on, its mask is inverted. This op follows the stated intent by
 * default, and both sizes are parameters.</li>
 * <li>The plugin's halo correction skips the first queued pixel of each
 * iteration when removing pixels, and never walks into the first row or
 * column of the image. This op removes all of them, and treats all image
 * borders alike.</li>
 * </ul>
 * <p>
 * The original PHANTAST license notice follows.
 * </p>
 *
 * <pre>
 * Copyright (c) 2013, Nicolas Jaccard
 * Department of Biochemical Engineering, UCL
 * Centre for Mathematics and Physics in the Life Sciences and Experimental
 * Biology, UCL
 * The British Heart Foundation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution
 *   * Neither the names of the University College London or British Heart
 *     Foundation nor the names of its contributors may be used to endorse
 *     or promote products derived from this software without specific
 *     prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Nicolas Jaccard
 * @author Olivier Burri
 * @param <T> the input type
 * @implNote op names='segment.phantast'
 */
public class DefaultPhantast<T extends RealType<T>> implements
	Functions.Arity6<RandomAccessibleInterval<T>, Double, Double, Boolean, Long, Long, RandomAccessibleInterval<BitType>>
{

	@OpDependency(name = "filter.localCoefficientOfVariation")
	private BiFunction<RandomAccessibleInterval<T>, Double, RandomAccessibleInterval<FloatType>> localCoefficientOfVariationOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> createOp;

	@OpDependency(name = "labeling.cca")
	private BiFunction<RandomAccessibleInterval<BitType>, StructuringElement, ImgLabeling<Integer, IntType>> ccaOp;

	/**
	 * Segments cells in a phase contrast image.
	 *
	 * @param input the phase contrast image, without NaN
	 * @param sigma the standard deviation, in pixels, of the Gaussian window of
	 *          the local contrast filter (PHANTAST default: 1.2)
	 * @param epsilon the local contrast threshold above which pixels are
	 *          foreground (PHANTAST default: 0.03)
	 * @param haloCorrection whether to correct for halo artifacts, which is
	 *          only supported for 2D images; defaults to true for 2D images and
	 *          false otherwise
	 * @param minObjectSize the minimum size, in pixels, of foreground objects
	 *          to keep; defaults to 100
	 * @param minHoleSize the minimum size, in pixels, of background regions to
	 *          keep (smaller ones are filled); defaults to 25
	 * @return the binary mask of the cells
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply( //
		final RandomAccessibleInterval<T> input, //
		final Double sigma, //
		final Double epsilon, //
		@Nullable Boolean haloCorrection, //
		@Nullable Long minObjectSize, //
		@Nullable Long minHoleSize //
	) {
		final var is2D = input.numDimensions() == 2;
		if (haloCorrection == null) haloCorrection = is2D;
		if (haloCorrection && !is2D) {
			throw new IllegalArgumentException(
				"Halo correction is only supported for 2D images");
		}
		if (minObjectSize == null) minObjectSize = 100L;
		if (minHoleSize == null) minHoleSize = 25L;

		for (final var t : Views.iterable(input)) {
			if (Double.isNaN(t.getRealDouble())) {
				throw new IllegalArgumentException("NaN values are not supported");
			}
		}
		final var zeroMin = Views.zeroMin(input);
		final var contrast = localCoefficientOfVariationOp.apply(zeroMin, sigma);
		final var mask = createOp.apply(zeroMin, new BitType());
		final double eps = epsilon;
		LoopBuilder.setImages(contrast, mask).forEachPixel((c, m) -> m.set(c
			.getRealDouble() > eps));

		removeSmallComponents(mask, minObjectSize, true);
		removeSmallComponents(mask, minHoleSize, false);

		if (haloCorrection) HaloCorrection.apply(mask, zeroMin);
		return mask;
	}

	/**
	 * Flips the 8-connected components of the given value which are smaller
	 * than {@code minSize} pixels.
	 */
	private void removeSmallComponents(final RandomAccessibleInterval<BitType> mask,
		final long minSize, final boolean value)
	{
		if (minSize <= 1) return;
		final var selected = Converters.convert(mask,
			(Converter<BitType, BitType>) (in, out) -> out.set(in.get() == value),
			new BitType());
		final var labeling = ccaOp.apply(selected,
			StructuringElement.EIGHT_CONNECTED);
		final var regions = new LabelRegions<>(labeling);
		final var ra = mask.randomAccess();
		for (final var region : regions) {
			if (region.inside().size() >= minSize) continue;
			final var c = region.inside().localizingCursor();
			while (c.hasNext()) {
				c.fwd();
				ra.setPosition(c);
				ra.get().set(!value);
			}
		}
	}
}
