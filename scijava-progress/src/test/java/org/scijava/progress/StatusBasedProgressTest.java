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

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StatusBasedProgressTest {

	public final Function<Integer, Integer> statusSpinningTask = (in) -> {
		for (int i = 0; i < in; i++) {
			Progress.setStatus("Setting status: " + (i + 1));
		}
		return in;
	};

	@Test
	public void testStatusUpdate() {
		Function<Integer, Integer> progressible = statusSpinningTask;
		int numIterations = 10;
		Progress.addListener(progressible, new ProgressListener() {

			boolean registered = false;
			int numUpdates = 0;

			@Override
			public void acknowledgeUpdate(Task task) {
				if (!registered) {
					registered = true;
					return;
				}
				if (numUpdates++ < numIterations) {
					Assertions.assertEquals(task.status(), "Setting status: " +
						(numUpdates));
				}
				else {
					Assertions.assertEquals(1., task.progress(), 1e-6);
				}
			}

		});
		Progress.register(progressible);
		progressible.apply(numIterations);
		Progress.complete();
	}

}
