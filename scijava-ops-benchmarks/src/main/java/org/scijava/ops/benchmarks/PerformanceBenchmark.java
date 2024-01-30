
package org.scijava.ops.benchmarks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imagej.ops.OpService;
import org.scijava.Context;
import org.scijava.ops.api.OpEnvironment;

/**
 * Tests performance of uint8 image operations with raw byte array, ImageJ 1.x,
 * and ImgLib2 libraries.
 *
 * @author Curtis Rueden
 */
public class PerformanceBenchmark {

	private static final boolean SAVE_RESULTS_TO_DISK = true;

	private static final String METHOD_RAW = "Raw";
	private static final String METHOD_SCIJAVA_OPS = "SciJava Ops";
	private static final String METHOD_IMAGEJ_OPS = "ImageJ Ops";
	private final int width, height;
	private final byte[] rawData;
	private final byte[] scijavaOpsData;
	private final byte[] imageJOpsData;

	/**
	 * List of timing results.
	 * <p>
	 * Each element of the list represents an iteration. Each entry maps the
	 * method name to the time measured.
	 * </p>
	 */
	private final List<Map<String, Long>> results = new ArrayList<>();
	private final OpEnvironment env = OpEnvironment.build();

	private final OpService ops;

	public static void main(final String[] args) throws IOException {
		final int iterations = 10;
		final int size;
		if (args.length > 0) size = Integer.parseInt(args[0]);
		else size = 4000;
		final PerformanceBenchmark bench = new PerformanceBenchmark(size);
		bench.testPerformance(iterations);
		System.exit(0);
	}

	/** Creates objects and measures memory usage. */
	public PerformanceBenchmark(final int imageSize) {
		Context ctx = new Context(OpService.class);
		ops = ctx.getService(OpService.class);

		width = height = imageSize;
		System.out.println();
		System.out.println("===== " + width + " x " + height + " =====");

		final List<Long> memUsage = new ArrayList<>();
		memUsage.add(getMemUsage());
		rawData = createRawData(width, height);
		memUsage.add(getMemUsage());
		scijavaOpsData = rawData.clone();
		memUsage.add(getMemUsage());
		imageJOpsData = rawData.clone();
		memUsage.add(getMemUsage());

		reportMemoryUsage(memUsage);
	}

	public void testPerformance(final int iterationCount) throws IOException {
		// initialize results map
		results.clear();
		for (int i = 0; i < iterationCount; i++) {
			final Map<String, Long> entry = new HashMap<>();
			results.add(entry);
		}
		testCheapPerformance(iterationCount);
		if (SAVE_RESULTS_TO_DISK) saveResults("cheap");
		testExpensivePerformance(iterationCount);
		if (SAVE_RESULTS_TO_DISK) saveResults("expensive");
	}

	/**
	 * Saves benchmark results to the given CSV file on disk.
	 * <p>
	 * The motivation is to produce two charts:
	 * </p>
	 * <ol>
	 * <li>performance by iteration number (on each size of image)
	 * <ul>
	 * <li>one line graph per method</li>
	 * <li>X axis = iteration number</li>
	 * <li>Y axis = time needed</li>
	 * </ul>
	 * </li>
	 * <li>average performance by image size (for first iteration, and 10th)
	 * <ul>
	 * <li>one line graph per method</li>
	 * <li>X axis = size of image</li>
	 * <li>Y axis = time needed</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * <p>
	 * The CSV file produced enables graph #1 very easily. For graph #2, results
	 * from several files must be combined.
	 * </p>
	 */
	public void saveResults(final String prefix) throws IOException {
		final StringBuilder sb = new StringBuilder();

		// write header
		final Map<String, Long> firstEntry = results.get(0);
		final String[] methods = firstEntry.keySet().toArray(new String[0]);
		Arrays.sort(methods);
		sb.append("Iteration");
		for (final String method : methods) {
			sb.append("\t");
			sb.append(method);
		}
		sb.append("\n");

		// write data
		for (int iter = 0; iter < results.size(); iter++) {
			final Map<String, Long> entry = results.get(iter);
			sb.append(iter + 1);
			for (final String method : methods) {
				sb.append("\t");
				sb.append(entry.get(method));
			}
			sb.append("\n");
		}

		// write to disk
		final String path = "results-" + prefix + "-" + width + "x" + height +
			".csv";
		final PrintWriter out = new PrintWriter(new FileWriter(path));
		out.print(sb);
		out.close();
	}

	// -- Helper methods --

