/*-
 * #%L
 * SciJava Progress: An Interrupt-Based Framework for Progress Reporting.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

/**
 * A static utility class serving as the interface between progress reporters
 * and progress listeners.
 *
 * @author Gabriel Selzer
 */
public final class ProgressListeners {

	/**
	 * Private constructor designed to prevent instantiation.
	 */
	private ProgressListeners() {}

	/**
	 * A record of all listeners interested in the progress of all Object
	 * executions
	 */
	public static final List<ProgressListener> globalListeners =
		new ArrayList<>();

	/**
	 * A record of all listeners interested in the progress of a given Object's
	 * executions
	 */
	public static final Map<Object, List<ProgressListener>> progressibleListeners =
		new WeakHashMap<>();

	/**
	 * A record of the progressible {@link Object}s running on each
	 * {@link Thread}.
	 */
	private static final ThreadLocal<ArrayDeque<Task>> progressibleStack =
		new InheritableThreadLocal<>() {

			@Override
			protected ArrayDeque<Task> childValue(ArrayDeque<Task> parentValue) {
				// Child threads should be aware of the Tasks operating on the parent.
				// For example, a progressible Object might wish to parallelize one of
				// its stages; the child threads must know which Task to update
				return parentValue.clone();
			}

			@Override
			protected ArrayDeque<Task> initialValue() {
				return new ArrayDeque<>();
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
		addListenerToList(progressible, l);
	}

	private static void addListenerToList(Object progressible,
		ProgressListener l)
	{
		List<ProgressListener> list = progressibleListeners.get(progressible);
		synchronized (list) {
			list.add(l);
		}
	}

	private static synchronized void createListenerList(Object progressible) {
		if (progressibleListeners.containsKey(progressible)) return;
		progressibleListeners.put(progressible, new ArrayList<>());
	}

	/**
	 * Registers the execution of a {@link Task}
	 *
	 * @param task the {@link Task} to be registered
	 */
	public static void register(final Task task)
	{
		var deque = progressibleStack.get();
		var parent = deque.peek();
		if (parent != null) {
			parent.addSubtask(task);
		}
		deque.push(task);
	}

	public static void unregister(Task task) {
		var deque = progressibleStack.get();
		if (deque.peek() == task) {
			deque.pop();
		}
	}
}

