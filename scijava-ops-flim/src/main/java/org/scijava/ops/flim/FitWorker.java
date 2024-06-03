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

import net.imglib2.type.numeric.RealType;
import org.scijava.ops.flim.impl.LMAFit;
import org.scijava.ops.flim.impl.PhasorFit;

import java.util.List;

public interface FitWorker<I extends RealType<I>> {

	/**
	 * The handler interface for fit events.
	 *
	 * @param <I> The parameter type
	 */
	public interface FitEventHandler<I extends RealType<I>> {

		/**
		 * The handler called by {@link FitWorker}s upon completion of all fits.
		 *
		 * @param params the params
		 * @param results the results
		 */
		default void onComplete(FitParams<I> params, FitResults results) {}

		/**
		 * The handler called by {@link FitWorker}s upon completion of a single fit.
		 *
		 * @param pos the x, y coordinate of the trans being fitted
		 * @param params the params (volatile) of the completed fit
		 * @param results the results (volatile) from the completed fit
		 */
		default void onSingleComplete(int[] pos, FitParams<I> params,
			FitResults results)
		{}
	}

	/**
	 * How many parameters should there be in {@code results.param}? E.g. 3 for
	 * one-component {@link LMAFit.LMAFitWorker} and 5 for
	 * {@link PhasorFit.PhasorFitWorker}.
	 *
	 * @return The number of output parameters in the parameter array.
	 */
	int nParamOut();

	/**
	 * How many bins will be fitted?
	 *
	 * @return {@code fitEnd - fitStart}
	 */
	int nDataOut();

	;

	/**
	 * Fit all coordinates listed and handles fit events.
	 *
	 * @param pos the coordinates of trans to fit
	 * @param handler the fit event handler
	 */
	void fitBatch(List<int[]> pos, FitEventHandler<I> handler);
}
