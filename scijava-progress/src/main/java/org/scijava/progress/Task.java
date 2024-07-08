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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Keeps track of progress associated with an execution of progressible code.
 * {@link Task}s should <b>not</b> be updated by the code itself, but rather
 * updated via {@link Progress#update()}.
 *
 * @author Gabriel Selzer
 */
public class Task {

	// -- FIELDS -- //

	/** The Object that will update its progress */
	private final Object progressible;

	/** Parent of this task */
	private final Task parent;

	/** Subtasks created by this task */
	private final List<Task> subTasks = new CopyOnWriteArrayList<>();

	/** Designates task completion */
	private boolean completed = false;

	/** Number of tasks (this task + any subtasks) contained within this task */
	private long totalTasks = 1;

	/** Progress in current stage */
	private final AtomicLong current = new AtomicLong(0);

	/** Maximum of current stage */
	private final AtomicLong max = new AtomicLong(1);

	/**
	 * True iff a call to {@link Task#defineTotal(long, long)} has been made
	 */
	private boolean tasksDefined = false;

	/** String identifying the task */
	private final String description;

	/** Computation status as defined by the task */
	private String status = "";

	public Task( //
		final Object progressible, //
		final String description //
	) {
		this(progressible, null, description);
	}

	public Task(final Object progressible, //
		final Task parent, //
		final String description)
	{
		this.progressible = progressible;
		this.parent = parent;
		if (this.parent != null) {
			this.parent.subTasks.add(this);
		}
		this.description = description;
	}

	/**
	 * Records the completion of this {@link Task}
	 */
	public void complete() {
		if (tasksDefined && progress() != 1.0) {
            var msg = "Task " + description();
			if (current.longValue() != max.longValue()) {
				msg += " finished in the middle of a stage!";
			}
			else {
				msg += " has subtasks that did not complete!";
			}
			throw new IllegalStateException(msg);
		}
		else {
			// For tasks with undefined progress, we simply update current to be max.
			this.current.set(this.max.get());
			this.totalTasks = subTasks.size() + 1;
		}
		this.completed = true;
	}

	/**
	 * Defines the total progress of this {@link Task}.
	 * <p>
	 * This method <b>must be called</b> before {@link #update(long)} or
	 * {@link Task#progress()} are called, as otherwise total progress cannot be
	 * defined.
	 * <p>
	 * Under the hood, this method calls {@link #defineTotal(long, long)}
	 *
	 * @param elements the number of discrete packets of computation.
	 */
	public void defineTotal(final long elements) {
		defineTotal(elements, 0);
	}

	/**
	 * Defines the total progress of this {@link Task}.
	 * <p>
	 * This method <b>must be called</b> before {@link #update(long)} or
	 * {@link Task#progress()} are called, as otherwise total progress cannot be
	 * defined.
	 *
	 * @param elements the number of discrete packets of computation.
	 * @param subTasks the number <b>of times</b> subtasks are called upon within
	 *          the task. This <b>is not</b> the same as the number of subtasks
	 *          used (as one subtask may run multiple times).
	 */
	public void defineTotal(final long elements, final long subTasks) {
		// Nonzero number of elements
		if (elements > 0L) {
			this.totalTasks = subTasks + 1;
			this.max.set(elements);
		}
		// Zero elements, nonzero subtasks
		else if (subTasks > 0L) {
			this.totalTasks = subTasks;
			this.max.set(1);
		}
		// Zero elements & zero subtasks
		// NB one common example of this situation occurs when you
		// want to programmatically set the elements to the size of a list,
		// but then you get passed an empty list. In this case, there is one task,
		// which is already complete!.
		else {
			this.totalTasks = 1;
			this.current.set(1);
		}
		this.tasksDefined = true;
	}

	/**
	 * Returns {@code true} iff this {@link Task} is complete.
	 *
	 * @return {@code true} iff this {@link Task} is complete.
	 */
	public boolean isComplete() {
		return completed;
	}

	/**
	 * Calculates and returns the progress of the associated progressible
	 * {@link Object}. If the total progress is defined using
	 * {@link Task#defineTotal(long, long)}, then this method will return a
	 * {@code double} within the range [0, 1]. If the progress is <b>not</b>
	 * defined, then this task will return {@code 0} until {@link #complete()} is
	 * called; after that call this method will return {@code 1.}.
	 *
	 * @return the progress
	 */
	public double progress() {
        var totalCompletion = current.doubleValue() / max.doubleValue();
		for (var t : subTasks) {
			totalCompletion += t.progress();
		}
		return totalCompletion / totalTasks;
	}

	/**
	 * Used by the progressible {@link Object} to set the status of its
	 * computation. This method should <b>not</b> used by any code not part of the
	 * progressible {@link Object}
	 *
	 * @param status the status of the progressible {@link Object}
	 */
	public void setStatus(final String status) {
		this.status = status;
	}

	/**
	 * The status of the progressible {@link Object}
	 *
	 * @return the status
	 */
	public String status() {
		return status;
	}

	/**
	 * The id of the progressible {@link Object}
	 *
	 * @return the id
	 */
	public String description() {
		return description;
	}

	/**
	 * Used by a progressible {@link Object}, through {@link Progress} to
	 * increment progress
	 *
	 * @param numElements the number of elements completed
	 * @see Progress#update(long, Task) when a particular task must be updated
	 */
	protected void update(final long numElements) {
		current.addAndGet(numElements);
	}

	/**
	 * Gets the progressible {@link Object} contributing to this {@link Task}'s
	 * progress.
	 *
	 * @return the progressible {@link Object}
	 */
	public Object progressible() {
		return progressible;
	}

	/**
	 * Returns the parent {@link Task} of this {@code Task}.
	 *
	 * @return the parent {@link Task}, or {@code null} if this Task has no parent
	 */
	protected Task parent() {
		return parent;
	}

	public boolean isSubTask() {
		return parent != null;
	}

	@Override
	public String toString() {
		if (isComplete()) {
			return String.format("Progress of %s: Complete\n", description());
		}
		return String.format( //
			"Progress of %s: %.2f\n", //
			description(), //
			progress() //
		);
	}
}
