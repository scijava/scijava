package org.scijava.ops.provenance;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.scijava.ops.OpInfo;

public class OpExecutionSummary implements ExecutionSummary<OpInstance> {

	private final OpInstance instance;
	private final List<Object> arguments;
	private Object output;
	private final ReadWriteLock lock;

	private boolean started;
	private boolean completed;

	public OpExecutionSummary(OpInfo info, Object op, List<Object> arguments) {
		this.instance = new OpInstance(info, op);
		this.arguments = arguments;
		this.started = false;
		this.completed = false;
		this.lock = new ReentrantReadWriteLock();
	}

	@Override
	public List<Object> arugments() {
		return arguments;
	}

	@Override
	public boolean hasOutput() {
		return getOutput() != null;
	}

	@Override
	public Object output() {
		return getOutput();
	}

	@Override
	public OpInstance executor() {
		return instance;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasCompleted() {
		return completed;
	}

	@Override
	public void recordStart() {
		started = true;
	}

	@Override
	public void recordCompletion(Object o) {
		setOutput(o);
		completed = true;
	}

	@Override
	public boolean isInput(Object o) {
		// TODO: consider whether this is fine for primitives
		return arguments.stream().anyMatch(a -> a == o);
	}

	@Override
	public boolean isOutput(Object o) {
		return getOutput() == o;
	}

	private void setOutput(Object o) {
		lock.writeLock().lock();
		output = o;
		lock.writeLock().unlock();
	}

	private Object getOutput() {
		lock.readLock().lock();
		Object o = output;
		lock.readLock().unlock();
		return o;
	}

}
