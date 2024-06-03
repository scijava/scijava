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
import flimlib.Float2DMatrix;
import net.imglib2.type.numeric.RealType;
import org.scijava.ops.flim.AbstractFitRAI;
import org.scijava.ops.flim.FitParams;
import org.scijava.ops.flim.FitResults;
import org.scijava.ops.flim.FitWorker;

public class LMAFit {

	/**
	 * Fits a RAI
	 *
	 * @param <I>
	 * @implNote op names="flim.fitLMA"
	 */
	public static class LMASingleFitRAI<I extends RealType<I>, K extends RealType<K>>
		extends AbstractFitRAI<I, K>
	{

		@Override
		public FitWorker<I> createWorker(FitParams<I> params, FitResults results) {
			return new LMAFitWorker<>(params, results);
		}
	}

	public static class LMAFitWorker<I extends RealType<I>> extends
		AbstractSingleFitWorker<I>
	{

		// reusable buffers
		private final Float2DMatrix covar, alpha, erraxes;

		private final RLDFit.RLDFitWorker<I> estimatorWorker;

		public LMAFitWorker(FitParams<I> params, FitResults results) {
			super(params, results);
			covar = new Float2DMatrix(nParam, nParam);
			alpha = new Float2DMatrix(nParam, nParam);
			erraxes = new Float2DMatrix(nParam, nParam);
			// in case both param and paramMap are not set
			estimatorWorker = new RLDFit.RLDFitWorker<>(params, results);
		}

		@Override
		protected void beforeFit() {
			// needs RLD estimation
			for (float param : paramBuffer) {
				// no estimation (+Inf was set by RAHelper#loadData)
				if (param == Float.POSITIVE_INFINITY) {
					estimatorWorker.fitSingle();
					break;
				}
			}
			super.beforeFit();
		}

		/**
		 * Performs an LMA fit.
		 */
		@Override
		public void doFit() {
			final int retCode = FLIMLib.GCI_marquardt_fitting_engine(params.xInc,
				transBuffer, adjFitStart, adjFitEnd, params.instr, params.noise,
				params.sig, paramBuffer, params.paramFree, params.restrain,
				params.fitFunc, fittedBuffer, residualBuffer, chisqBuffer, covar, alpha,
				erraxes, rawChisq_target, params.chisq_delta, params.chisq_percent);

			switch (retCode) {
				case -1: // initial estimation failed
				case -2: // max iteration reached before converge
				case -3: // iteration failed
				case -4: // final iteration failed
				case -5: // error estimation failed
					results.retCode = FitResults.RET_BAD_FIT_DIVERGED;
					break;

				default: // non-negative: iteration count
					results.retCode = retCode >= 0 ? FitResults.RET_OK
						: FitResults.RET_UNKNOWN;
					break;
			}
		}

		@Override
		protected AbstractSingleFitWorker<I> duplicate(FitParams<I> params,
			FitResults rslts)
		{
			return new LMAFitWorker<>(params, rslts);
		}
	}
}
