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

package org.scijava.ops.image.segment.levelset;

import java.util.function.BiFunction;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Level-set active contour segmentation: evolves an initial contour on an
 * image and returns the final segmentation mask (see
 * {@code image.levelSetFunction} for the level-set function).
 * <p>
 * Two models are available, selected by {@code method}:
 * </p>
 * <ul>
 * <li>{@code "chanVese"} (default): the region-based, piecewise-constant
 * active contour without edges of T. F. Chan and L. A. Vese ("Active contours
 * without edges", <i>IEEE Trans. Image Process.</i> 10(2): 266&ndash;277,
 * 2001). The contour minimizes {@code mu Length + nu Area + sum over the
 * inside of (u - c1)^2 + sum over the outside of (u - c2)^2}, where {@code u}
 * is the image standardized to zero mean and unit variance (which makes the
 * weights independent of the intensity scale) and {@code c1}, {@code c2} are
 * the mean values inside and outside, by the gradient flow
 * {@code phi_t = delta(phi) (mu kappa - nu - (u - c1)^2 + (u - c2)^2)} with a
 * regularized Dirac delta of width one pixel. No edges are needed, so it
 * segments noisy or blurred objects that differ in mean intensity from the
 * background.</li>
 * <li>{@code "geodesic"}: the geodesic active contours of V. Caselles, R.
 * Kimmel and G. Sapiro ("Geodesic active contours", <i>Int. J. Comput.
 * Vision</i> 22(1): 61&ndash;79, 1997), with the balloon force of their model:
 * {@code phi_t = g (alpha kappa + c) |grad phi| + beta grad g . grad phi}. The
 * edge stopping function is {@code g = 1 / (1 + (|grad u_sigma| /
 * lambda)^2)}, where {@code u_sigma} is the image rescaled to [0, 1] and
 * smoothed by a
 * Gaussian of standard deviation {@code sigma} and {@code lambda} is a tenth
 * of the largest gradient magnitude, so that {@code g} is close to 0 on the
 * strongest edges and to 1 in flat regions. The contour expands ({@code c >
 * 0}) or shrinks ({@code c < 0}) until it is stopped and attracted by
 * edges.</li>
 * </ul>
 * <p>
 * Here {@code phi} is the level-set function (positive inside the contour) and
 * {@code kappa = div(grad phi / |grad phi|)} its mean curvature, which
 * regularizes the contour. The level-set function is initialized with the
 * signed distance transform ({@code image.signedDistanceTransform}) of the
 * initial mask and evolved with an explicit scheme on the full grid, using
 * upwind differences for the propagation and advection terms, and differences
 * of the unit normals between adjacent pixels for the curvature (which thus
 * also removes isolated pixels). The time step is chosen for stability and,
 * for Chan-Vese, so that the contour moves by at most half a pixel per
 * iteration. Every five iterations, the level-set function is reinitialized to a signed distance function: pixels next
 * to the zero level set keep their sub-pixel distance estimate {@code phi /
 * |grad phi|}, and all others get the signed distance of the current mask.
 * The evolution stops after {@code maxIterations} iterations, or earlier when,
 * between two reinitializations, at most {@code tolerance} times the number of
 * inside pixels have changed sides. The computation works in any number of
 * dimensions, with anisotropic calibration.
 * </p>
 * <p>
 * Both models are implemented from the cited papers. The level-set framework
 * is that of S. Osher and J. A. Sethian ("Fronts propagating with
 * curvature-dependent speed", <i>J. Comput. Phys.</i> 79(1): 12&ndash;49,
 * 1988).
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @param <T> image type
 * @param <B> initial mask type
 * @see <a href="https://doi.org/10.1109/83.902291">Chan and Vese 2001</a>
 * @see <a href="https://doi.org/10.1023/A:1007979827043">Caselles, Kimmel and
 *      Sapiro 1997</a>
 * @implNote op names='segment.levelSet'
 */
public class LevelSetSegmentation<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Functions.Arity10<RandomAccessibleInterval<T>, RandomAccessibleInterval<B>, String, Double, Double, Double, Double, Integer, Double, double[], RandomAccessibleInterval<BitType>>
{

	@OpDependency(name = "image.signedDistanceTransform")
	private BiFunction<RandomAccessibleInterval<BitType>, double[], RandomAccessibleInterval<FloatType>> sdtOp;

	@OpDependency(name = "filter.gauss")
	private Computers.Arity2<RandomAccessibleInterval<DoubleType>, double[], RandomAccessibleInterval<DoubleType>> gaussOp;

	/**
	 * Evolves the active contour and returns the segmented region.
	 *
	 * @param image the image to segment
	 * @param init the initial contour, as a mask with the dimensions of the
	 *          image
	 * @param method the active contour model, {@code chanVese} or
	 *          {@code geodesic} (default: chanVese)
	 * @param curvature the weight of the curvature term, {@code mu} for
	 *          Chan-Vese and {@code alpha} for geodesic contours (default: 1)
	 * @param propagation the area penalty {@code nu} for Chan-Vese (positive
	 *          values shrink the contour; default 0), or the balloon force
	 *          {@code c} for geodesic contours (positive values expand the
	 *          contour; default 1)
	 * @param advection the weight {@code beta} of the edge attraction term of
	 *          geodesic contours (default: 1; ignored by Chan-Vese)
	 * @param sigma the standard deviation, in calibrated units, of the Gaussian
	 *          smoothing applied before computing the edge map of geodesic
	 *          contours, or 0 for none (default: 1; ignored by Chan-Vese)
	 * @param maxIterations the maximal number of iterations (default: 500)
	 * @param tolerance the convergence tolerance: the evolution stops when the
	 *          fraction of pixels (relative to the inside area) that changed
	 *          sides in five iterations is at most this value (default: 0.005)
	 * @param calibration the pixel spacing along each dimension (default: 1 in
	 *          every dimension)
	 * @return the segmentation mask (the positive values of the final level-set
	 *         function), with the interval of the image
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply(
		final RandomAccessibleInterval<T> image,
		final RandomAccessibleInterval<B> init, @Nullable final String method,
		@Nullable final Double curvature, @Nullable final Double propagation,
		@Nullable final Double advection, @Nullable final Double sigma,
		@Nullable final Integer maxIterations, @Nullable final Double tolerance,
		@Nullable final double[] calibration)
	{
		final var phi = LevelSets.evolve(image, init, method, curvature,
			propagation, advection, sigma, maxIterations, tolerance, calibration,
			sdtOp, gaussOp);
		final var out = ArrayImgs.bits(image.dimensionsAsLongArray());
		var i = 0;
		for (final var t : out)
			t.set(phi[i++] > 0);
		return Views.translate(out, image.minAsLongArray());
	}
}
