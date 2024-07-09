/*
 * #%L
 * SciJava library facilitating consistent parallel processing.
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

package org.scijava.concurrent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class, with methods to create {@link TaskExecutor}s.
 */
public final class TaskExecutors {

	private TaskExecutors() {
		// prevent from instantiation
	}

	/**
	 * {@link TaskExecutor} for single threaded execution.
	 */
	public static TaskExecutor singleThreaded() {
		return SequentialTaskExecutor.getInstance();
	}

	/**
	 * {@link TaskExecutor} for multi-threaded execution. {@link ForkJoinPool} is
	 * used.
	 */
	public static TaskExecutor multiThreaded() {
		return FORK_JOIN_TASK_EXECUTOR;
	}

	private static final TaskExecutor FORK_JOIN_TASK_EXECUTOR =
		new DefaultTaskExecutor(new ForkJoinExecutorService());

	/**
	 * {@link TaskExecutor} that uses the given number or threads. The
	 * {@link TaskExecutor} needs to be closed by calling
	 * {@link TaskExecutor#close()}.
	 */
	public static TaskExecutor numThreads(int numThreads) {
		numThreads = Math.max(1, numThreads);
		return forExecutorService(new ForkJoinPool(numThreads));
	}

	/**
	 * Creates a {@link TaskExecutor} that wraps around given
	 * {@link ExecutorService}.
	 */
	public static TaskExecutor forExecutorService(
		ExecutorService executorService)
	{
		return new DefaultTaskExecutor(executorService);
	}

	/**
	 * Creates a {@link TaskExecutor} that wraps around given
	 * {@link ExecutorService}, and will return the given parallelism.
	 */
	public static TaskExecutor forExecutorServiceAndNumThreads(
		ExecutorService executorService, int numThreads)
	{
		return new DefaultTaskExecutor(executorService) {

			@Override
			public int getParallelism() {
				return numThreads;
			}
		};
	}

	/**
	 * Creates a {@link TaskExecutor} that wraps around given
	 * {@link ExecutorService}, and will suggest the given number of tasks when
	 * asked.
	 */
	public static TaskExecutor forExecutorServiceAndNumTasks(
		ExecutorService executorService, int numTasks)
	{
		return new DefaultTaskExecutor(executorService) {

			@Override
			public int suggestNumberOfTasks() {
				return numTasks;
			}
		};
	}

	/**
	 * Returns a {@link TaskExecutor} that uses a fixed thread pool with the given
	 * number of threads.
	 */
	public static TaskExecutor fixedThreadPool(int numThreads) {
        var threadFactory = threadFactory(() -> singleThreaded());
		return forExecutorService(Executors.newFixedThreadPool(numThreads,
			threadFactory));
	}

	/**
	 * Returns a {@link TaskExecutor} that uses a fixed thread pool with the given
	 * number of threads. But that's not the end of the story.
	 * <p>
	 * Each of the threads uses itself a fixed thread pool with the given number
	 * of sub threads.
	 * <p>
	 * This {@link TaskExecutor} is useful for nested parallelization, when
	 * detailed control for the level of parallelism is needed.
	 */
	public static TaskExecutor nestedFixedThreadPool(int numThreads,
		int numSubThreads)
	{
        var threadFactory = threadFactory(() -> fixedThreadPool(
			numSubThreads));
		return forExecutorService(Executors.newFixedThreadPool(numThreads,
			threadFactory));
	}

	/**
	 * Returns a {@link ThreadFactory}. Whenever this thread factory is used to
	 * create a thread, a {@link TaskExecutor} will is create and assigned to the
	 * thread. The {@link TaskExecutor} is created using the given supplier.
	 */
	public static ThreadFactory threadFactory(
		Supplier<TaskExecutor> taskExecutorFactory)
	{
		return applyTaskExecutorToThreadFactory(taskExecutorFactory, Executors
			.defaultThreadFactory());
	}

	/**
	 * Returns a {@link ThreadFactory}. Whenever this thread factory is used to
	 * create a thread, a {@link TaskExecutor} will is create and assigned to the
	 * thread. The threads created, are using the given thread factory, and the
	 * {@link TaskExecutors} are create using the given supplier.
	 */
	public static ThreadFactory applyTaskExecutorToThreadFactory(
		Supplier<TaskExecutor> taskExecutorFactory, ThreadFactory threadFactory)
	{
		return runnable -> threadFactory.newThread(() -> {
			try (var taskExecutor = taskExecutorFactory.get()) {
				Parallelization.runWithExecutor(taskExecutor, runnable);
			}
		});
	}

	/**
	 * A {@link TaskExecutor} that wraps around a given {@link ExecutorService}.
	 */
	static class DefaultTaskExecutor implements TaskExecutor {

		private final ExecutorService executorService;

