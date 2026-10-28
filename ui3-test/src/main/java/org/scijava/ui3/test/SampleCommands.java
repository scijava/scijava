/*
 * #%L
 * Sample commands and conformance tests, for every toolkit binding.
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

package org.scijava.ui3.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.scijava.command3.Command;
import org.scijava.command3.Menu;
import org.scijava.context.Plugin;
import org.scijava.execute.Parameter;
import org.scijava.execute.Parameters;
import org.scijava.harvest.Group;
import org.scijava.struct.ItemIO;
import org.scijava.struct.StructInstance;

/**
 * The parameters every toolkit binding is asked to render.
 * <p>
 * They live here, rather than beside one toolkit, so that Swing and JavaFX
 * demonstrate and are tested against exactly the same declarations - which is
 * the only way to notice that a binding has quietly diverged.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class SampleCommands {

	private SampleCommands() {
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


	// -- Commands with menu paths, for an application shell --

	@Plugin(type = Command.class)
	@Menu(path = "File>Open...", weight = 1, accelerator = "^O")
	public static class Open implements Command {

		@Parameter
		private File file;

		@Parameter(io = ItemIO.OUTPUT)
		private String opened;

		@Override
		public void run() {
			opened = "Opened " + file;
		}
	}

	@Plugin(type = Command.class)
	@Menu(path = "File>Quit", weight = 90, accelerator = "^Q")
	public static class Quit implements Command {

		@Override
		public void run() {
			// NB: a command reaching the application it runs in is what an
			// application layer is for; until there is one, the demo exits.
			System.exit(0);
		}
	}

	@Plugin(type = Command.class)
	@Menu(path = "Image>Adjust>Brightness/Contrast...", weight = 10,
		accelerator = "^+C")
	public static class BrightnessContrast implements Command {

		@Parameter(min = "0", max = "255", style = "slider,ticks:4")
		private int minimum = 0;

		@Parameter(min = "0", max = "255", style = "slider,ticks:4")
		private int maximum = 255;

		@Parameter(io = ItemIO.OUTPUT)
		private String range;

		@Override
		public void run() {
			range = "Display range: " + minimum + "-" + maximum;
		}
	}

	@Plugin(type = Command.class)
	@Menu(path = "Process>Filters>Gaussian Blur...", weight = 20)
	@Group(name = "Advanced", collapsible = true, visibleWhen = "showAdvanced")
	public static class GaussianBlur implements Command {

		@Parameter(min = "0", softMax = "20", stepSize = "0.1",
			validator = "checkSigma")
		private double sigma = 2.0;

		@Parameter(label = "Show advanced options")
		private boolean advanced = false;

		@Parameter(group = "Advanced", choices = { "Reflect", "Zero", "Wrap" })
		private String edges = "Reflect";

		@Parameter(group = "Advanced", min = "1", max = "64")
		private int threads = 4;

		@Parameter(io = ItemIO.OUTPUT)
		private String applied;

		@SuppressWarnings("unused")
		private boolean showAdvanced() {
			return advanced;
		}

		@SuppressWarnings("unused")
		private String checkSigma(final Object value) {
			return (Double) value > 0 ? null : "Sigma must be greater than zero";
		}

		@Override
		public void run() {
			applied = "Blurred with sigma " + sigma + ", " + edges + " edges, " +
				threads + " threads";
		}
	}

	@Plugin(type = Command.class)
	@Menu(path = "Analyze>Measure...", weight = 30, accelerator = "^M")
	public static class Measure implements Command {

		@Parameter(callback = "sourceChanged", choices = { "stack.tif",
			"table.csv" })
		private String source = "stack.tif";

		@Parameter(choicesFrom = "columnChoices")
		private String column = "X";

		@Parameter(io = ItemIO.OUTPUT)
		private String measured;

		@SuppressWarnings("unused")
		private void sourceChanged() {
			if (!columnChoices().contains(column)) column = columnChoices().get(0);
		}

		private List<String> columnChoices() {
			return source.endsWith(".tif") ? List.of("X", "Y", "Z", "Channel")
				: List.of("id", "label", "area", "mean");
		}

		@Override
		public void run() {
			measured = "Measured " + column + " of " + source;
		}
	}

	@Plugin(type = Command.class)
	@Menu(path = "Help>About...", weight = 100)
	public static class About implements Command {

		@Parameter(style = "message", required = false)
		private String about = "A SciJava3 application: menus from the " +
			"annotation index, dialogs from whichever toolkit binding is running.";

		@Override
		public void run() {}
	}

	/** Not in any menu, but perfectly runnable - and findable by searching. */
	@Plugin(type = Command.class)
	public static class Hidden implements Command {

		@Parameter(io = ItemIO.OUTPUT)
		private String ran = "The hidden command ran";

		@Override
		public void run() {}
	}
}
