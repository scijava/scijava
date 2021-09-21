package org.scijava.ops.engine.progress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Task {

	public List<Task> subTasks = new ArrayList<>();

	AtomicLong max = new AtomicLong(0);

	AtomicLong current = new AtomicLong(0);

	public String status;

	public synchronized Task createSubtask() {
		Task sub = new Task();
		subTasks.add(sub);
		return sub;
	}

	
	public void complete() {
		current.set(max.get());
		
	}

	public void setMax(long max) {
		this.max.set(max);
	}


	// TODO: If an Op tries to call Progress.update() without these variables
	// being declared, throw an error!!!
	public void defineStages(long numStages) {
		
		
	}

	// TODO: If an Op tries to call Progress.update() without these variables
	// being declared, throw an error!!!
	public void setSubTaskCount( long numSubTasks) {
		
		
	}

	public void setCurrent(long current) {
		this.current.set(current);
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
		return (double) current() / max();
	}


	public void update(long numElements) {
		current.addAndGet(numElements);
	}

}
