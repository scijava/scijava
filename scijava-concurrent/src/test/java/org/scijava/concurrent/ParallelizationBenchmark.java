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

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Demonstrates how to use {@link Parallelization} to execute a algorithm single
 * threaded / multi threaded .... And shows the execution time.
 */
@Fork(1)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@BenchmarkMode({ Mode.AverageTime })
public class ParallelizationBenchmark {

	private final int[][][] image = generateRandomImage(42, 100, 500, 500);

	/**
	 * If the image is one dimensional: run simple calculate sum. If the image is
	 * at least 2D: cut the image into slices. Use {@link TaskExecutor#forEach }
	 * to call {@link #calculateSum} for each slice, and sum up the results.
	 */
	private static long calculateSum(int[][][] image) {
		List<int[][]> slices = Arrays.asList(image);
		AtomicLong result = new AtomicLong();
		Parallelization.getTaskExecutor().forEach(slices, slice -> result.addAndGet(
			calculateSum(slice)));
		return result.get();
	}

	private static long calculateSum(int[][] slice) {
		List<int[]> rows = Arrays.asList(slice);
		AtomicLong result = new AtomicLong();
		Parallelization.getTaskExecutor().forEach(rows, row -> result.addAndGet(
			simpleCalculateSum(row)));
		return result.get();
	}

	private static int[][][] generateRandomImage(long seed, int x, int y, int z) {
		Random r = new Random(seed);
		int[][][] arr = new int[x][y][z];
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; i < arr[i].length; j++) {
				for (int k = 0; k < arr[i][j].length; k++) {
					arr[i][j][k] = r.nextInt();
				}
			}
		}
		return arr;
	}

	private static long simpleCalculateSum(int[][][] image) {
		long result = 0;
		for (int[][] slice : image)
			result += simpleCalculateSum(slice);
		return result;
	}

	private static long simpleCalculateSum(int[][] slice) {
		long result = 0;
		for (int[] row : slice)
			result += simpleCalculateSum(row);
		return result;
	}

	private static long simpleCalculateSum(int[] row) {
		long result = 0;
		for (int pixel : row)
			result += pixel;
		return result;
	}

	@Benchmark
	public Long fixedThreadPool() {
		final ExecutorService executor = Executors.newFixedThreadPool(Runtime
			.getRuntime().availableProcessors());
		Long sum = Parallelization.runWithExecutor(executor, () -> calculateSum(
			image));
		executor.shutdown();
		return sum;
	}

	@Benchmark
	public Long twoThreadsForkJoinPool() {
		ForkJoinPool executor = new ForkJoinPool(2);
		Long sum = Parallelization.runWithExecutor(executor, () -> calculateSum(
			image));
		executor.shutdown();
		return sum;
	}

	@Benchmark
	public Long multiThreaded() {
		return Parallelization.runMultiThreaded(() -> calculateSum(image));
	}

	@Benchmark
	public Long singleThreaded() {
		return Parallelization.runSingleThreaded(() -> calculateSum(image));
	}

	@Benchmark
	public Long defaultBehavior() {
		return calculateSum(image);
	}

	@Benchmark
	public Long singleThreadedBaseline() {
		return calculateSum(image);
	}

	public static void main(String... args) throws RunnerException {
		Options options = new OptionsBuilder().include(
			ParallelizationBenchmark.class.getName()).build();
		new Runner(options).run();
	}
}
