/*-
 * #%L
 * Interoperability between SciJava Ops and ImageJ/ImageJ2.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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

package org.scijava.legacy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.ops.api.Hints;
import org.scijava.task.Task;
import org.scijava.task.TaskService;
import org.scijava.task.event.TaskEvent;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.script.ScriptService;

import java.util.Collections;

/**
 * Tests {@link OpEnvironmentService} API.
 *
 * @author Gabriel Selzer
 */
public class OpEnvironmentServiceTest {

	/**
	 * Tests that an {@link OpEnvironmentService} produces an
	 * {@link OpEnvironment} with some Ops in it.
	 */
	@Test
	public void testOpEnvironmentService() {
		Context ctx = new Context(OpEnvironmentService.class);
		OpEnvironmentService ops = ctx.getService(OpEnvironmentService.class);
		Double result = ops.env().op("math.add") //
			.input(2., 3.) //
			.outType(Double.class) //
			.apply();
		Assertions.assertEquals(5., result);
		ctx.dispose();
	}

	/**
	 * Test that when a {@link ScriptService} is present, the "OpEnvironment"
	 * alias points to {@link OpEnvironment}.
	 */
	@Test
	public void testOpEnvironmentServiceAliases() {
		Context ctx = new Context(OpEnvironmentService.class, ScriptService.class);
		ScriptService script = ctx.getService(ScriptService.class);
		// Assert the correct alias for OpEnvironment
		Assertions.assertEquals( //
			OpEnvironment.class, //
			script.getAliases().get("OpEnvironment") //
		);
		// Assert no alias for OpEnvironmentService
		Assertions.assertNull(script.getAliases().get("OpEnvironmentService"));
		ctx.dispose();
	}

	/**
	 * Tests that SciJava Progress updates propagate to SciJava Common's
	 * {@link TaskService}.
	 */
	@Test
	public void testTaskForwarding() {
		Context ctx = new Context(OpEnvironmentService.class, TaskService.class,
			EventService.class);
		OpEnvironmentService opService = ctx.getService(OpEnvironmentService.class);
		OpEnvironment env = opService.env();
		EventService event = ctx.getService(EventService.class);

		// Expected Pings:
		// 1 - When the task is started
		// 2 - When the task maximum is set
		// 3 - When the task value is set to 0
		// 4 - When the task value is set to maximum
		// 5 - When the task is finished
		int[] totalPings = { 0 };
		EventSubscriber<TaskEvent> e = new EventSubscriber<>() {

			@Override
			public void onEvent(TaskEvent event) {
				Task t = event.getTask();
				totalPings[0]++;
				// Assert that the max progress is set on the SECOND event
				Assertions.assertEquals(totalPings[0] < 2 ? 0L : 100L, t
					.getProgressMaximum());
				// Assert that the current progress is set on the FOURTH event
				long expectedValue = totalPings[0] < 4 ? 0L : 100L;
				Assertions.assertEquals(expectedValue, t.getProgressValue());
				// Assert that the task is finished on the FIFTH event
				Assertions.assertEquals(totalPings[0] == 5, t.isDone());
			}

			@Override
			public Class<TaskEvent> getEventClass() {
				return TaskEvent.class;
			}
		};
		event.subscribe(e);
		var hints = new Hints("progress.TRACK");
		env.op("math.div", hints).input(2, 3).apply();
		Assertions.assertEquals(5, totalPings[0]);
		event.unsubscribe(Collections.singletonList(e));
	}
}
