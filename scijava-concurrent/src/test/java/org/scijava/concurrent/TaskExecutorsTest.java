/*-
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

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link TaskExecutors}.
 *
 * @author Matthias Arzt
 */
public class TaskExecutorsTest {

	@Test
	public void testSingleThreaded() {
		TaskExecutor executor = TaskExecutors.singleThreaded();
		assertTrue(executor instanceof SequentialTaskExecutor);
	}

	@Test
	public void testMultiThreaded() {
		TaskExecutor executor = TaskExecutors.multiThreaded();
		assertTrue(executor
			.getExecutorService() instanceof ForkJoinExecutorService);
	}

	@Test
	public void testNumThreads() {
		TaskExecutor executor = TaskExecutors.numThreads(2);
		assertEquals(2, executor.getParallelism());
		executor.getExecutorService().shutdown();
	}

	@Test
	public void testForExecutorService() {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		TaskExecutor executor = TaskExecutors.forExecutorService(executorService);
		assertEquals(executorService, executor.getExecutorService());
		executorService.shutdown();
	}

	@Test
	public void testForExecutorServiceAndNumThreads() {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		TaskExecutor executor = TaskExecutors.forExecutorServiceAndNumThreads(
			executorService, 42);
		assertEquals(executorService, executor.getExecutorService());
		assertEquals(42, executor.getParallelism());
		executorService.shutdown();
	}

	@Test
	public void testForExecutorServiceAndNumTasks() {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		TaskExecutor executor = TaskExecutors.forExecutorServiceAndNumTasks(
			executorService, 42);
		assertEquals(executorService, executor.getExecutorService());
		assertEquals(2, executor.getParallelism());
		assertEquals(42, executor.suggestNumberOfTasks());
	}
}