	/** Measures performance of a cheap operation (image inversion). */
	private void testCheapPerformance(final int iterationCount) {
		System.out.println();
		System.out.println("-- TIME PERFORMANCE - CHEAP OPERATION --");
		for (int i = 0; i < iterationCount; i++) {
			System.gc();
			System.out.println("Iteration #" + (i + 1) + "/" + iterationCount + ":");
			final List<Long> times = new ArrayList<>();
			times.add(System.currentTimeMillis());
			invertRaw(rawData);
			times.add(System.currentTimeMillis());
			invertUsingSciJavaOps(scijavaOpsData);
			times.add(System.currentTimeMillis());
			invertUsingImageJOps(imageJOpsData);
			times.add(System.currentTimeMillis());
			logTimePerformance(i, times);
		}
	}

	/** Measures performance of a computationally more expensive operation. */
	private void testExpensivePerformance(final int iterationCount) {
		System.out.println();
		System.out.println("-- TIME PERFORMANCE - EXPENSIVE OPERATION --");
		for (int i = 0; i < iterationCount; i++) {
			System.gc();
			System.out.println("Iteration #" + (i + 1) + "/" + iterationCount + ":");
			final List<Long> times = new ArrayList<>();
			times.add(System.currentTimeMillis());
			randomizeRaw(rawData);
			times.add(System.currentTimeMillis());
			randomizeUsingSciJavaOps(scijavaOpsData);
			times.add(System.currentTimeMillis());
			randomizeUsingImageJOps(imageJOpsData);
			times.add(System.currentTimeMillis());
			logTimePerformance(i, times);
		}
	}

	private long getMemUsage() {
		final Runtime r = Runtime.getRuntime();
		System.gc();
		System.gc();
		return r.totalMemory() - r.freeMemory();
	}

	private void reportMemoryUsage(final List<Long> memUsage) {
		final long rawMem = computeDifference(memUsage);
		final long sjMem = computeDifference(memUsage);
		final long ijMem = computeDifference(memUsage);
		System.out.println();
		System.out.println("-- MEMORY OVERHEAD --");
		System.out.println(METHOD_RAW + ": " + rawMem + " bytes");
		System.out.println(METHOD_SCIJAVA_OPS + ": " + sjMem + " bytes");
		System.out.println(METHOD_IMAGEJ_OPS + ": " + ijMem + " bytes");
	}

	private void logTimePerformance(final int iter, final List<Long> times) {
		final long rawTime = computeDifference(times);
		final long sjTime = computeDifference(times);
		final long ijTime = computeDifference(times);

		final Map<String, Long> entry = results.get(iter);
		entry.put(METHOD_RAW, rawTime);
		entry.put(METHOD_SCIJAVA_OPS, sjTime);
		entry.put(METHOD_IMAGEJ_OPS, ijTime);

		reportTime(METHOD_RAW, rawTime, rawTime, sjTime);
		reportTime(METHOD_SCIJAVA_OPS, sjTime, rawTime, sjTime);
		reportTime(METHOD_IMAGEJ_OPS, ijTime, rawTime, sjTime);
	}

	private long computeDifference(final List<Long> list) {
		final long mem = list.remove(0);
		return list.get(0) - mem;
	}

	private void reportTime(final String label, final long time,
		final long... otherTimes)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("\t");
		sb.append(label);
		sb.append(": ");
		sb.append(time);
		sb.append(" ms");
		for (final long otherTime : otherTimes) {
			sb.append(", ");
			sb.append(time / (float) otherTime);
		}
		System.out.println(sb);
	}

	// -- Creation methods --

	private byte[] createRawData(final int w, final int h) {
		final int size = w * h;
		final int max = w + h;
		final byte[] data = new byte[size];
		int index = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				data[index++] = (byte) (255 * (x + y) / max);
			}
		}
		return data;
	}

	// -- Inversion methods --

	/**
	 * @param data the data to invert
	 * @implNote op name="benchmark.invert",type=Inplace1
	 */
	public static void invertRaw(final byte[] data) {
		for (int i = 0; i < data.length; i++) {
			final int value = data[i] & 0xff;
			final int result = 255 - value;
			data[i] = (byte) result;
		}
	}

	private void invertUsingSciJavaOps(final byte[] data) {
		env.unary("benchmark.invert").input(data).mutate();
	}

	private void invertUsingImageJOps(final byte[] data) {
		ops.run("image.invert", new Object[] { data });
	}

	// -- Randomization methods --

	/**
	 * @param data the data to invert
	 * @implNote op name="benchmark.randomize",type=Inplace1
	 */
	public static void randomizeRaw(final byte[] data) {
		for (int i = 0; i < data.length; i++) {
			final int value = data[i] & 0xff;
			final double result = expensiveOperation(value);
			data[i] = (byte) result;
		}
	}

	private void randomizeUsingSciJavaOps(final byte[] data) {
		env.unary("benchmark.randomize").input(data).mutate();
	}

	private void randomizeUsingImageJOps(final byte[] data) {
		ops.run("math.randomUniform", new Object[] { data });
	}

	private static double expensiveOperation(final int value) {
		return 255 * Math.random() * Math.sin(value / 255.0);
	}

}
