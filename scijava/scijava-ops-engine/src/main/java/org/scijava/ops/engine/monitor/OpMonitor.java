/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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
package org.scijava.ops.engine.monitor;

import java.util.concurrent.CancellationException;

import org.scijava.ops.engine.log.Logger;
import org.scijava.ops.spi.Op;

/**
 * Used as a bridge between the Op and the user. Allows the user to
 * cancel any Op as well as allowing the Op to notify the user of the Op's
 * progress and of any warnings encountered through the course of computation.
 * 
 * @author Gabriel Selzer
 * @author Marcel Wiedenmann
 */
public interface OpMonitor {

	/**
	 * Checks if the user has canceled computation.
	 * 
	 * @return true if the user has canceled computation.
	 */
	boolean isCanceled();

	/**
	 * Throws an {@link CancellationException} if the user has canceled
	 * computation.
	 */
	default void checkCanceled() {
		if (isCanceled())
			throw new CancellationException("The Op was canceled before it was able to complete computation");
	}

	/**
	 * If called by the user, computation is canceled.
	 */
	void cancel();

	/**
	 * Returns a {@link Logger} for use by the Op.
	 * 
	 * @return a {@link Logger}.
	 */
	Logger logger();

	/**
	 * Sets the progress of the computation monitored by this {@link OpMonitor}
	 * 
	 * @param progress
	 *            - the progress <i>p</i> of the Op's computation. 0 &lt;=
	 *            <i>p</i> &lt;= 1
	 */
	void setProgress(double progress);

	/**
	 * Returns the completion progress of the Op's computation as a decimal.
	 * 
	 * @return progress - the progress <i>p</i> of the Op's computation. 0
	 *         &lt;= <i>p</i> &lt;= 1
	 */
	double getProgress();

	// OpMonitor createSubMonitor();

}
