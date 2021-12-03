/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2020 SciJava developers.
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

package org.scijava.threads;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Default service for managing active threads.
 *
 * @author Curtis Rueden
 */
public final class DefaultThreadManager implements
		ThreadManager
{

	private static final String SCIJAVA_THREAD_PREFIX = "SciJava-";

	private static final WeakHashMap<Thread, Thread> parents =
			new WeakHashMap<>();

	private ExecutorService executor;

	/** Mapping from ID to single-thread {@link ExecutorService} queue. */
	private Map<String, ExecutorService> queues;

	private int nextThread = 0;

	private boolean disposed;


	// -- ThreadService methods --

	@Override
	public <V> Future<V> run(final Callable<V> code) {
		if (disposed) return null;
		return executor().submit(wrap(code));
	}

	@Override
	public Future<?> run(final Runnable code) {
		if (disposed) return null;
		return executor().submit(wrap(code));
	}

	@Override
	public ExecutorService getExecutorService() {
		return executor();
	}

	@Override
	public void setExecutorService(final ExecutorService executor) {
		this.executor = executor;
	}

	@Override
	public boolean isDispatchThread() {
		return EventQueue.isDispatchThread();
	}

	@Override
	public void invoke(final Runnable code) throws InterruptedException,
			InvocationTargetException
	{
		if (isDispatchThread()) {
			// just call the code
			code.run();
		}
		else {
			// invoke on the EDT
			EventQueue.invokeAndWait(wrap(code));
		}
	}

	@Override
	public void queue(final Runnable code) {
		EventQueue.invokeLater(wrap(code));
	}

	@Override
	public Future<?> queue(final String id, final Runnable code) {
		return executor(id).submit(wrap(code));
	}

	@Override
	public <V> Future<V> queue(final String id, final Callable<V> code) {
		return executor(id).submit(wrap(code));
	}

	@Override
	public Thread getParent(final Thread thread) {
		return parents.get(thread != null ? thread : Thread.currentThread());
	}

	// -- Disposable methods --

	@Override
	public synchronized void dispose() {
		disposed = true;
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
		if (queues != null) {
			for (final ExecutorService queue : queues.values()) {
				queue.shutdown();
			}
		}
	}

	// -- ThreadFactory methods --

	@Override
	public Thread newThread(final Runnable r) {
		final String threadName = contextThreadPrefix() + nextThread++;
		return new Thread(r, threadName);
	}

	// -- Helper methods --

	private ExecutorService executor() {
		if (executor == null) initExecutor();
		return executor;
	}

	private synchronized ExecutorService executor(final String id) {
		if (disposed) return null;
		if (queues == null) queues = new HashMap<>();
		if (!queues.containsKey(id)) {
			final ThreadFactory factory = r -> {
				final String threadName = contextThreadPrefix() + id;
				return new Thread(r, threadName);
			};
			final ExecutorService queue = Executors.newSingleThreadExecutor(factory);
			queues.put(id, queue);
		}
		return queues.get(id);
	}

	private synchronized void initExecutor() {
		if (executor != null) return;
		executor = Executors.newCachedThreadPool(this);
	}

	private Runnable wrap(final Runnable r) {
		final Thread parent = Thread.currentThread();
		return () -> {
			final Thread thread = Thread.currentThread();
			try {
				if (parent != thread) parents.put(thread, parent);
				r.run();
			}
			finally {
				if (parent != thread) parents.remove(thread);
			}
		};
	}

	private <V> Callable<V> wrap(final Callable<V> c) {
		final Thread parent = Thread.currentThread();
		return () -> {
			final Thread thread = Thread.currentThread();
			try {
				if (parent != thread) parents.put(thread, parent);
				return c.call();
			}
			finally {
				if (parent != thread) parents.remove(thread);
			}
		};
	}

	private String contextThreadPrefix() {
		final String contextHash = Integer.toHexString(this.hashCode());
		return SCIJAVA_THREAD_PREFIX + contextHash + "-Thread-";
	}

}
