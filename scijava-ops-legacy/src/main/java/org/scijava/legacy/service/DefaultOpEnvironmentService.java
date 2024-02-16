
package org.scijava.legacy.service;

import org.scijava.app.StatusService;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.progress.Progress;
import org.scijava.progress.ProgressListener;
import org.scijava.progress.Task;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.task.TaskService;

import java.io.ObjectInputFilter;
import java.util.Map;
import java.util.WeakHashMap;

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

	private OpEnvironment env;

	@Override
	public void initialize() {
		// Build the Op Environment
		env = OpEnvironment.build();

		// Set up alias, if ScriptService available
		if (scriptService != null) {
			scriptService.addAlias(OpEnvironment.class);
		}

		// Set up progress, if StatusService available
		if (taskService != null) {
			Progress.addGlobalListener(new SciJavaProgressListener(taskService));
		}
	}

	private class SciJavaProgressListener implements ProgressListener {

		private final Map<Task, org.scijava.task.Task> taskMap;
		private TaskService tasks;

		public SciJavaProgressListener(TaskService tasks) {
			this.tasks = tasks;
			this.taskMap = new WeakHashMap<>();
		}

		@Override
		public void acknowledgeUpdate(Task task) {
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

	@Override
	public OpEnvironment env() {
		return env;
	}
}
