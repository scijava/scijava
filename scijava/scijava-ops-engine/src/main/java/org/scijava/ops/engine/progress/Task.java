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

	long numStages = -1;
	AtomicLong stagesCompleted = new AtomicLong(0);

	long numSubTasks = -1;
	public List<Task> subTasks = new ArrayList<>();
	AtomicLong subTasksCompleted = new AtomicLong(0);

	boolean tasksDefined = false;

	AtomicLong max = new AtomicLong(1);

	AtomicLong current = new AtomicLong(0);

	public String status;

	public synchronized Task createSubtask() {
		Task sub = new Task(this);
		subTasks.add(sub);
		return sub;
	}

	
	public void complete() {
		current.set(max.get());
		if (parent != null) parent.recordSubtaskCompletion(this);
	}

	private void recordSubtaskCompletion(Task task) {
		if (!subTasks.contains(task)) throw new IllegalArgumentException("Task " +
			task + " is not a subtask of Task " + this);
		subTasksCompleted.getAndIncrement();

	}

	public boolean isComplete() {
		return max.get() == current.get() && subTasks.stream().allMatch(t -> t.isComplete());
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
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long max( ) {
		return subTasks.parallelStream().mapToLong(t -> t.max()).sum() + max.get();
	}

	public long current( ) {
		return subTasks.parallelStream().mapToLong(t -> t.current()).sum() + current.get();
	}

	public double progress() {
		if(!this.tasksDefined) throw new IllegalStateException("Progress undefined - Op does not define total progress");
		double totalCompletion = stagesCompleted.get();
		totalCompletion += current.doubleValue() / max.doubleValue();
		totalCompletion += subTasksCompleted.get();
		return totalCompletion / (numStages + numSubTasks);
	}


	public void update(long numElements) {
		if(numStages == 0) throw new IllegalStateException("");
		current.addAndGet(numElements);
	}

}
