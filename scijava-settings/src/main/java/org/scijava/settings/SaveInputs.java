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

import org.scijava.execute.Execution;
import org.scijava.execute.Preprocessor;
import org.scijava.priority.Priority;
import org.scijava.struct.MemberInstance;

/**
 * Remembers what the user just chose.
 * <p>
 * NB: a <em>preprocessor</em>, and deliberately. Saving after the run would
 * lose the values whenever the run threw - which is exactly when a user is
 * about to try again, and exactly when retyping everything is most
 * infuriating. It runs last among preprocessors, after the harvester has
 * collected whatever the user typed and after a declined run has stopped the
 * chain, so what it stores is a complete set of values somebody meant.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SaveInputs implements Preprocessor {

	private final Settings settings;

	public SaveInputs(final Settings settings) {
		this.settings = settings;
	}

	@Override
	public void process(final Execution execution) {
		final String table = RememberedInputs.table(execution.executable()
			.executable());
		boolean stored = false;
		for (final MemberInstance<?> member : RememberedInputs.remembered(execution
			.instance()))
		{
			final Object value = member.get();
			if (value == null) continue;
			stored |= settings.set(table, RememberedInputs.key(member.member()),
				value);
		}
		if (stored) settings.save();
	}

	@Override
	public double priority() {
		// NB: after the harvester, which is VERY_LOW, so that what the user typed
		// is what gets remembered.
		return Priority.EXTREMELY_LOW;
	}
}
