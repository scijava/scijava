/*-
 * #%L
 * Fluorescence lifetime analysis in ImageJ.
 * %%
 * Copyright (C) 2017 - 2022 Board of Regents of the University of Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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

public abstract class AbstractFitRAI<I extends RealType<I>, K extends RealType<K>>
	implements
	Functions.Arity4<FitParams<I>, RealMask, RandomAccessibleInterval<K>, FitWorker.FitEventHandler<I>, FitResults>
{

	@OpDependency(name = "filter.convolve")
	private Functions.Arity3<RandomAccessibleInterval<I>, RandomAccessibleInterval<K>, I, RandomAccessibleInterval<I>> convolveOp;

	private RandomAccessibleInterval<K> kernel;

	private RealMask roi;

	private FitWorker<I> fitWorker;

	private int lifetimeAxis;

	private ParamEstimator<I> est;

	private List<int[]> roiPos;

	private FitParams<I> params;

	private FitResults rslts;

	public void assertConformity(final FitParams<I> in) {
		// requires a 3D image
		if (in.transMap.numDimensions() != 3) {
			throw new IllegalArgumentException(
				"Fitting requires 3-dimensional input");
		}
		// lifetime axis must be valid
		lifetimeAxis = in.ltAxis;
		if (lifetimeAxis < 0 || lifetimeAxis >= in.transMap.numDimensions()) {
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

	public void initialize(FitParams<I> in) {

		// dimension doesn't really matter
		if (roi == null) {
			roi = Masks.allRealMask(0);
		}

		// So that we bin the correct axis
		if (kernel != null) {
			kernel = Views.permute(kernel, 2, lifetimeAxis);
		}

		params = in.copy();
		initParam();
		rslts = new FitResults();
		fitWorker = createWorker(params, rslts);
		initRslt();
	}

	/**
	 * @param params the {@link FitParams} used for fitting
	 * @param mask a {@link RealMask} defining the areas to fit
	 * @param kernel kernel used in an optional convolution preprocessing step
	 * @param handler
	 * @return the results of fitting
	 */
	@Override
	public FitResults apply(FitParams<I> params, @Nullable RealMask mask,
		@Nullable RandomAccessibleInterval<K> kernel,
		@Nullable FitWorker.FitEventHandler<I> handler)
	{
		this.kernel = kernel;
		this.roi = mask;
		assertConformity(params);
		initialize(params);
		fitWorker.fitBatch(roiPos, handler);
		return rslts;
	}

	/**
	 * Generates a worker for the actual fit.
	 *
	 * @return A {@link FitWorker}.
	 */
	public abstract FitWorker<I> createWorker(FitParams<I> params,
		FitResults results);

	private void initParam() {
		// convolve the image if necessary
		if (kernel != null) {
			params.transMap = convolveOp.apply( //
				params.transMap, //
				kernel, //
				Util.getTypeFromInterval(params.transMap));
		}

		roiPos = getRoiPositions(params.transMap);

		est = new ParamEstimator<>(params, roiPos);
		est.estimateStartEnd();
		est.estimateIThreshold();
	}

	private void initRslt() {
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

	private List<int[]> getRoiPositions(RandomAccessibleInterval<I> trans) {
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
