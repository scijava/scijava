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

import java.io.File;
import java.util.List;

import org.scijava.command.Command;
import org.scijava.command.Menu;
import org.scijava.context.Plugin;
import org.scijava.execute.Parameter;
import org.scijava.harvest.Group;
import org.scijava.struct.ItemIO;

/**
 * The commands the demo shell puts in its menus.
 * <p>
 * Nothing here knows that a menu, a dialog or Swing exists: each declares what
 * it needs and what it produces, and is found through the annotation index.
 * </p>
 * <p>
 * NB: the weights put the menus in the familiar order. A branch takes the
 * weight of its lightest leaf, so it is enough for one command in each
 * top-level menu to say where that menu belongs.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class ShellCommands {

	private ShellCommands() {
		// prevent instantiation of utility class
	}

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
			"annotation index, dialogs from scijava-swing.";

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
