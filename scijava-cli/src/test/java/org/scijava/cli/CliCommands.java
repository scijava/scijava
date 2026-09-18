/*
 * #%L
 * Running commands and scripts from a command line.
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

package org.scijava.cli;

import java.io.File;

import org.scijava.command3.Command;
import org.scijava.command3.Menu;
import org.scijava.context.Plugin;
import org.scijava.execute.Parameter;
import org.scijava.struct.ItemIO;

/**
 * Commands for the command line to find and run.
 *
 * @author Curtis Rueden
 */
public final class CliCommands {

	private CliCommands() {
		// prevent instantiation of utility class
	}

	@Plugin(type = Command.class)
	@Menu(path = "Process>Filters>Gaussian Blur...", weight = 20)
	public static class GaussianBlur implements Command {

		@Parameter(label = "Blur radius", min = "0", max = "20")
		private double sigma = 2;

		@Parameter(choices = { "Reflect", "Zero", "Wrap" })
		private String edges = "Reflect";

		@Parameter(required = false)
		private File input;

		@Parameter(io = ItemIO.OUTPUT)
		private String result;

		@Override
		public void run() {
			result = "blurred sigma=" + sigma + " edges=" + edges;
		}
	}

	@Plugin(type = Command.class)
	@Menu(path = "Analyze>Measure...", weight = 30)
	public static class Measure implements Command {

		@Parameter
		private String column = "X";

		@Parameter(io = ItemIO.OUTPUT)
		private String measured;

		@Override
		public void run() {
			measured = "measured " + column;
		}
	}

	/** Two commands whose labels overlap, to make a lookup ambiguous. */
	@Plugin(type = Command.class)
	@Menu(path = "Image>Adjust>Brightness...", weight = 10)
	public static class Brightness implements Command {

		@Parameter
		private int level = 50;

		@Parameter(io = ItemIO.OUTPUT)
		private String adjusted;

		@Override
		public void run() {
			adjusted = "brightness " + level;
		}
	}

	@Plugin(type = Command.class)
	@Menu(path = "Image>Adjust>Brightness/Contrast...", weight = 11)
	public static class BrightnessContrast implements Command {

		@Parameter
		private int level = 50;

		@Parameter(io = ItemIO.OUTPUT)
		private String adjusted;

		@Override
		public void run() {
			adjusted = "brightness/contrast " + level;
		}
	}

	/** A command that declines, to show what that looks like from a shell. */
	@Plugin(type = Command.class)
	@Menu(path = "File>Save", weight = 1)
	public static class Save implements Command {

		@Parameter
		private boolean fail = false;

		@Parameter(io = ItemIO.OUTPUT)
		private String saved;

		@Override
		public void run() {
			if (fail) throw new IllegalStateException("Nothing to save");
			saved = "saved";
		}
	}

	/** Runnable, but in no menu: reachable by name and nothing else. */
	@Plugin(type = Command.class)
	public static class Hidden implements Command {

		@Parameter(io = ItemIO.OUTPUT)
		private String ran = "the hidden command ran";

		@Override
		public void run() {}
	}
}
