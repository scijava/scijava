/*
 * #%L
 * Presenting SciJava Common's modules as SciJava3 commands.
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

package org.scijava.bridge;

import java.io.File;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Commands written the SciJava Common way: the old annotations, the old
 * interfaces, the old everything.
 * <p>
 * NB: nothing here imports a single SciJava3 class. That is the test - this is
 * what the existing ecosystem looks like, and the bridge has to take it as it
 * finds it.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class LegacyCommands {

	private LegacyCommands() {
		// prevent instantiation of utility class
	}

	/** An ordinary legacy command, with the metadata one carries. */
	@Plugin(type = Command.class, menu = { //
		@Menu(label = "Process", weight = 20), //
		@Menu(label = "Filters"), //
		@Menu(label = "Legacy Blur...", weight = 7, accelerator = "^L") }, //
		iconPath = "/icons/blur.png")
	public static class LegacyBlur implements Command {

		@Parameter(label = "Blur radius", min = "0", max = "20", stepSize = "0.5",
			style = "slider")
		private double sigma = 2;

		@Parameter(choices = { "Reflect", "Zero", "Wrap" })
		private String edges = "Reflect";

		@Parameter(required = false)
		private File input;

		@Parameter(type = ItemIO.OUTPUT)
		private String result;

		@Override
		public void run() {
			result = "Legacy blur: sigma=" + sigma + " edges=" + edges;
		}
	}

	/** A legacy command with a callback, the SciJava Common way. */
	@Plugin(type = Command.class, menuPath = "Edit>Legacy Temperature")
	public static class LegacyTemperature implements Command {

		@Parameter(callback = "celsiusChanged")
		private double celsius = 0;

		@Parameter
		private double fahrenheit = 32;

		@SuppressWarnings("unused")
		private void celsiusChanged() {
			fahrenheit = celsius * 9 / 5 + 32;
		}

		@Override
		public void run() {}
	}

	/**
	 * A legacy command with a validator.
	 * <p>
	 * NB: it <em>throws</em> to report a bad value, which is what released
	 * SciJava Common honors. Returning the message instead works on its
	 * development line (the validation mechanism was improved in March 2026,
	 * after 2.99.2), and the bridge maps either onto SciJava3's single
	 * protocol - so this fixture tests the one every existing command in the
	 * wild is written against.
	 * </p>
	 */
	@Plugin(type = Command.class, menuPath = "Edit>Legacy Validation")
	public static class LegacyValidation implements Command {

		@Parameter(validater = "checkCount")
		private int count = 5;

		@SuppressWarnings("unused")
		private void checkCount() {
			if (count < 1 || count > 10) {
				throw new IllegalArgumentException(
					"Count must be between 1 and 10");
			}
		}

		@Override
		public void run() {}
	}

	/**
	 * The evil one: a command that builds its own parameters at run time.
	 * <p>
	 * NB: {@code DynamicCommand} is what SciJava3 deliberately has no
	 * equivalent of - and the bridge does not need one, because a
	 * {@code ModuleInfo} reports whatever parameters it has by the time it is
	 * asked. What SciJava3 refuses to <em>offer</em> as a way of writing new
	 * commands, it can still <em>present</em>.
	 * </p>
	 */
	@Plugin(type = Command.class, menuPath = "Analyze>Legacy Dynamic")
	public static class LegacyDynamic extends DynamicCommand {

		@Parameter
		private String name = "ada";

		@Override
		public void initialize() {
			final MutableModuleItem<Integer> added = addInput("addedAtRuntime",
				Integer.class);
			added.setLabel("Added at run time");
			added.setValue(this, 42);
		}

		@Override
		public void run() {}
	}
}