		public DefaultTaskExecutor(final ExecutorService executorService) {
			this.executorService = executorService;
		}

		@Override
		public ExecutorService getExecutorService() {
			return executorService;
		}

		@Override
		public int getParallelism() {
			if (executorService instanceof ForkJoinPool)
				return ((ForkJoinPool) executorService).getParallelism();
			else if (executorService instanceof ThreadPoolExecutor) return Math.max(1,
				((ThreadPoolExecutor) executorService).getCorePoolSize());
			else if (executorService instanceof ForkJoinExecutorService)
				return ((ForkJoinExecutorService) executorService).getParallelism();
			else if (executorService instanceof SequentialExecutorService)
				return ((SequentialExecutorService) executorService).getParallelism();
			return Runtime.getRuntime().availableProcessors();
		}

		@Override
		public void runAll(final List<Runnable> tasks) {
			final List<Callable<Object>> callables = new ArrayList<>(tasks.size());
			// use for-loop because stream with collect(Collectors.toList) is slow.
			for (var task : tasks)
				callables.add(Executors.callable(task));
			invokeAllIgnoreResults(callables);
		}

		@Override
		public int suggestNumberOfTasks() {
            var parallelism = getParallelism();
			return (parallelism == 1) ? 1 : (int) Math.min((long) parallelism * 4L,
				(long) Integer.MAX_VALUE);
		}

		@Override
		public <T> void forEach(final List<? extends T> parameters,
			final Consumer<? super T> task)
		{
			final List<Callable<Object>> callables = new ArrayList<>(parameters.size());
			// use for-loop because stream with collect(Collectors.toList) is slow.
			for (var parameter : parameters)
				callables.add(() -> {
					task.accept(parameter);
					return null;
				});
			invokeAllIgnoreResults(callables);
		}

		@Override
		public <T, R> List<R> forEachApply(List<? extends T> parameters,
			Function<? super T, ? extends R> task)
		{
			final List<Callable<R>> callables = new ArrayList<>(parameters.size());
			// use for-loop because stream with collect(Collectors.toList) is slow.
			for (var parameter : parameters)
				callables.add(() -> task.apply(parameter));
			try {
				final var futures = executorService.invokeAll(callables);
				final List<R> results = new ArrayList<>(futures.size());
				for (var future : futures)
					results.add(future.get());
				return results;
			}
			catch (InterruptedException | ExecutionException e) {
				throw unwrapExecutionException(e);
			}
		}

		private void invokeAllIgnoreResults(final List<Callable<Object>> callables) {
			try {
				final var futures = executorService.invokeAll(callables);
				for (var future : futures)
					future.get();
			}
			catch (InterruptedException | ExecutionException e) {
				throw unwrapExecutionException(e);
			}
		}

		/**
		 * {@link ExecutorService} wrap all exceptions thrown by any task into a
		 * {@link ExecutionException}, this makes the stack traces rather hard to
		 * read. This method unwraps the {@link ExecutionException} and thereby
		 * reveals the original exception, and ensures it's complete stack trace.
		 */
		private RuntimeException unwrapExecutionException(Throwable e) {
			if (e instanceof ExecutionException) {
				final var cause = e.getCause();
				cause.setStackTrace(concatenate(cause.getStackTrace(), e
					.getStackTrace()));
				e = cause;
			}
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			else return new RuntimeException(e);
		}

		private <T> T[] concatenate(final T[] a, final T[] b) {
            var aLen = a.length;
            var bLen = b.length;
			@SuppressWarnings("unchecked")
            var c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen +
				bLen);
			System.arraycopy(a, 0, c, 0, aLen);
			System.arraycopy(b, 0, c, aLen, bLen);
			return c;
		}

