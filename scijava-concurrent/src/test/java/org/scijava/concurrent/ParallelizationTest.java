/*
 * #%L
 * SciJava library facilitating consistent parallel processing.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests {@link Parallelization}.
 *
 * @author Matthias Arzt
 */
public class ParallelizationTest {

	@Test
	public void testSingleThreaded() {
		int parallelism = Parallelization.runSingleThreaded(() -> Parallelization
			.getTaskExecutor().getParallelism());
		assertEquals(1, parallelism);
	}

	@Test
	public void testSingleThreadedWithRunnable() {
		AtomicInteger parallelism = new AtomicInteger();
		Parallelization.runSingleThreaded(() -> parallelism.set(Parallelization
			.getTaskExecutor().getParallelism()));
		assertEquals(1, parallelism.get());
	}

	@Test
	public void testMultiThreaded() {
		assumeTrue(ForkJoinPool.commonPool().getParallelism() > 1);
		int parallelism = Parallelization.runMultiThreaded(() -> Parallelization
			.getTaskExecutor().getParallelism());
		assertTrue(parallelism > 1);
	}

	@Test
	public void testWithExecutor() {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		ExecutorService result = Parallelization.runWithExecutor(executor,
			() -> Parallelization.getTaskExecutor().getExecutorService());
		assertEquals(executor, result);
	}

	@Test
	public void testSetExecutorService() {
		TaskExecutor outside = Parallelization.getTaskExecutor();
		TaskExecutor inside = TaskExecutors.forExecutorService(
			new TaskExecutors.SequentialExecutorService());
		try (Parallelization.Frame frame = Parallelization.setExecutorRequiresReset(
			inside))
		{
			assertSame(inside, Parallelization.getTaskExecutor());
		}
		assertSame(outside, Parallelization.getTaskExecutor());
	}
}
