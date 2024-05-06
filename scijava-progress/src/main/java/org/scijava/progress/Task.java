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
 * Keeps track of progress associated with an execution of progressable code.
 * {@link Task}s should <b>not</b> be updated by the code itself; it should be
 * update via {@link Progress#update()}.
 *
 * @author Gabriel Selzer
 */
public class Task {

	// -- FIELDS -- //

	/** The Object that will update its progress */
	private final Object progressible;

	/** Parent of this task */
	private final Task parent;

	// NB this is a concurrent map being used as a concurrent set
	/** Subtasks created by this task */
	private final List<Task> subTasks = new CopyOnWriteArrayList<>();

	/** Designates task completion */
	private boolean completed = false;

	private boolean hasElements = true;

	/** Number of tasks utilized by the task */
	private long tasks = 1;

	/** Progress in current stage */
	private final AtomicLong current = new AtomicLong(0);

	/** Maximum of current stage */
	private final AtomicLong max = new AtomicLong(1);

	/**
	 * True iff a call to {@link Task#define(long, long)} has been made
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
		this.description = description;
	}

	/**
	 * Records the completion of this {@link Task}
	 */
	public void complete() {
		if (tasksDefined && progress() != 1.0) {
			String msg = "Task " + description();
			if (current.longValue() != max.longValue()) {
				msg += " finished in the middle of a stage!";
			}
			else {
				msg += " has subtasks that did not complete!";
			}
			throw new IllegalStateException(msg);
		}
		this.completed = true;
		if (hasElements) {
			this.current.set(this.max.get());
		}
	}

	/**
	 * Creates a subtask, recording it to keep track of progress.
	 *
	 * @return the subtask.
	 */
	public synchronized Task createSubtask( //
		Object progressible, //
		String description //
	) {
		final Task sub = new Task(progressible, this, description);
		subTasks.add(sub);
		return sub;
	}

	/**
	 * Defines the total progress of this {@link Task}.
	 * <p>
	 * This method <b>must be called</b> before {@link #update(long)} or
	 * {@link Task#progress()} are called, as otherwise total progress cannot be
	 * defined.
	 * <p>
	 * Under the hood, this method calls {@link #define(long, long)}
	 *
	 * @param elements the number of discrete "elements" in the computation
	 */
	public void define(final long elements) {
		define(elements, 0);
	}

	/**
	 * Defines the total progress of this {@link Task}.
	 * <p>
	 * This method <b>must be called</b> before {@link #update(long)} or
	 * {@link Task#progress()} are called, as otherwise total progress cannot be
	 * defined.
	 *
	 * @param elements the number of discrete "elements" in the computation
	 * @param subTasks the number <b>of times</b> subtasks are called upon within
	 *          the task. This <b>is not</b> the same as the number of subtasks
	 *          used (as one subtask may run multiple times).
	 */
	public void define(final long elements, final long subTasks) {
		if (tasksDefined) {
			throw new IllegalStateException(
				"Progress has already been defined for this task");
		}
		// Total tasks = all subtasks, plus one iff there are also progress elements
		// in this stage
		this.tasks = subTasks + (elements == 0 ? 0 : 1);

		if (elements == 0) {
			this.hasElements = false;
			if (this.tasks == 0) {
				this.tasks = 1;
				this.current.set(1);
				this.max.set(1);
				return;
			}
		}
		else {
			this.max.set(elements);
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
	 * {@link Task#define(long, long)}, then this method will return a
	 * {@code double} within the range [0, 1]. If the progress is <b>not</b>
	 * defined, then this task will return {@code 0} until {@link #complete()} is
	 * called; after that call this method will return {@code 1.}.
	 *
	 * @return the progress
	 */
	public double progress() {
		double totalCompletion = current.doubleValue() / max.doubleValue();
		for (Task t : subTasks) {
			totalCompletion += t.isComplete() ? 1.0 : t.progress();
		}
		return totalCompletion / tasks;
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
	public Task parent() {
		return parent;
	}
}
