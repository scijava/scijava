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

package org.scijava.ops.image.filter.tensor;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.scijava.ops.spi.Nullable;

/**
 * Filters derived from second-order tensors: the Hessian matrix and the
 * structure tensor.
 * <p>
 * All scales ({@code sigma}, {@code sigmaGradient}, {@code sigmaIntegration})
 * are Gaussian standard deviations in physical units, i.e. they are divided by
 * the optional per-dimension {@code calibration} (pixel spacing, default 1) to
 * obtain pixel sigmas, and derivatives are likewise expressed in physical
 * units. Unlike most ops, a calibration with fewer values than dimensions is
 * accepted, and the missing values default to 1. Derivatives are central finite differences of the Gaussian-smoothed
 * image ({@code [-1, 0, 1] / 2} for gradients, {@code [1, -2, 1]} and the
 * 3&times;3 cross stencil for second derivatives); a scale of 0 disables
 * smoothing. Eigenvalues are computed with {@code TensorEigenValues} of
 * imglib2-algorithm. The image is mirrored
 * at its borders for smoothing, and border pixels are repeated for the finite
 * differences. All outputs are {@link DoubleType} images with zero-based
 * coordinates.
 * </p>
 * <p>
 * Multi-component outputs ({@code filter.hessianEigenvalues},
 * {@code filter.structureTensor}, {@code filter.structureTensorEigenvalues})
 * have one extra, last dimension holding the components. Symmetric tensors
 * are stored as their upper triangle in row-major order,
 * {@code [t11, t12, ..., t1n, t22, t23, ..., tnn]} (n(n+1)/2 components), as
 * in imglib2-algorithm's {@code HessianMatrix} and {@code TensorEigenValues}.
 * </p>
 * <p>
 * Note: unlike {@code filter.hessian}, which applies a Sobel operator twice
 * without explicit smoothing and returns the full n&times;n matrix, the Hessian
 * here is computed at a chosen Gaussian scale, which is what scale-space
 * measures like eigenvalues, shape index and sheetness require, similar to
 * {@code filter.tubeness}. However, {@code filter.tubeness} uses the
 * {@code HessianMatrix} of imglib2-algorithm, whose diagonal stencil has a
 * step of 2 pixels, so its eigenvalues differ slightly from those of
 * {@code filter.hessianEigenvalues} at the same scale. Eigenvalue orders also
 * differ: {@code filter.hessianEigenvalues} sorts by ascending absolute value
 * (as in Frangi et al.), {@code filter.structureTensorEigenvalues} by
 * descending value, and {@code filter.tubeness} keeps the order of
 * {@code TensorEigenValues}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class TensorFilters {

	private TensorFilters() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the eigenvalues of the Hessian matrix at each pixel of an n-D
	 * image, at the given Gaussian scale.
	 * <p>
	 * The eigenvalues are sorted in ascending order of absolute value, i.e.
	 * {@code |λ1| <= |λ2| <= ... <= |λn|}, the convention of Frangi et al.
	 * (1998) and Sato et al. (1998). Bright structures on a dark background
	 * have negative eigenvalues across them; for example, at the center of a
	 * bright 3D line, {@code λ1 ≈ 0} and {@code λ2 ≈ λ3 << 0}.
	 * </p>
	 * <p>
	 * With {@code normalize} enabled, the Hessian is multiplied by
	 * {@code sigma²} (γ = 1 scale normalization; Lindeberg 1998), which makes
	 * responses comparable across scales.
	 * </p>
	 *
	 * @param <T> the input type
	 * @param input the n-dimensional input image
	 * @param sigma the Gaussian scale in physical units (0 for no smoothing)
	 * @param normalize whether to scale-normalize the Hessian by
	 *          {@code sigma²}; defaults to {@code false}
	 * @param calibration the pixel spacing per dimension; defaults to 1 in every
	 *          dimension
	 * @return an (n+1)-dimensional image whose last dimension holds the n
	 *         sorted eigenvalues
	 * @implNote op names='filter.hessianEigenvalues'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		hessianEigenvalues( //
			final RandomAccessibleInterval<T> input, //
			final Double sigma, //
			@Nullable Boolean normalize, //
			@Nullable final double[] calibration //
	) {
		if (normalize == null) normalize = false;
		final var hessian = TensorUtils.hessian(input, sigma, calibration,
			normalize);
		return TensorUtils.eigenvalues(hessian, true);
	}

	/**
	 * Computes the structure tensor (also called the second-moment matrix) of
	 * an n-D image: the outer product of the image gradient with itself,
	 * averaged with a Gaussian window,
	 * {@code J = G(sigmaIntegration) * (∇f ∇fᵀ)}, where the gradient
	 * {@code ∇f} is computed at scale {@code sigmaGradient}.
	 * <p>
	 * References: Förstner &amp; Gülch (1987), "A fast operator for detection
	 * and precise location of distinct points, corners and centres of circular
	 * features"; Bigün, Granlund &amp; Wiklund (1991), "Multidimensional
	 * orientation estimation with applications to texture analysis and optical
	 * flow", IEEE TPAMI 13(8).
	 * </p>
	 *
	 * @param <T> the input type
	 * @param input the n-dimensional input image
	 * @param sigmaGradient the Gaussian scale for computing the gradient, in
	 *          physical units (0 for no pre-smoothing)
	 * @param sigmaIntegration the Gaussian scale of the averaging window, in
	 *          physical units
	 * @param calibration the pixel spacing per dimension; defaults to 1 in every
	 *          dimension
	 * @return an (n+1)-dimensional image whose last dimension holds the n(n+1)/2
	 *         components {@code [J11, J12, ..., J1n, J22, ..., Jnn]}
	 * @implNote op names='filter.structureTensor'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		structureTensor( //
			final RandomAccessibleInterval<T> input, //
			final Double sigmaGradient, //
			final Double sigmaIntegration, //
			@Nullable final double[] calibration //
	) {
		return TensorUtils.structureTensor(input, sigmaGradient, sigmaIntegration,
			calibration);
	}

	/**
	 * Computes the eigenvalues of the structure tensor (see
	 * {@code filter.structureTensor}) at each pixel of an n-D image.
	 * <p>
	 * The structure tensor is positive semi-definite, so all eigenvalues are
	 * non-negative; they are sorted in descending order,
	 * {@code λ1 >= λ2 >= ... >= λn >= 0}. In 2D, {@code λ1 ≈ λ2 ≈ 0} indicates
	 * a flat region, {@code λ1 >> λ2 ≈ 0} an edge or oriented texture, and
	 * {@code λ1 ≈ λ2 >> 0} a corner or isotropic texture.
	 * </p>
	 * <p>
	 * This corresponds to the "structure tensor eigenvalues" feature of
	 * labkit-pixel-classification (Matthias Arzt, BSD-2-Clause) and the
	 * "Structure" feature of Trainable Weka Segmentation (via imagescience).
	 * </p>
	 *
	 * @param <T> the input type
	 * @param input the n-dimensional input image
	 * @param sigmaGradient the Gaussian scale for computing the gradient, in
	 *          physical units (0 for no pre-smoothing)
	 * @param sigmaIntegration the Gaussian scale of the averaging window, in
	 *          physical units
	 * @param calibration the pixel spacing per dimension; defaults to 1 in every
	 *          dimension
	 * @return an (n+1)-dimensional image whose last dimension holds the n
	 *         eigenvalues in descending order
	 * @implNote op names='filter.structureTensorEigenvalues'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		structureTensorEigenvalues( //
			final RandomAccessibleInterval<T> input, //
			final Double sigmaGradient, //
			final Double sigmaIntegration, //
			@Nullable final double[] calibration //
	) {
		final var tensor = TensorUtils.structureTensor(input, sigmaGradient,
			sigmaIntegration, calibration);
		return TensorUtils.eigenvalues(tensor, false);
	}

	/**
	 * Computes the shape index of a 2D image, treating intensity as a height
	 * map (Koenderink &amp; van Doorn 1992, "Surface shape and curvature
	 * scales", Image and Vision Computing 10(8)).
	 * <p>
	 * With {@code κ1 >= κ2} the eigenvalues of the Hessian at scale
	 * {@code sigma}, the shape index is
	 * {@code S = -(2/π) · atan((κ1 + κ2) / (κ1 - κ2))}, which lies in
	 * {@code [-1, 1]}: +1 for a bright spherical cap (local maximum), +0.5 for
	 * a bright ridge, 0 for a saddle, -0.5 for a dark valley and -1 for a dark
	 * spherical cup (local minimum). The shape index is undefined where the
	 * image is locally planar ({@code κ1 = κ2 = 0}); there the output is 0.
	 * </p>
	 * <p>
	 * This matches the sign convention of Fiji's {@code Shape_Index_Map}
	 * (Johannes Schindelin), but uses Gaussian-smoothed central differences
	 * rather than one-sided differences.
	 * </p>
	 *
	 * @param <T> the input type
	 * @param input the 2-dimensional input image
	 * @param sigma the Gaussian scale in physical units (0 for no smoothing)
	 * @param calibration the pixel spacing per dimension; defaults to 1 in every
	 *          dimension
	 * @return the shape index image
	 * @implNote op names='filter.shapeIndex'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		shapeIndex( //
			final RandomAccessibleInterval<T> input, //
			final Double sigma, //
			@Nullable final double[] calibration //
	) {
		TensorUtils.checkDimensions(input, 2, "filter.shapeIndex");
		final var hessian = TensorUtils.hessian(input, sigma, calibration, false);
		final var output = TensorUtils.createImg(input.dimensionsAsLongArray());
		final var factor = -2 / Math.PI;
		LoopBuilder.setImages(Views.collapse(hessian), output).multiThreaded()
			.forEachPixel((h, out) -> {
				final var hxx = h.get(0).getRealDouble();
				final var hxy = h.get(1).getRealDouble();
				final var hyy = h.get(2).getRealDouble();
				final var diff = hxx - hyy;
				// Note: κ1 + κ2 is the trace, and κ1 - κ2 is sqrt(diff² + 4 hxy²).
				final var s = factor * Math.atan((hxx + hyy) / Math.sqrt(diff * diff +
					4 * hxy * hxy));
				out.set(Double.isNaN(s) ? 0 : s);
			});
		return output;
	}

	/**
	 * Computes a sheetness (bright sheet / plate) measure of a 3D image from
	 * the eigenvalues of its Hessian matrix, following the sheet filter of
	 * Sato et al. (2000), "Tissue classification based on 3D local intensity
	 * structures for volume rendering", IEEE TVCG 6(2).
	 * <p>
	 * With {@code λ1 >= λ2 >= λ3} the eigenvalues of the scale-normalized
	 * Hessian ({@code sigma²·H}), the response is 0 unless {@code λ3 < 0}, and
	 * otherwise {@code |λ3| · ω(λ2, λ3) · ω(λ1, λ3)}, where
	 * </p>
	 * <ul>
	 * <li>{@code ω(λs, λt) = 1 + λs/|λt|} if {@code λt <= λs <= 0},</li>
	 * <li>{@code ω(λs, λt) = 1 - α·λs/|λt|} if {@code 0 < λs < |λt|/α},
	 * and</li>
	 * <li>{@code ω(λs, λt) = 0} otherwise.</li>
	 * </ul>
	 * <p>
	 * A bright plate thus gives {@code |λ3|}, while lines ({@code λ2 ≈ λ3}) and
	 * blobs ({@code λ1 ≈ λ2 ≈ λ3}) are suppressed to 0. This is the sheet
	 * counterpart of {@code filter.tubeness}.
	 * </p>
	 * <p>
	 * Note: Fiji's VIB "Surfaceness" plugin (Mark Longair) uses
	 * {@code sqrt(|λ3/λ2|) - 1} when both {@code λ2, λ3 < 0} (sorted by
	 * magnitude). That ratio is unbounded and vanishes at the center of an
	 * ideal plate (where {@code λ2 = 0}), so this op uses Sato's measure
	 * instead, under a different name.
	 * </p>
	 *
	 * @param <T> the input type
	 * @param input the 3-dimensional input image
	 * @param sigma the Gaussian scale in physical units (0 for no smoothing)
	 * @param alpha the asymmetry parameter α of the weight function, penalizing
	 *          positive (dark-side) curvature; defaults to 0.25
	 * @param calibration the pixel spacing per dimension; defaults to 1 in every
	 *          dimension
	 * @return the sheetness image
	 * @implNote op names='filter.sheetness'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		sheetness( //
			final RandomAccessibleInterval<T> input, //
			final Double sigma, //
			@Nullable Double alpha, //
			@Nullable final double[] calibration //
	) {
		TensorUtils.checkDimensions(input, 3, "filter.sheetness");
		if (alpha == null) alpha = 0.25;
		final double a = alpha;
		final var hessian = TensorUtils.hessian(input, sigma, calibration, true);
		final var evs = TensorUtils.eigenvalues(hessian, false);
		final var output = TensorUtils.createImg(input.dimensionsAsLongArray());
		LoopBuilder.setImages(Views.collapse(evs), output).multiThreaded()
			.forEachPixel((ev, out) -> {
				final var l1 = ev.get(0).getRealDouble();
				final var l2 = ev.get(1).getRealDouble();
				final var l3 = ev.get(2).getRealDouble();
				if (l3 >= 0) out.setZero();
				else out.set(-l3 * satoWeight(l2, l3, a) * satoWeight(l1, l3, a));
			});
		return output;
	}

	/**
	 * Computes the local orientation of a 2D image from its structure tensor,
	 * following the conventions of OrientationJ (Rezakhaniha et al. 2012,
	 * "Experimental investigation of collagen waviness and orientation in the
	 * arterial adventitia using confocal laser scanning microscopy", Biomech.
	 * Model. Mechanobiol. 11; Püspöki et al. 2016, "Transforms and operators
	 * for directional bioimage analysis: a survey", Adv. Anat. Embryol. Cell
	 * Biol. 219).
	 * <p>
	 * The result is the orientation of the dominant oriented structure (i.e.
	 * perpendicular to the dominant gradient direction), in radians within
	 * {@code [-π/2, π/2]}, measured counterclockwise from the x axis as the
	 * image is displayed (y axis pointing up on screen, i.e. opposite to
	 * increasing row index): {@code θ = ½·atan2(2·Jxy, Jyy - Jxx)}, where
	 * {@code J} is the structure tensor in image coordinates. Horizontal
	 * structures yield 0 and vertical structures ±π/2.
	 * </p>
	 * <p>
	 * Unlike OrientationJ, which offers cubic-spline and finite-difference
	 * gradients, the gradient is computed by central differences of the image
	 * smoothed at {@code sigmaGradient}.
	 * </p>
	 *
	 * @param <T> the input type
	 * @param input the 2-dimensional input image
	 * @param sigmaGradient the Gaussian scale for computing the gradient, in
	 *          physical units (0 for no pre-smoothing)
	 * @param sigmaIntegration the Gaussian scale of the averaging window
	 *          (the local window of OrientationJ), in physical units
	 * @param calibration the pixel spacing per dimension; defaults to 1 in every
	 *          dimension
	 * @return the orientation image, in radians
	 * @implNote op names='filter.orientation'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		orientation( //
			final RandomAccessibleInterval<T> input, //
			final Double sigmaGradient, //
			final Double sigmaIntegration, //
			@Nullable final double[] calibration //
	) {
		TensorUtils.checkDimensions(input, 2, "filter.orientation");
		final var tensor = TensorUtils.structureTensor(input, sigmaGradient,
			sigmaIntegration, calibration);
		final var output = TensorUtils.createImg(input.dimensionsAsLongArray());
		LoopBuilder.setImages(Views.collapse(tensor), output).multiThreaded()
			.forEachPixel((j, out) -> {
				final var jxx = j.get(0).getRealDouble();
				final var jxy = j.get(1).getRealDouble();
				final var jyy = j.get(2).getRealDouble();
				out.set(0.5 * Math.atan2(2 * jxy, jyy - jxx));
			});
		return output;
	}

	/**
	 * Computes the local coherency of a 2D image from its structure tensor,
	 * following the conventions of OrientationJ (see
	 * {@code filter.orientation}).
	 * <p>
	 * With {@code λ1 >= λ2} the eigenvalues of the structure tensor, the
	 * coherency is {@code (λ1 - λ2) / (λ1 + λ2)}
	 * {@code = sqrt((Jyy - Jxx)² + 4·Jxy²) / (Jxx + Jyy)}, which lies in
	 * {@code [0, 1]}: 1 for a perfectly oriented pattern, 0 for an isotropic
	 * one. Where the tensor vanishes (a constant region), the output is 0.
	 * </p>
	 *
	 * @param <T> the input type
	 * @param input the 2-dimensional input image
	 * @param sigmaGradient the Gaussian scale for computing the gradient, in
	 *          physical units (0 for no pre-smoothing)
	 * @param sigmaIntegration the Gaussian scale of the averaging window
	 *          (the local window of OrientationJ), in physical units
	 * @param calibration the pixel spacing per dimension; defaults to 1 in every
	 *          dimension
	 * @return the coherency image
	 * @implNote op names='filter.coherency'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		coherency( //
			final RandomAccessibleInterval<T> input, //
			final Double sigmaGradient, //
			final Double sigmaIntegration, //
			@Nullable final double[] calibration //
	) {
		TensorUtils.checkDimensions(input, 2, "filter.coherency");
		final var tensor = TensorUtils.structureTensor(input, sigmaGradient,
			sigmaIntegration, calibration);
		final var output = TensorUtils.createImg(input.dimensionsAsLongArray());
		LoopBuilder.setImages(Views.collapse(tensor), output).multiThreaded()
			.forEachPixel((j, out) -> {
				final var jxx = j.get(0).getRealDouble();
				final var jxy = j.get(1).getRealDouble();
				final var jyy = j.get(2).getRealDouble();
				final var trace = jxx + jyy;
				final var diff = jyy - jxx;
				out.set(trace > 0 ? Math.sqrt(diff * diff + 4 * jxy * jxy) / trace
					: 0);
			});
		return output;
	}

	/**
	 * Computes the Harris corner response of an n-D image (Harris &amp;
	 * Stephens 1988, "A combined corner and edge detector", Proc. 4th Alvey
	 * Vision Conference).
	 * <p>
	 * With {@code J} the structure tensor (see {@code filter.structureTensor}),
	 * the response is {@code R = det(J) - k·trace(J)ⁿ}, where n is the number
	 * of dimensions. In 2D this is the classic {@code det - k·trace²}; the
	 * exponent n keeps both terms of the same degree in higher dimensions (as
	 * in the 3D extension of Laptev 2005, "On space-time interest points",
	 * IJCV 64). Corners give large positive responses, edges negative ones and
	 * flat regions values near 0. Corner points can be extracted as local
	 * maxima of the response.
	 * </p>
	 * <p>
	 * Note: Fiji's VIB {@code Harris_Corner_Detector} (3D) computes the
	 * determinant with {@code gyy·gyy} where {@code gyy·gzz} is intended, and
	 * uses {@code trace²}; neither is replicated here.
	 * </p>
	 *
	 * @param <T> the input type
	 * @param input the n-dimensional input image
	 * @param sigmaGradient the Gaussian scale for computing the gradient, in
	 *          physical units (0 for no pre-smoothing)
	 * @param sigmaIntegration the Gaussian scale of the averaging window, in
	 *          physical units
	 * @param k the sensitivity parameter; defaults to 0.04 (typical values in
	 *          2D are 0.04 to 0.06; smaller values suit higher dimensions, e.g.
	 *          0.005 in 3D)
	 * @param calibration the pixel spacing per dimension; defaults to 1 in every
	 *          dimension
	 * @return the Harris corner response image
	 * @implNote op names='filter.harris'
	 */
	public static <T extends RealType<T>> RandomAccessibleInterval<DoubleType>
		harris( //
			final RandomAccessibleInterval<T> input, //
			final Double sigmaGradient, //
			final Double sigmaIntegration, //
			@Nullable Double k, //
			@Nullable final double[] calibration //
	) {
		if (k == null) k = 0.04;
		final double kk = k;
		final var n = input.numDimensions();
		final var tensor = TensorUtils.structureTensor(input, sigmaGradient,
			sigmaIntegration, calibration);
		final var evs = TensorUtils.eigenvalues(tensor, false);
		final var output = TensorUtils.createImg(input.dimensionsAsLongArray());
		LoopBuilder.setImages(Views.collapse(evs), output).multiThreaded()
			.forEachPixel((ev, out) -> {
				var det = 1.0;
				var trace = 0.0;
				for (var i = 0; i < n; i++) {
					final var v = ev.get(i).getRealDouble();
					det *= v;
					trace += v;
				}
				out.set(det - kk * Math.pow(trace, n));
			});
		return output;
	}

	/** Sato's weight function {@code ω(λs, λt)} with γ = 1. */
	private static double satoWeight(final double ls, final double lt,
		final double alpha)
	{
		final var abs = Math.abs(lt);
		if (lt <= ls && ls <= 0) return 1 + ls / abs;
		if (ls > 0 && alpha * ls < abs) return 1 - alpha * ls / abs;
		return 0;
	}
}
