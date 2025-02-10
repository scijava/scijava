/*-
 * #%L
 * Interoperability with legacy SciJava libraries.
 * %%
 * Copyright (C) 2023 - 2025 SciJava developers.
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

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.progress.Progress;
import org.scijava.progress.Task;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.task.TaskService;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link OpEnvironmentService}
 *
 * @author Gabriel Selzer
 */
@Plugin(type = Service.class, attrs = { @Attr(name = "noAlias") })
public class DefaultOpEnvironmentService extends AbstractService implements
	OpEnvironmentService
{

	@Parameter(required = false)
	private ScriptService scriptService;

	@Parameter(required = false)
	private TaskService taskService;

	@Override
	public void initialize() {
		// Set up alias, if ScriptService available
		if (scriptService != null) {
			scriptService.addAlias(OpEnvironment.class);
		}

		// Set up progress, if StatusService available
		if (taskService != null) {
			Progress.addGlobalListener(new SciJavaTaskConsumer(taskService));
		}
	}

	private static class SciJavaTaskConsumer implements Consumer<Task> {

		private final Map<Task, org.scijava.task.Task> taskMap;
		private final TaskService tasks;

		public SciJavaTaskConsumer(TaskService tasks) {
			this.tasks = tasks;
			this.taskMap = new WeakHashMap<>();
		}

		@Override
		public void accept(Task task) {
			if (task.isSubTask()) return;
			var sjTask = taskMap.computeIfAbsent( //
				task, //
				(t) -> { //
					var value = this.tasks.createTask(t.description());
					value.start();
					value.setProgressMaximum(100L);
					return value;
				});
			sjTask.setProgressValue((long) (task.progress() * 100));
			if (task.isComplete()) {
				sjTask.finish();
			}
		}
	}

	/**
	 * A class that lazily loads a static {@link OpEnvironment} using the <a
	 * href=https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom>Initialization-on-demand
	 * holder idiom</a>. This solution provides high concurrency, ensuring the
	 * {@link OpEnvironment} is only constructed once.
	 *
	 * @author Gabriel Selzer
	 */
	private static class OpEnvironmentHolder {

		private static final OpEnvironment env = OpEnvironment.build();

		static {
			env.setDefaultHints(new Hints("progress.TRACK"));
		}

	}

	@Override
	public OpEnvironment env() {
		return OpEnvironmentHolder.env;
	}
}
