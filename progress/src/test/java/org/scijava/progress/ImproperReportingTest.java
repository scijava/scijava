/*-
 * #%L
 * An interrupt-based subsystem for progress reporting.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Tests that improper progress reporting results in failure
 *
 * @author Gabriel Selzer
 */
public class ImproperReportingTest {

	/**
	 * Tests that tasks who updated progress past the defined maximum result in a
	 * thrown error.
	 */
	@Test
	public void testDefineTooFewStages() {
		assertISEFromTask( //
			// task
			() -> {
				// defines 2 stages
				Progress.defineTotal(2);
				// but completes 3 stages
				for (int i = 0; i < 3; i++) {
					Progress.update();
				}
				return "All done";
			});
	}

	/**
	 * Tests that tasks who did not complete as many stages as they said they
	 * would results in a thrown error.
	 */
	@Test
	public void testDefineTooManyStages() {
		assertISEFromTask( //
			// task
			() -> {
				// defines 3 stages
				Progress.defineTotal(3);
				// but completes 2 stages
				for (int i = 0; i < 2; i++) {
					Progress.update();
				}
				return "All done!";
			});
	}

	/**
	 * A testing subtask
	 */
	private final Function<Integer, Integer> subtask = (in) -> in;

	/**
	 * Tests that tasks who updated progress past the defined maximum result in a
	 * thrown error.
	 */
	@Test
	public void testDefineTooFewSubTasks() {
		assertISEFromTask( //
			// task
			() -> {
				// define 2 subtasks
				Progress.defineTotal(0, 2);
				// but complete 3 subtasks
				for (int i = 0; i < 3; i++) {
					// Call subtask
					Progress.register(subtask);
					subtask.apply(4);
					Progress.complete();
				}
				return "All done!";
			});
	}

	/**
	 * Tests that tasks who did not complete as many subtasks as they said they
	 * would results in a thrown error.
	 */
	@Test
	public void testDefineTooManySubTasks() {
		assertISEFromTask( //
			// task
			() -> {
				// define 3 subtasks
				Progress.defineTotal(0, 3);
				// but run 2 subtasks
				for (int i = 0; i < 2; i++) {
					// Call subtask
					Progress.register(subtask);
					subtask.apply(4);
					Progress.complete();
				}
				return "All done!";
			});
	}

	// -- Helper methods -- //

	private void assertISEFromTask(Supplier<?> task) {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			Progress.register(task);
			task.get();
			Progress.complete();
		});
	}

}
