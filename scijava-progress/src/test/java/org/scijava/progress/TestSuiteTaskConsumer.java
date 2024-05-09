/*-
 * #%L
 * An interrupt-based subsystem for progress reporting.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.progress;

import org.junit.jupiter.api.Assertions;

import java.util.function.Consumer;

public class TestSuiteTaskConsumer implements Consumer<Task> {

	private final String id;
	private final long expectedIterations;
	private double currentIterations = 0;

	private boolean registered = false;
	private boolean completed = false;

	public TestSuiteTaskConsumer(final long expectedIterations) {
		this(expectedIterations, null);
	}

	public TestSuiteTaskConsumer(final long expectedIterations, final String id) {
		this.id = id;
		this.expectedIterations = expectedIterations;
	}

	@Override
	public void accept(Task task) {
		if (id != null && !task.description().equals(id)) return;
		// Registration ping
		if (!registered) {
			registered = true;
			return;
		}
		// Progress pings
		if (!task.isComplete()) {
			Assertions.assertEquals(++currentIterations / expectedIterations, task
				.progress(), 1e-6);
		}
		// Completion ping
		else {
			Assertions.assertFalse(completed);
			Assertions.assertEquals(1., task.progress(), 1e-6);
			completed = true;
		}
	}

	public boolean isComplete() {
		return this.completed;
	}

	public void reset() {
		this.currentIterations = 0;
		this.registered = false;
		this.completed = false;
	}

	public double progress() {
		return this.currentIterations / this.expectedIterations;
	}
}
