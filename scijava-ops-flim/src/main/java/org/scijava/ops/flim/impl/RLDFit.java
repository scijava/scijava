
package org.scijava.ops.flim.impl;

import flimlib.FLIMLib;
import net.imglib2.type.numeric.RealType;
import org.scijava.ops.flim.AbstractFitRAI;
import org.scijava.ops.flim.FitParams;
import org.scijava.ops.flim.FitResults;
import org.scijava.ops.flim.FitWorker;

public class RLDFit {

	/**
	 * Fits a RAI
	 *
	 * @param <I>
	 * @implNote op names="flim.fitRLD"
	 */
	public static class RLDSingleFitRAI<I extends RealType<I>, K extends RealType<K>>
		extends AbstractFitRAI<I, K>
	{

		@Override
		public FitWorker<I> createWorker(FitParams<I> params, FitResults results) {
			return new RLDFitWorker<>(params, results);
		}
	}

	public static class RLDFitWorker<I extends RealType<I>> extends
		AbstractSingleFitWorker<I>
	{

		// RLD's own buffers
		private final float[] z, a, tau;

		public RLDFitWorker(FitParams<I> params, FitResults results) {
			super(params, results);
			z = new float[1];
			a = new float[1];
			tau = new float[1];
		}

		@Override
		protected void beforeFit() {
			super.beforeFit();
			// setup params
			z[0] = paramBuffer[0];
			a[0] = paramBuffer[1];
			tau[0] = paramBuffer[2];
		}

		/**
		 * Performs the RLD fit.
		 */
		@Override
		protected void doFit() {
			final int retCode = FLIMLib.GCI_triple_integral_fitting_engine(
				params.xInc, transBuffer, adjFitStart, adjFitEnd, params.instr,
				params.noise, params.sig, z, a, tau, fittedBuffer, residualBuffer,
				chisqBuffer, rawChisq_target);

			// -1: malloc failed
			if (retCode < 0) results.retCode = retCode == -1
				? FitResults.RET_INTERNAL_ERROR : FitResults.RET_UNKNOWN;
			else
				// non-negative: iteration count
				results.retCode = FitResults.RET_OK;
		}

		@Override
		protected void afterFit() {
			// Barber, P. R. et al. (2008). Multiphoton time-domain fluorescence
			// lifetime imaging microscopy: practical application to protein–protein
			// interactions using global analysis. Journal of The Royal Society
			// Interface, 6(suppl_1), S93-S105.
			if (params.paramFree[0]) {
				paramBuffer[0] = z[0];
			}
			if (params.paramFree[1]) {
				paramBuffer[1] = a[0];
			}
			if (params.paramFree[2]) {
				paramBuffer[2] = tau[0];
			}
			// splitting a and tau across components. This is how TRI2 does it. See:
			if (params.nComp >= 2) {
				if (params.paramFree[1]) {
					paramBuffer[1] = a[0] * 3 / 4;
				}
				if (params.paramFree[3]) {
					paramBuffer[3] = a[0] * 1 / 4;
				}
				if (params.paramFree[4]) {
					paramBuffer[4] = tau[0] * 2 / 3;
				}
			}
			if (params.nComp >= 3) {
				if (params.paramFree[3]) {
					paramBuffer[3] = a[0] * 1 / 6;
				}
				if (params.paramFree[5]) {
					paramBuffer[5] = a[0] * 1 / 6;
				}
				if (params.paramFree[6]) {
					paramBuffer[6] = tau[0] * 1 / 3;
				}
			}
			if (params.nComp >= 4) {
				// doesn't really matter, used estimation for global
				// see flimlib:flimlib/src/main/c/EcfGlobal.c
				for (int i = 7; i < nParam; i += 2) {
					if (params.paramFree[i]) {
						paramBuffer[i] = a[0] / i;
					}
					if (params.paramFree[i]) {
						paramBuffer[i] = tau[0] / i;
					}
				}
			}
			super.afterFit();
		}

		@Override
		protected AbstractSingleFitWorker<I> duplicate(FitParams<I> params,
			FitResults rslts)
		{
			return new RLDFitWorker<>(params, rslts);
		}
	}

}
