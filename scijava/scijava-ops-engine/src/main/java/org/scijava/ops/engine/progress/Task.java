package org.scijava.ops.engine.progress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Task {

	private final Task parent;

	public Task() {
		this.parent = null;
	}

	public Task(Task parent) {
		this.parent = parent;
	}

	long numStages = 0;
	AtomicLong stagesCompleted = new AtomicLong(0);

	long numSubTasks = 0;
	public List<Task> subTasks = new ArrayList<>();
	AtomicLong subTasksCompleted = new AtomicLong(0);

	boolean tasksDefined = false;
	boolean updateDefined = false;

	AtomicLong max = new AtomicLong(1);

	AtomicLong current = new AtomicLong(0);

	public String status = "Executing...";
	private boolean completed = false;

	public synchronized Task createSubtask() {
		Task sub = new Task(this);
		subTasks.add(sub);
		return sub;
	}

	
	public void complete() {
		if (current.longValue() != 0) throw new IllegalStateException(
			"Task finished in the middle of a stage!");
		if (stagesCompleted.longValue() != numStages)
			throw new IllegalStateException("Task declared " + numStages +
				" total stages, however only " + stagesCompleted + " were completed!");
		if (subTasksCompleted.longValue() != numSubTasks)
			throw new IllegalStateException("Task declared " + numSubTasks +
				" op subtasks, however only " + subTasksCompleted + " were completed!");
		this.completed = true;
		if (parent != null) parent.recordSubtaskCompletion(this);
	}

	private void recordSubtaskCompletion(Task task) {
		if (!subTasks.contains(task)) throw new IllegalArgumentException("Task " +
			task + " is not a subtask of Task " + this);
		if (tasksDefined) subTasksCompleted.getAndIncrement();
	}

	public boolean isComplete() {
		return completed;
	}

	public void defineTotalProgress(int opStages) {
		defineTotalProgress(opStages, 0);
	}

	public void defineTotalProgress(int opStages, int totalSubTasks) {
		this.numStages = opStages;
		this.numSubTasks = totalSubTasks;
		this.tasksDefined = true;
	}

	public boolean progressDefined () {
		return tasksDefined;
	}

	public void setStageMax(long max) {
		this.max.set(max);
		this.updateDefined = true;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public double progress() {
		if(isComplete()) return 1.;
		if(!this.tasksDefined) return 0.;
		double totalCompletion = stagesCompleted.get();
		totalCompletion += current.doubleValue() / max.doubleValue();
		totalCompletion += subTasksCompleted.get();
		return totalCompletion / (numStages + numSubTasks);
	}

	public String status() {
		return status;
	}

	public void update(long numElements) {
		if (updateDefined) {
			current.addAndGet(numElements);
			if (current.longValue() == max.longValue()) {
				current.set(0);
				stagesCompleted.incrementAndGet();
				this.updateDefined = false;
				if (stagesCompleted.longValue() == numStages && subTasksCompleted
					.longValue() == numSubTasks) completed = true;
			}
		}
		else {
			// update is undefined if total progress has not been set!
			throw new IllegalStateException(
				"Cannot update; progress has not yet been defined!");
		}
	}

}
