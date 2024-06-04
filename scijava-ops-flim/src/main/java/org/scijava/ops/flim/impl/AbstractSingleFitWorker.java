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

import net.imglib2.type.numeric.RealType;
import org.scijava.concurrent.Parallelization;
import org.scijava.ops.flim.FitParams;
import org.scijava.ops.flim.FitResults;
import org.scijava.ops.flim.util.RAHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractSingleFitWorker<I extends RealType<I>> extends
	AbstractFitWorker<I>
{

	/** Data buffers, all except for {@code transBuffer} are writable */
	protected final float[] paramBuffer, transBuffer, chisqBuffer, fittedBuffer,
			residualBuffer;

	public AbstractSingleFitWorker(FitParams<I> params, FitResults results) {
		super(params, results);

		// setup input buffers
		if (results.param == null || results.param.length != nParam) {
			results.param = new float[nParam];
		}
		if (params.trans == null || params.trans.length != nDataTotal) {
			params.trans = new float[nDataTotal];
		}
		paramBuffer = results.param;
		transBuffer = params.trans;

		// setup output buffers
		chisqBuffer = new float[1];
		if (results.fitted == null || results.fitted.length != nDataTotal) {
			results.fitted = new float[nDataTotal];
		}
		if (results.residuals == null || results.residuals.length != nDataTotal) {
			results.residuals = new float[nDataTotal];
		}
		fittedBuffer = results.fitted;
		residualBuffer = results.residuals;
	}

	/**
	 * A routine called before {@link #doFit()}. Can be used to throw away the
	 * left-overs from the previous run.
	 */
	protected void beforeFit() {
		chisqBuffer[0] = -1;
	}

	/**
	 * Does the actual implementation-specific fitting routine.
	 */
	protected abstract void doFit();

	/**
	 * A routine called after {@link #doFit()}. Can be used to copy back results
	 * from buffers.
	 */
	protected void afterFit() {
		// reduced by degree of freedom
		results.chisq = chisqBuffer[0] / (nData - nParam);
	}

	/**
	 * Fit the data in the buffer.
	 */
	public void fitSingle() {
		beforeFit();

		doFit();

		afterFit();
	}

	/**
	 * Make a worker of the same kind but does not share any writable buffers
	 * (thread safe) if that buffer is null.
	 *
	 * @param params the parameters
	 * @param rslts the results
	 * @return a worker of the same kind.
	 */
	protected abstract AbstractSingleFitWorker<I> duplicate(FitParams<I> params,
		FitResults rslts);

	/**
	 * Called on the worker thread after worker duplication. Can be used to
	 * initialize thread-local globals such as Bayesian search grid parameters.
	 *
	 * @see BayesFit.BayesFitWorker#onThreadInit()
	 */
	protected void onThreadInit() {}

	protected boolean runMultiThreaded() {
		return params.multithread;
	}

	@Override
	public void fitBatch(List<int[]> pos, FitEventHandler<I> handler) {
		final AbstractSingleFitWorker<I> thisWorker = this;

		Consumer<int[]> worker = (data) -> {
			int start = data[0];
			int size = data[1];
			if (!runMultiThreaded()) {
				// let the first fitting thread do all the work
				if (start != 0) {
					return;
				}
				size = pos.size();
			}

			// thread-local reusable read/write buffers
			final FitParams<I> lParams;
			final FitResults lResults;
			final AbstractSingleFitWorker<I> fitWorker;
			// don't make copy in single thread mode
			if (!runMultiThreaded() || pos.size() == 1) {
				lParams = params;
				lResults = results;
				fitWorker = thisWorker;
			}
			else {
				lParams = params.copy();
				lResults = results.copy();
				// grab your own buffer
				lParams.param = lParams.trans = lResults.param = lResults.fitted =
					lResults.residuals = null;
				fitWorker = duplicate(lParams, lResults);
			}
			fitWorker.onThreadInit();

			final RAHelper<I> helper = new RAHelper<>(params, results);

			for (int i = start; i < start + size; i++) {
				final int[] xytPos = pos.get(i);

				if (!helper.loadData(fitWorker.transBuffer, fitWorker.paramBuffer,
					params, xytPos)) lResults.retCode =
						FitResults.RET_INTENSITY_BELOW_THRESH;
				else {
					fitWorker.fitSingle();

					// invalidate fit if chisq is insane
					final float chisq = lResults.chisq;
					if (params.dropBad && lResults.retCode == FitResults.RET_OK &&
						(chisq < 0 || chisq > 1E5 || Float.isNaN(chisq))) lResults.retCode =
							FitResults.RET_BAD_FIT_CHISQ_OUT_OF_RANGE;
				}

				helper.commitRslts(lParams, lResults, xytPos);

				if (handler != null) handler.onSingleComplete(xytPos, params, results);
			}
		};

		// The idea for parallelization is as follows. We have t = pos.size() pixels
		// and n tasks that must together iterate over them all. Let s be the
		// rounded quotient ((int) t / n) and r be the remainder (t mod n).
		// Each task should thus be responsible for at least s pixels, with the
		// first r tasks also assuming an additional pixel.
		int n = Parallelization.getTaskExecutor().suggestNumberOfTasks();
		int s = pos.size() / n;
		int r = pos.size() % n;

		List<int[]> list = new ArrayList<>(n);
		int start = 0;
		// Initial size is s + 1
		int size = s + 1;
		for (int i = 0; i < n; i++) {
			// The first r tasks take an additional pixel - the remaining tasks
			// should only take s pixels
			if (i == r) {
				size--;
			}
			list.add(new int[] { start, size });
			start += size;
		}

		Parallelization.getTaskExecutor().forEach(list, worker);
		if (handler != null) handler.onComplete(params, results);
	}
}
