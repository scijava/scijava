/*-
 * #%L
 * Fluorescence lifetime analysis in SciJava Ops.
 * %%
 * Copyright (C) 2024 SciJava developers.
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

package org.scijava.ops.flim;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.Masks;
import net.imglib2.roi.RealMask;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared base class for all FLIM fitting Ops
 *
 * @param <I> element type of the input image
 * @param <K> element type of the kernel
 * @author Dasong Gao
 * @author Gabriel Selzer
 */
public abstract class AbstractFitRAI<I extends RealType<I>, K extends RealType<K>>
	implements
	Functions.Arity4<FitParams<I>, RealMask, RandomAccessibleInterval<K>, FitWorker.FitEventHandler<I>, FitResults>
{

	@OpDependency(name = "filter.convolve")
	private Functions.Arity3<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, I, RandomAccessibleInterval<I>> convolveOp;

	/**
	 * @param params the {@link FitParams} used for fitting
	 * @param mask a {@link RealMask} defining the areas to fit
	 * @param kernel kernel used in an optional convolution preprocessing step
	 * @param handler a {@link FitWorker.FitEventHandler} allowing for callback
	 *          once computation has completed
	 * @return the results of fitting
	 */
	@Override
	public FitResults apply( //
		FitParams<I> params, //
		@Nullable RealMask mask, //
		@Nullable RandomAccessibleInterval<K> kernel, //
		@Nullable FitWorker.FitEventHandler<I> handler //
	) {
		assertConformity(params, mask, kernel);

		// Assign reasonable defaults for nullable params
		if (mask == null) {
			mask = Masks.allRealMask(0);
		}
		if (kernel != null) {
			kernel = Views.permute(kernel, 2, params.ltAxis);
		}

		// -- Initialize -- //
		params = params.copy(); // TODO: Is this necessary
		// convolve the image if necessary
		if (kernel != null) {
			params.transMap = convolveOp.apply( //
				params.transMap, //
				kernel, //
				Util.getTypeFromInterval(params.transMap));
		}
		List<int[]> roiPos = getRoiPositions(mask, params.ltAxis, params.transMap);

		ParamEstimator<I> est = new ParamEstimator<>(params, roiPos);
		est.estimateStartEnd();
		est.estimateIThreshold();
		FitResults rslts = new FitResults();
		FitWorker<I> fitWorker = createWorker(params, rslts);
		initRslt(params, fitWorker, est, rslts);

		// -- Run -- //
		fitWorker.fitBatch(roiPos, handler);
		return rslts;
	}

	private void assertConformity( //
		final FitParams<I> in, //
		final RealMask roi, //
		final RandomAccessibleInterval<K> kernel //
	) {
		// requires a 3D image
		if (in.transMap.numDimensions() != 3) {
			throw new IllegalArgumentException(
				"Fitting requires 3-dimensional input");
		}
		// lifetime axis must be valid
		if (in.ltAxis < 0 || in.ltAxis >= in.transMap.numDimensions()) {
			throw new IllegalArgumentException("Lifetime axis must be 0, 1, or 2");
		}

		// and possibly a 2D mask
		if (roi != null && roi.numDimensions() != 2) {
			throw new IllegalArgumentException("Mask must be 2-dimensional");
		}

		// and possibly a 3D kernel
		if (kernel != null && kernel.numDimensions() != 3) {
			throw new IllegalArgumentException("Kernel must be 3-dimensional");
		}
	}

	/**
	 * Generates a worker for the actual fit.
	 *
	 * @return A {@link FitWorker}.
	 */
	public abstract FitWorker<I> createWorker(FitParams<I> params,
		FitResults results);

	private void initRslt( //
		FitParams<I> params, //
		FitWorker<I> fitWorker, //
		ParamEstimator<I> est, //
		FitResults rslts //
	) {
		int lifetimeAxis = params.ltAxis;
		// get dimensions and replace time axis with decay parameters
		long[] dimFit = new long[params.transMap.numDimensions()];
		params.transMap.dimensions(dimFit);
		if (params.getParamMap) {
			dimFit[lifetimeAxis] = fitWorker.nParamOut();
			rslts.paramMap = ArrayImgs.floats(dimFit);
		}
		if (params.getFittedMap) {
			dimFit[lifetimeAxis] = fitWorker.nDataOut();
			rslts.fittedMap = ArrayImgs.floats(dimFit);
		}
		if (params.getResidualsMap) {
			dimFit[lifetimeAxis] = fitWorker.nDataOut();
			rslts.residualsMap = ArrayImgs.floats(dimFit);
		}
		if (params.getReturnCodeMap) {
			dimFit[lifetimeAxis] = 1;
			rslts.retCodeMap = ArrayImgs.ints(dimFit);
		}
		if (params.getChisqMap) {
			dimFit[lifetimeAxis] = 1;
			rslts.chisqMap = ArrayImgs.floats(dimFit);
		}
		rslts.ltAxis = lifetimeAxis;

		rslts.intensityMap = est.getIntensityMap();
	}

	private List<int[]> getRoiPositions(RealMask roi, int lifetimeAxis,
		RandomAccessibleInterval<I> trans)
	{
		final List<int[]> interested = new ArrayList<>();
		final IntervalView<I> xyPlane = Views.hyperSlice(trans, lifetimeAxis, 0);
		final Cursor<I> xyCursor = xyPlane.localizingCursor();

		// work to do
		while (xyCursor.hasNext()) {
			xyCursor.fwd();
			if (roi.test(xyCursor)) {
				int[] pos = new int[3];
				xyCursor.localize(pos);
				// swap in lifetime axis
				for (int i = 2; i > lifetimeAxis; i--) {
					int tmp = pos[i];
					pos[i] = pos[i - 1];
					pos[i - 1] = tmp;
				}
				pos[lifetimeAxis] = 0;
				interested.add(pos);
			}
		}
		return interested;
	}
}
