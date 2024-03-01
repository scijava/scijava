
package org.scijava.legacy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.app.StatusService;
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
		Double result = ops.env().binary("math.add") //
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
		env.binary("math.div").input(2, 3).apply();
		Assertions.assertEquals(5, totalPings[0]);
		event.unsubscribe(Collections.singletonList(e));
	}
}
