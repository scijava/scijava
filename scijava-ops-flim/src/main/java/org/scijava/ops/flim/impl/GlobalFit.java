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
import flimlib.FitType;
import flimlib.Float2DMatrix;
import net.imglib2.type.numeric.RealType;
import org.scijava.ops.flim.AbstractFitRAI;
import org.scijava.ops.flim.FitWorker;
import org.scijava.ops.flim.util.RAHelper;
import org.scijava.ops.flim.FitParams;
import org.scijava.ops.flim.FitResults;

import java.util.List;

public class GlobalFit {

	/**
	 * Fits a RAI
	 *
	 * @param <I>
	 * @implNote op names="flim.fitGlobal"
	 */
	public static class GlobalSingleFitRAI<I extends RealType<I>, K extends RealType<K>>
		extends AbstractFitRAI<I, K>
	{

		@Override
		public FitWorker<I> createWorker(FitParams<I> params, FitResults results) {
			return new GlobalFit.GlobalFitWorker<>(params, results);
		}
	}

	public static class GlobalFitWorker<I extends RealType<I>> extends
		AbstractFitWorker<I>
	{

		public GlobalFitWorker(FitParams<I> params, FitResults results) {
			super(params, results);
		}

		@Override
		public void fitBatch(List<int[]> pos, FitEventHandler<I> handler) {
            var nTrans = pos.size();

			// trans data and fitted parameters for each trans
			final var trans = new float[nTrans][nDataTotal];
			final var param = new float[nTrans][nParam];
			final var transSkipped = new boolean[nTrans];

			final var helper = new RAHelper<I>(params, results);

			// fetch parameters from RA
			for (var i = 0; i < nTrans; i++)
				transSkipped[i] = !helper.loadData(trans[i], param[i], params, pos.get(
					i));

			// each row is a transient series
            var transMat = new Float2DMatrix(trans);
			// each row is a parameter series
            var paramMat = new Float2DMatrix(param);
			// only the first row is used
            var fittedMat = new Float2DMatrix(1, nDataTotal);
            var residualMat = new Float2DMatrix(1, nDataTotal);
			// $\chi^2$ for each trans
            var chisq = new float[nTrans];
			// global $\chi^2$
            var chisqGlobal = new float[1];
			// degrees of freedom (used to reduce $\chi^2$)
            var df = new int[1];

			final var retCode = FLIMLib.GCI_marquardt_global_exps_instr(params.xInc,
				transMat, adjFitStart, adjFitEnd, params.instr, params.noise,
				params.sig, FitType.FIT_GLOBAL_MULTIEXP, paramMat, params.paramFree,
				params.restrain, params.chisq_delta, fittedMat, residualMat, chisq,
				chisqGlobal, df, params.dropBad ? 1 : 0);

			// fetch fitted stuff from native
            var fittedParam = params.getParamMap ? paramMat.asArray() : null;
            var fitted = params.getFittedMap ? fittedMat.asArray() : null;
            var residual = params.getResidualsMap ? residualMat.asArray()
				: null;

			// copy back
			for (var i = 0; i < nTrans; i++) {
				results.param = params.getParamMap ? fittedParam[i] : null;
				results.fitted = params.getFittedMap ? fitted[i] : null;
				results.residuals = params.getResidualsMap ? residual[i] : null;
				results.chisq = chisq[i];
				results.retCode = transSkipped[i]
					? FitResults.RET_INTENSITY_BELOW_THRESH : convertRetCode(retCode);

				if (params.dropBad && results.retCode == FitResults.RET_OK) {
					// GCI_marquardt_global_exps_calculate_exps_instr fills chisq with -1
					// if
					// drop_bad_transients is true and
					// GCI_marquardt_global_exps_do_fit_single fails
					if (results.chisq < 0) results.retCode =
						FitResults.RET_BAD_FIT_DIVERGED;
					else if (Float.isNaN(results.chisq) || results.chisq > 1E5)
						results.retCode = FitResults.RET_BAD_FIT_CHISQ_OUT_OF_RANGE;
				}

				helper.commitRslts(params, results, pos.get(i));
			}
			results.chisq = chisqGlobal[0];

			if (handler != null) handler.onComplete(params, results);
		}

		/**
		 * Roughly categorize return code from
		 * {@link FLIMLib#GCI_marquardt_global_exps_instr}. "Roughly" in the sense
		 * that some of the recutrn code are hard to trace while others overlap.
		 *
		 * @param retCode return code from
		 *          <code>GCI_marquardt_global_exps_instr</code>
		 * @return return code defined in {@link FitResults}
		 */
		private int convertRetCode(final int retCode) {
			int convertedretCode;
			switch (retCode) {
				case -1: // bad parameter
				case -12: // bad fit type
				case -21: // bad fit type
				case -22: // bad fit type in
					// GCI_marquardt_global_exps_calculate_exps_instr
				case -31: // bad fit type in GCI_marquardt_global_exps_do_fit_instr
				case -32: // bad fit type in GCI_marquardt_global_exps_do_fit_instr
					convertedretCode = FitResults.RET_BAD_SETTING;
					break;

				case -2: // malloc failed
				case -3: // malloc failed
				case -4: // malloc failed
				case -5: // malloc failed
				case -11: // calloc failed
					convertedretCode = FitResults.RET_INTERNAL_ERROR;
					break;

				case -13: // initial MLA failed
					convertedretCode = FitResults.RET_BAD_FIT_DIVERGED;
					break;

				default: // non-negative: iteration count
					convertedretCode = retCode >= 0 ? FitResults.RET_OK
						: FitResults.RET_UNKNOWN;
					break;
			}
			return convertedretCode;
		}
	}

}
