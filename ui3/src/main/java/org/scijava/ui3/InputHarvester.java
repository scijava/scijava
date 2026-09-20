/*
 * #%L
 * Toolkit-agnostic contracts for widgets and input harvesting.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.ui3;

import org.scijava.execute.Execution;
import org.scijava.execute.Preprocessor;
import org.scijava.harvest.ParameterModel;
import org.scijava.priority.Priority;

/**
 * Asks the user for the inputs a run still needs.
 * <p>
 * It is a {@link Preprocessor}, so a run harvests its inputs simply by going
 * through a {@link org.scijava.execute.Runner} whose chain contains one. A
 * user who dismisses the dialog {@linkplain Execution#decline declines} the
 * run, which is an ordinary outcome and not an error.
 * </p>
 * <p>
 * It runs last among preprocessors, so that everything else - filled-in
 * services, values supplied by the caller, preconditions - has already had its
 * say and the user is asked only for what remains.
 * </p>
 *
 * @author Curtis Rueden
 */
public abstract class InputHarvester implements Preprocessor {

	@Override
	public void process(final Execution execution) {
		final ParameterModel model = new ParameterModel(execution.executable());
		if (model.tree().nodes().isEmpty()) return; // nothing left to ask
		if (!harvest(model)) execution.decline("Canceled by user");
	}

	/**
	 * Puts the given parameters in front of the user and waits for an answer.
	 * <p>
	 * An implementation sets values <em>through the model</em>, which is what
	 * runs callbacks and reshapes the dialog; it should not write to the
	 * parameters directly.
	 * </p>
	 *
	 * @param model the values to gather, and the shape to render
	 * @return true if the user accepted, false if they dismissed the dialog
	 */
	public abstract boolean harvest(ParameterModel model);

	@Override
	public double priority() {
		return Priority.VERY_LOW;
	}
}
