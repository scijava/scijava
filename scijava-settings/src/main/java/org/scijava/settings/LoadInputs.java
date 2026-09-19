/*
 * #%L
 * Settings a user can read and edit, in one TOML file.
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

package org.scijava.settings;

import java.util.Optional;

import org.scijava.execute.Execution;
import org.scijava.execute.Preprocessor;
import org.scijava.priority.Priority;
import org.scijava.struct.MemberInstance;

/**
 * Fills in what the user chose last time.
 * <p>
 * It runs early - before the harvester - so that a dialog opens showing the
 * values from the previous run, which is what makes a dialog feel like it
 * remembers you. A value the caller supplied is left alone: an input given on
 * a command line, or by the code doing the running, is that run's business.
 * </p>
 *
 * @author Curtis Rueden
 */
public class LoadInputs implements Preprocessor {

	private final Settings settings;

	public LoadInputs(final Settings settings) {
		this.settings = settings;
	}

	@Override
	public void process(final Execution execution) {
		final String table = RememberedInputs.table(execution.executable()
			.executable());
		for (final MemberInstance<?> member : RememberedInputs.remembered(execution
			.instance()))
		{
			// NB: only where the caller supplied nothing. Asking the parameter
			// whether it is empty would not do: a primitive is never empty, so a
			// `double` would never be filled in, and overwriting regardless would
			// make a remembered value beat an explicit one.
			if (execution.isSupplied(member.member().key())) continue;
			final String key = RememberedInputs.key(member.member());
			final Optional<Object> value = settings.get(table, key, member.member()
				.type());
			value.ifPresent(member::set);
		}
	}

	@Override
	public double priority() {
		// NB: before the harvester, so that the dialog shows what was remembered.
		return Priority.HIGH;
	}
}
