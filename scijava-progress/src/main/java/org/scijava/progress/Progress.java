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

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A static utility class serving as the interface between progress reporters
 * and progress listeners.
 *
 * @author Gabriel Selzer
 */
public final class Progress {

	/**
	 * Private constructor designed to prevent instantiation.
	 */
	private Progress() {}

	/**
	 * A record of all listeners interested in the progress of all Object
	 * executions. We do not expect very many of these, so to provide high
	 * concurrency we use {@link CopyOnWriteArrayList} as the backing
	 * implementation
	 */
	private static final List<ProgressListener> globalListeners =
		new CopyOnWriteArrayList<>();

	/**
	 * A record of all listeners interested in the progress of a given Object's
	 * executions. We do not expect very many of these, so to provide high
	 * concurrency we use {@link CopyOnWriteArrayList} as the backing
	 * implementation
	 */
	private static final Map<Object, List<ProgressListener>> progressibleListeners =
		new WeakHashMap<>();

	/** Singleton NOP task */
	private static final NOPTask IGNORED = new NOPTask();

	/**
	 * A record of the progressible {@link Object}s running on each
	 * {@link Thread}.
	 */
	private static final ThreadLocal<ArrayDeque<Task>> progressibleStack =
		new InheritableThreadLocal<>()
		{

			private final Collection<Task> initialContents = Collections.singleton(
				IGNORED);

			@Override
			protected ArrayDeque<Task> childValue(ArrayDeque<Task> parentValue) {
				// Child threads should be aware of the Tasks operating on the parent.
				// For example, a progressible Object might wish to parallelize one of
				// its stages; the child threads must know which Task to update
				return parentValue.clone();
			}

			@Override
			protected ArrayDeque<Task> initialValue() {
				return new ArrayDeque<>(initialContents);
			}
		};

	/**
	 * Records {@link ProgressListener} {@code l} as a callback for all
	 * progressible {@link Object}s
	 *
	 * @param l a {@link ProgressListener} that would like to know about the
	 *          progress of {@code progressible} {@link Object}s
	 */
	public static void addGlobalListener(ProgressListener l) {
		if (!globalListeners.contains(l)) {
			globalListeners.add(l);
		}
	}

	/**
	 * Records {@link ProgressListener} {@code l} as a callback for progressible
	 * {@link Object} {@code progressible}
	 *
	 * @param progressible an {@link Object} that reports its progress
	 * @param l a {@link ProgressListener} that would like to know about the
	 *          progress of {@code progressible}
	 */
	public static void addListener(Object progressible, ProgressListener l) {
		if (!progressibleListeners.containsKey(progressible)) {
			createListenerList(progressible);
		}
		progressibleListeners.get(progressible).add(l);
	}

	private static synchronized void createListenerList(Object progressible) {
		if (progressibleListeners.containsKey(progressible)) return;
		progressibleListeners.put(progressible, new CopyOnWriteArrayList<>());
	}

	/**
	 * Completes the current task on this {@link Thread}'s execution hierarchy,
	 * removing it in the process. This method also takes care to ping relevant
	 * {@link ProgressListener}s.
	 *
	 * @see Task#complete()
	 */
	public static void complete() {
		// update completed task
		Task t = progressibleStack.get().pop();
		if (!t.isComplete()) {
			t.complete();
			// ping relevant listeners
			pingListeners(t);
		}
	}

	/**
	 * Adds a new NOP {@link Task} to the current execution chain, used to ignore
	 * {@link Progress} calls by a progressible object. This NOP task will handle
	 * all {@code Progress} calls on this {@link Thread} between the time this
	 * method is invoked and the time when {@link Progress#complete()} is called.
	 */
	public static void ignore() {
		progressibleStack.get().push(IGNORED);
	}

	/**
	 * Creates a new {@link Task} for {@code progressible}. This method makes the
	 * assumption that {@code progressible} is responsible for any calls to
	 * {@link Progress}' progress-reporting API between the time this method is
	 * called and the time when {@link Progress#complete()} is called.
	 *
	 * @param progressible an {@link Object} that would like to report its
	 *          progress.
	 */
	public static void register(final Object progressible) {
		register(progressible, progressible.toString());
	}