		@Override
		public void close() {
			executorService.shutdown();
		}
	}

	/**
	 * An {@link ExecutorService}, for efficient nested parallelization.
	 * <p>
	 * The {@link ForkJoinPool} is an ExecutorService that provides an entry point
	 * to a technique called work-steeling. Work-steeling allows good performance
	 * for nested parallelization. But calling {@link ForkJoinPool#submit} or
	 * {@link ForkJoinPool#invokeAll} alone, won't result in any work-steeling and
	 * performance boost. It's necessary to use {@link ForkJoinTask}s and their
	 * methods {@link ForkJoinTask#fork fork} or {@link ForkJoinTask#invokeAll
	 * invokeAll} to benefit from work-steeling.
	 * <p>
	 * ForkJoinExecutorService is an ExecutorService that internally calls
	 * ForkJoinTask.fork() and ForkJoinTask.invokeAll(...) and therefore directly
	 * achieves good performance by work-steeling.
	 * <p>
	 * ForkJoinExecutorService is not a fully functional ExecutorService. Methods
	 * like {@link #shutdownNow()}, {@link #awaitTermination(long, TimeUnit)} and
	 * {@link #invokeAll(Collection, long, TimeUnit)} are not implemented.
	 */
	static class ForkJoinExecutorService extends AbstractExecutorService {

		public int getParallelism() {
			return getPool().getParallelism();
		}

		@Override
		public void shutdown() {}

		@Override
		public List<Runnable> shutdownNow() {
			throw new UnsupportedOperationException(
					"ForkJoinExecutorService, shutdownNow is not implemented.");
		}

		@Override
		public boolean isShutdown() {
			return false;
		}

		@Override
		public boolean isTerminated() {
			return false;
		}

		@Override
		public boolean awaitTermination(long l, TimeUnit timeUnit)
				throws InterruptedException
		{
			// NB: it's possible to implement this method. One might use a set of weak
			// references to collect all tasks submitted.
			// And this method call ForkJoinTask.get( long, timeUnit), to get the timing
			// correct.
			// But doing so introduces reduced performance, as the set of tasks needs to
			// be managed.
			// It's simpler to not use await termination at all.
			// Alternative is to collect the futures and call get on them.
			throw new UnsupportedOperationException(
					"ForkJoinExecutorService, awaitTermination is not implemented.");
		}

		@Override
		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> collection) throws InterruptedException
		{
			// TODO: Revisit if we ever drop support for Java 8.
			// For Java 11, the code below could be replaced by
			// return getPool().invokeAll( collection );
			// For Java 8, this throws
			// RejectedExecutionException: Thread limit exceeded replacing blocked
			// worker
			// Also revisit the submit/execute methods below.
			// See https://github.com/imglib/imglib2/pull/269#discussion_r326855353
			List<ForkJoinTask<T>> futures = new ArrayList<>(collection.size());
			for (var callable : collection)
				futures.add(ForkJoinTask.adapt(callable));
			ForkJoinTask.invokeAll(futures);
			return Collections.unmodifiableList(futures);
		}

		@Override
		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
				throws InterruptedException
		{
			throw new UnsupportedOperationException(
					"ForkJoinExecutorService, invokeAll with timeout is not implemented.");
		}

		@Override
		public Future<?> submit(Runnable runnable) {
			return ForkJoinTask.adapt(runnable).fork();
		}

		@Override
		public <T> Future<T> submit(Runnable runnable, T t) {
			return ForkJoinTask.adapt(runnable, t).fork();
		}

		@Override
		public <T> Future<T> submit(Callable<T> callable) {
			return ForkJoinTask.adapt(callable).fork();
		}

		@Override
		public void execute(Runnable runnable) {
			ForkJoinTask.adapt(runnable).fork();
		}

		private ForkJoinPool getPool() {
            var pool = ForkJoinTask.getPool();
			return pool != null ? pool : ForkJoinPool.commonPool();
		}
	}

	/**
	 * A {@link ExecutorService} that is single-threaded, it never uses threads.
	 */
	static class SequentialExecutorService extends AbstractExecutorService {

		public int getParallelism() {
			return 1;
		}

		@Override
		public void shutdown() {
			// do nothing
		}

		@Override
		public List<Runnable> shutdownNow() {
			return Collections.emptyList();
		}

		@Override
		public boolean isShutdown() {
			return false;
		}

		@Override
		public boolean isTerminated() {
			return true;
		}

		@Override
		public boolean awaitTermination(long l, TimeUnit timeUnit)
				throws InterruptedException
		{
			return true;
		}

		@Override
		public void execute(Runnable runnable) {
			runnable.run();
		}
	}

	/**
	 * A {@link TaskExecutor} for single threaded execution.
	 */
	static class SequentialTaskExecutor implements TaskExecutor {

		private static final SequentialTaskExecutor INSTANCE = new SequentialTaskExecutor();

		private final ExecutorService executorService = new SequentialExecutorService();

		private SequentialTaskExecutor() {
			// Only one instance of the sequential task executor is needed.
		}

		public static TaskExecutor getInstance() {
			return INSTANCE;
		}

		@Override
		public ExecutorService getExecutorService() {
			return executorService;
		}

		@Override
		public int suggestNumberOfTasks() {
			return 1;
		}

		@Override
		public int getParallelism() {
			return 1;
		}

		@Override
		public void runAll(List<Runnable> tasks) {
			for (var task : tasks)
				task.run();
		}

		@Override
		public <T> void forEach(List<? extends T> parameters,
								Consumer<? super T> task)
		{
			for (var value : parameters)
				task.accept(value);
		}

		@Override
		public <T, R> List<R> forEachApply(List<? extends T> parameters,
										   Function<? super T, ? extends R> task)
		{
			final List<R> results = new ArrayList<>(parameters.size());
			for (final var value : parameters) {
                var result = task.apply(value);
				results.add(result);
			}
			return results;
		}

		@Override
		public void close() {
			// no resources that need to be cleaned up
		}
	}
}
