/*
 * #%L
 * Swing widgets, and a dialog to harvest inputs with them.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.swing;

import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.scijava.context.Access;
import org.scijava.context.Context;
import org.scijava.execute.Executables;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.Parameter;
import org.scijava.execute.Parameters;
import org.scijava.execute.Runner;
import org.scijava.harvest.Group;
import org.scijava.struct.ItemIO;
import org.scijava.struct.StructInstance;

/**
 * Dialogs to click on.
 * <p>
 * Run it with:
 * </p>
 *
 * <pre>
 * mvn -pl scijava-swing test-compile exec:java \
 *   -Dexec.classpathScope=test -Dexec.mainClass=org.scijava.swing.Demos
 * </pre>
 * <p>
 * A name on the command line runs that demo straight away; with no name, a
 * window lists them. Each is a behavior that is hard to get right, so that
 * what the dialog does can be seen rather than reasoned about.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Demos {

	private Demos() {
		// prevent instantiation of utility class
	}

	// -- 1. One of everything --

	/** Every widget at once, to see how a full dialog reads. */
	public static class KitchenSink implements Runnable {

		@Parameter(style = "message", required = false)
		private String message = "One of everything the widgets can do.";

		@Parameter(description = "Shown as the tooltip of this parameter's label")
		private String name = "ada";

		@Parameter(min = "0", max = "100", style = "slider,ticks:4")
		private int percent = 50;

		/** Permitted up to a million; the slider spans only the useful part. */
		@Parameter(min = "0", max = "1000000", softMax = "100",
			style = "scroll bar")
		private long iterations = 10;

		@Parameter(min = "0", max = "1", stepSize = "0.01")
		private double weight = 0.25;

		@Parameter(label = "Interpolation")
		private Method method = Method.LINEAR;

		@Parameter(label = "Boxed value, not a primitive", required = false)
		private Integer boxed = 7;

		@Parameter(label = "Exactly, however many digits")
		private java.math.BigDecimal exact = new java.math.BigDecimal("1.25");

		@Parameter
		private char initial = 'a';

		@Parameter
		private boolean enabled = true;

		@Parameter(choices = { "Mean", "Median", "Max" })
		private String statistic = "Mean";

		@Parameter(required = false)
		private File input;

		@Parameter(style = "directory", required = false)
		private File outputDir;

		@Parameter(style = "password", required = false)
		private String secret = "";

		@Parameter(style = "text area", required = false)
		private String notes = "";

		@Parameter(io = ItemIO.OUTPUT)
		private String summary;

		@Override
		public void run() {
			summary = name + " " + percent + "% n=" + iterations + " w=" + weight +
				" " + method + " boxed=" + boxed + " exact=" + exact + " '" +
				initial + "' " + statistic +
				(enabled ? " [enabled]" : "") + " in=" + input + " out=" + outputDir +
				" notes=" + notes.length() + " chars";
		}
	}

	/** Values with names of their own, which need declaring nowhere else. */
	public enum Method {
			NEAREST, LINEAR, CUBIC
	}

	// -- 2. A group that appears when a box is ticked --

	@Group(name = "Advanced", collapsible = true, visibleWhen = "showAdvanced")
	public static class AdvancedToggle implements Runnable {

		@Parameter
		private String file = "data.csv";

		@Parameter(label = "Show advanced options")
		private boolean advanced = false;

		@Parameter(group = "Advanced", min = "1", max = "64")
		private int threads = 4;

		@Parameter(group = "Advanced")
		private boolean verbose = false;

		@Parameter(io = ItemIO.OUTPUT)
		private String summary;

		@SuppressWarnings("unused")
		private boolean showAdvanced() {
			return advanced;
		}

		@Override
		public void run() {
			summary = file + " threads=" + threads + " verbose=" + verbose;
		}
	}

	// -- 3. A group whose size a value decides --

	@Group(name = "Dimensions", membersFrom = "dimensionMembers",
		after = "numDims")
	public static class GeneratedGroup implements Runnable {

		@Parameter
		private String dataset = "stack.tif";

		@Parameter(callback = "dimsChanged", min = "1", max = "5")
		private int numDims = 2;

		private StructInstance<Map<String, Object>> dims = buildDims(2);

		@Parameter(io = ItemIO.OUTPUT)
		private String summary;

		@SuppressWarnings("unused")
		private void dimsChanged() {
			dims = buildDims(numDims);
		}

		@SuppressWarnings("unused")
		private Object dimensionMembers() {
			return dims;
		}

		private static StructInstance<Map<String, Object>> buildDims(
			final int count)
		{
			final Parameters.Builder builder = Parameters.builder();
			for (int i = 0; i < count; i++)
				builder.add("dim" + i, String.class, "d" + i);
			return builder.build();
		}

		@Override
		public void run() {
			summary = dataset + " " + dims.object();
		}
	}

	// -- 4. Choices one value computes for another --

	public static class ComputedChoices implements Runnable {

		@Parameter(choices = { "stack.tif", "table.csv" }, callback = "fileChanged")
		private String file = "stack.tif";

		@Parameter(choicesFrom = "columnChoices")
		private String column = "X";

		@Parameter(io = ItemIO.OUTPUT)
		private String summary;

		@SuppressWarnings("unused")
		private void fileChanged() {
			// NB: a different file has different columns, and the widget beside
			// this one follows without the dialog being told.
			if (!columnChoices().contains(column)) column = columnChoices().get(0);
		}

		private List<String> columnChoices() {
			return file.endsWith(".tif") ? List.of("X", "Y", "Z", "Channel")
				: List.of("id", "label", "area", "mean");
		}

		@Override
		public void run() {
			summary = file + " -> " + column;
		}
	}

	// -- 5. Values checked as they are typed --

	public static class LiveValidation implements Runnable {

		@Parameter(validator = "checkCount")
		private int count = 5;

		@Parameter(validator = "checkName")
		private String name = "ada";

		@Parameter(io = ItemIO.OUTPUT)
		private String summary;

		@SuppressWarnings("unused")
		private String checkCount(final Object value) {
			final int n = (Integer) value;
			return n > 0 && n <= 10 ? null : "Count must be between 1 and 10";
		}

		@SuppressWarnings("unused")
		private String checkName(final Object value) {
			final String s = String.valueOf(value);
			return s.isEmpty() ? "A name is required" : null;
		}

		@Override
		public void run() {
			summary = name + " x" + count;
		}
	}

	// -- 6. Two values that keep each other in step --

	public static class LinkedValues implements Runnable {

		@Parameter(callback = "celsiusChanged", stepSize = "0.5")
		private double celsius = 0;

		@Parameter(callback = "fahrenheitChanged", stepSize = "0.5")
		private double fahrenheit = 32;

		@SuppressWarnings("unused")
		private void celsiusChanged() {
			fahrenheit = celsius * 9 / 5 + 32;
		}

		@SuppressWarnings("unused")
		private void fahrenheitChanged() {
			celsius = (fahrenheit - 32) * 5 / 9;
		}

		@Override
		public void run() {}
	}

	// -- 7. Two values linked only while a third says so --

	/**
	 * The harder version: whether the callbacks do anything is itself a value.
	 * <p>
	 * NB: a callback runs at most once per {@code set}, so width setting height
	 * setting width settles rather than spinning.
	 * </p>
	 */
	public static class LockAspectRatio implements Runnable {

		@Parameter(callback = "widthChanged", min = "1", max = "4096")
		private int width = 800;

		@Parameter(callback = "heightChanged", min = "1", max = "4096")
		private int height = 600;

		@Parameter(label = "Lock aspect ratio", callback = "lockChanged")
		private boolean lock = true;

		@Parameter(style = "message", required = false)
		private String ratio = "800 x 600 (1.333:1), locked";

		private double aspect = 800.0 / 600.0;

		@Parameter(io = ItemIO.OUTPUT)
		private String summary;

		@SuppressWarnings("unused")
		private void widthChanged() {
			if (lock) height = (int) Math.max(1, Math.round(width / aspect));
			else aspect = (double) width / height;
			describe();
		}

		@SuppressWarnings("unused")
		private void heightChanged() {
			if (lock) width = (int) Math.max(1, Math.round(height * aspect));
			else aspect = (double) width / height;
			describe();
		}

		@SuppressWarnings("unused")
		private void lockChanged() {
			// NB: locking adopts the ratio on screen, rather than restoring the
			// one in force when it was last unlocked.
			if (lock) aspect = (double) width / height;
			describe();
		}

		private void describe() {
			ratio = String.format("%d x %d (%.3f:1)%s", width, height, (double) width /
				height, lock ? ", locked" : "");
		}

		@Override
		public void run() {
			summary = width + "x" + height;
		}
	}

	// -- Launcher --

	private static final Map<String, Class<? extends Runnable>> DEMOS =
		new LinkedHashMap<>();

	static {
		DEMOS.put("Kitchen sink", KitchenSink.class);
		DEMOS.put("Advanced toggle", AdvancedToggle.class);
		DEMOS.put("Generated group", GeneratedGroup.class);
		DEMOS.put("Computed choices", ComputedChoices.class);
		DEMOS.put("Live validation", LiveValidation.class);
		DEMOS.put("Linked values", LinkedValues.class);
		DEMOS.put("Lock aspect ratio", LockAspectRatio.class);
	}

	public static void main(final String... args) {
		final Context context = Context.create();
		if (args.length > 0) {
			final String wanted = String.join(" ", args);
			final Class<? extends Runnable> demo = DEMOS.entrySet().stream() //
				.filter(e -> e.getKey().equalsIgnoreCase(wanted)) //
				.map(Map.Entry::getValue).findFirst().orElse(null);
			if (demo == null) {
				System.err.println("No such demo: " + wanted);
				System.err.println("Try one of: " + String.join(", ", DEMOS.keySet()));
				System.exit(1);
			}
			run(context, demo, true);
			return;
		}
		SwingUtilities.invokeLater(() -> launcher(context));
	}

	// -- Helper methods --

	private static void launcher(final Context context) {
		final JFrame frame = new JFrame("SciJava Swing demos");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
		panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		for (final Map.Entry<String, Class<? extends Runnable>> demo : DEMOS
			.entrySet())
		{
			final JButton button = new JButton(demo.getKey());
			button.addActionListener(e -> run(context, demo.getValue(), false));
			panel.add(button);
		}
		frame.setContentPane(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/** Runs one demo, reporting what came back. */
	private static void run(final Context context,
		final Class<? extends Runnable> type, final boolean exitWhenDone)
	{
		final SwingInputHarvester harvester = SwingInputHarvester.of(context);
		harvester.setTitle(type.getSimpleName());
		final Runner runner = Runner.of(List.of(harvester), List.of());
		final Runnable command;
		try {
			command = type.getDeclaredConstructor().newInstance();
		}
		catch (final ReflectiveOperationException exc) {
			throw new IllegalStateException(exc);
		}
		// NB: reflect through a lookup the container supplies, so that a demo
		// needs no `opens` beyond the one this module already declares.
		final Future<ExecutionResult> future = runner.run(Executables.executableOf(
			command, Access.lookupIn(type)), Map.of());
		new Thread(() -> {
			final String message = describe(future);
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(null, message, type.getSimpleName(),
					JOptionPane.INFORMATION_MESSAGE);
				if (exitWhenDone) System.exit(0);
			});
		}, "demo-result").start();
	}

	private static String describe(final Future<ExecutionResult> future) {
		final ExecutionResult result;
		try {
			result = future.get();
		}
		catch (final Exception exc) {
			return "Failed: " + exc;
		}
		if (result.isDeclined()) {
			return "Declined: " + result.reason().orElse("no reason given");
		}
		final List<String> lines = new ArrayList<>();
		result.outputs().forEach((key, value) -> lines.add(key + " = " + value));
		return lines.isEmpty() ? "Completed with no outputs" : String.join("\n",
			lines);
	}
}
