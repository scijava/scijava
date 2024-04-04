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

package org.scijava.ops.flim.impl;

import flimlib.FLIMLib;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ops.flim.AbstractFitRAI;
import org.scijava.ops.flim.FitParams;
import org.scijava.ops.flim.FitResults;
import org.scijava.ops.flim.FitWorker;
import org.scijava.ops.spi.OpDependency;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BayesFit {

	/**
	 * Fits a RAI
	 *
	 * @param <I>
	 * @implNote op names="flim.fitBayes"
	 */
	public static class BayesSingleFitRAI<I extends RealType<I>, K extends RealType<K>>
		extends AbstractFitRAI<I, K>
	{

		@OpDependency(name = "flim.fitRLD")
		private Function<FitParams<I>, FitResults> rldFitter;

		@OpDependency(name = "stats.percentile")
		private BiFunction<Img<FloatType>, Integer, FloatType> percentileOp;

		@Override
		public FitWorker<I> createWorker(FitParams<I> params, FitResults results) {
			return new BayesFitWorker<>(params, results, rldFitter, percentileOp);
		}
	}

	public static class BayesFitWorker<I extends RealType<I>> extends
		AbstractSingleFitWorker<I>
	{

		// Bayes's own buffers
		private final float[] error, minusLogProb;
		private final int[] nPhotons;
		private final Function<FitParams<I>, FitResults> rldFitter;
		private final BiFunction<Img<FloatType>, Integer, FloatType> percentileOp;

		private float laserPeriod;

		private float[] gridMin, gridMax;

		public BayesFitWorker( //
			FitParams<I> params, //
			FitResults results, //
			Function<FitParams<I>, FitResults> rldFitter, //
			BiFunction<Img<FloatType>, Integer, FloatType> percentileOp //
		) {
			super(params, results);
			this.rldFitter = rldFitter;
			this.percentileOp = percentileOp;

			if (nParam != 3) throw new IllegalArgumentException(
				"Bayesian analysis is currently single-component (3 parameters) only");

			error = new float[nParam];
			minusLogProb = new float[1];
			nPhotons = new int[1];

			if (gridMin == null || gridMax == null) estimateGrid();
		}

		private void estimateGrid() {
			gridMin = new float[nParam];
			gridMax = new float[nParam];

			FitParams<I> copyParams = params.copy();
			copyParams.getChisqMap = true;
			copyParams.param = null;
			FitResults estResults = rldFitter.apply(params);
			Img<FloatType> paramMap = estResults.paramMap;
			Img<FloatType> chisqMap = estResults.chisqMap;

			float chisqCutoff = percentileOp.apply(chisqMap, 20).getRealFloat();

			// calculate mean and std (exluding Inf and NaN)
			for (int i = 0; i <= paramMap.max(params.ltAxis); i++) {
				double mean = 0;
				double std = 0;
				double count = 0;

				IntervalView<FloatType> paramPlane = Views.hyperSlice(paramMap,
					params.ltAxis, i);
				Cursor<FloatType> ppCursor = paramPlane.cursor();
				Cursor<FloatType> xmCursor = chisqMap.cursor();

				// calculate the mean and std of best 20% fit
				while (ppCursor.hasNext()) {
					float pf = ppCursor.next().getRealFloat();
					float xf = xmCursor.next().getRealFloat();
					if (xf <= chisqCutoff && Float.isFinite(pf)) {
						mean += pf;
						std += pf * pf;
						count++;
					}
				}
				mean /= count;
				std /= count;

				// Global will give a std of 0 for taus
				double tauStdCompensation = (i == 2 || i == 4) ? 10 : 0;
				std = Math.sqrt(std - mean * mean + tauStdCompensation);

				// min[i] = (float) Math.max(mean - std, 0);
				gridMin[i] = 0;
				gridMax[i] = (float) Math.max(mean + std * 2, 0);
			}
		}

		@Override
		protected void beforeFit() {
			super.beforeFit();
			// TODO: expose as a parameter
			laserPeriod = params.xInc * (adjFitEnd - adjFitStart);
		}

		/**
		 * Performs an Bayes fit.
		 */
		@Override
		public void doFit() {
			final int retCode = FLIMLib.Bayes_fitting_engine(params.xInc, transBuffer,
				adjFitStart, adjFitEnd, laserPeriod, params.instr, paramBuffer,
				params.paramFree, fittedBuffer, residualBuffer, error, minusLogProb,
				nPhotons, chisqBuffer);

			switch (retCode) {
				case -1: // Bayes: Invalid data
				case -2: // Bayes: Invalid data window
				case -3: // Bayes: Invalid model
				case -4: // Bayes: Functionality not supported
				case -5: // Bayes: Invalid fixed parameter value
				case -6: // Bayes: All parameter values are fixed
				case -8: // Bayes: No rapid grid for parameter estimation
				case -14: // Bayes: Insufficient gridimation failure
					results.retCode = FitResults.RET_BAD_SETTING;
					break;

				case -7: // Bayes: Parameter estError in Ave & Errs
				case -9: // Bayes: Model selection parameter estimation failure
				case -10: // Bayes: Model selection Hessian error
				case -11: // Bayes: w max not found, pdf too sharp, too many counts?
				case -12: // Bayes: Error in Ave & Errs (MP Vals only)
				case -13: // Bayes: Error in Ave & Errs
				case -99: // BAYES__RESULT_USER_CANCEL
					results.retCode = FitResults.RET_BAD_FIT_DIVERGED;
					break;

				default:
					results.retCode = retCode >= 0 ? FitResults.RET_OK
						: FitResults.RET_UNKNOWN;
					break;
			}
		}

		protected void onThreadInit() {
			// grid settings are thread-local globals, which must be initialized on
			// the worker thread
			FLIMLib.Bayes_set_search_grid(gridMin, gridMax);
		}

		@Override
		protected AbstractSingleFitWorker<I> duplicate(FitParams<I> params,
			FitResults rslts)
		{
			// child will inherit the estimated grid config
			BayesFitWorker<I> child = new BayesFitWorker<>(params, rslts, rldFitter,
				percentileOp);
			child.gridMin = this.gridMin;
			child.gridMax = this.gridMax;
			return child;
		}

		@Override
		protected boolean runMultiThreaded() {
			// TODO: Multithreaded execution runs worse, so we force single-threaded
			// computation
			return false;
		}
	}

}