	/**
	 * Creates a new {@link Task} for {@code progressible}. This method makes the
	 * assumption that {@code progressible} is responsible for any calls to
	 * {@link Progress}' progress-reporting API between the time this method is
	 * called and the time when {@link Progress#complete()} is called.
	 *
	 * @param progressible an {@link Object} that would like to report its
	 *          progress.
	 * @param description a {@link String} describing {@code progressible}
	 */
	public static void register(final Object progressible,
		final String description)
	{
		Task t;
		var deque = progressibleStack.get();
		var parent = deque.peek();
		if (parent == null || parent == IGNORED) {
			// completely new execution hierarchy
			t = new Task(progressible, description);
		}
		else {
			// part of an existing execution hierarchy
			t = new Task(progressible, parent, description);
		}
		deque.push(t);
		// Ping Listeners about the registration of progressible
		pingListeners(deque.peek());
	}

	/**
	 * Activates all callback {@link ProgressListener}s listening for progress
	 * updates on executions of {@code o}
	 *
	 * @param task an {@link Object} reporting its progress.
	 */
	private static void pingListeners(Task task) {
		if (task == IGNORED) {
			return;
		}
		// Ping object-specific listeners
		List<ProgressListener> list = progressibleListeners.getOrDefault( //
			task.progressible(), //
			Collections.emptyList() //
		);
		synchronized (list) {
			list.forEach(l -> l.acknowledgeUpdate(task));
		}
		// Ping global listeners
		for (var l : globalListeners)
			l.acknowledgeUpdate(task);
		// Ping parent
		if (task.isSubTask()) {
			pingListeners(task.parent());
		}
	}

	/**
	 * Returns the currently-executing {@link Task} on this {@link Thread}
	 *
	 * @return the currently-execution {@link Task}
	 */
	public static Task currentTask() {
		return progressibleStack.get().peek();
	}

	/**
	 * Updates the progress of the current {@link Task}, pinging any interested
	 * {@link ProgressListener}s.
	 *
	 * @see Task#update(long)
	 */
	public static void update() {
		update(1);
	}

	/**
	 * Updates the progress of the current {@link Task}, pinging any interested
	 * {@link ProgressListener}s.
	 *
	 * @param elements the number of elements completed in the current stage.
	 * @see Task#update(long)
	 */
	public static void update(long elements) {
		update(elements, currentTask());
	}

	/**
	 * Updates the progress of the provided {@link Task}, pinging any interested
	 * {@link ProgressListener}s.
	 *
	 * @param numElements the number of elements completed in the current stage.
	 * @param task the {@link Task} to update
	 * @see Task#update(long)
	 */
	public static void update(final long numElements, final Task task) {
		task.update(numElements);
		pingListeners(task);
	}

	/**
	 * Sets the status of the current {@link Task}, pinging any interested
	 * {@link ProgressListener}s.
	 *
	 * @see Task#setStatus(String)
	 */
	public static void setStatus(String status) {
		currentTask().setStatus(status);
		pingListeners(progressibleStack.get().peek());
	}

	/**
	 * Defines the total progress of the current {@link Task}
	 *
	 * @param elements the number of discrete packets of computation.
	 * @see Task#defineTotal(long)
	 */
	public static void defineTotal(long elements) {
		currentTask().defineTotal(elements);
	}

	/**
	 * Defines the total progress of the current {@link Task}
	 *
	 * @param elements the number of discrete packets of computation.
	 * @param subTasks the number <b>of times</b> subtasks are called upon within
	 *          the task. This <b>is not</b> the same as the number of subtasks
	 *          used (as one subtask may run multiple times).
	 * @see Task#defineTotal(long, long)
	 */
	public static void defineTotal(long elements, long subTasks) {
		currentTask().defineTotal(elements, subTasks);
	}

	/**
	 * An internally-used {@link Task} implementation used to suppress
	 * {@link Progress} updates in a performant manner This class should not be
	 * instantiated more than once, as a {@code static} instance is enough to be
	 * used everywhere.
	 */
	private static final class NOPTask extends Task {

		private static final double NOP_PROGRESS = 0.0;

		private NOPTask() {
			super(null, null, null);
		}

		@Override
		public boolean isComplete() {
			return false;
		}

		@Override
		public void complete() {
			// NB: No-op
		}

		@Override
		public void update(long numElements) {
			// NB: No-op
		}

		@Override
		public double progress() {
			return NOP_PROGRESS;
		}

		@Override
		public void defineTotal(final long elements, final long subTasks) {
			// NB: No-op
		}

	}

}
