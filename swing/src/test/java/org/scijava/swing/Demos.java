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
import org.scijava.execute.Runner;
import org.scijava.ui3.test.SampleCommands;

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
 * <p>
 * The commands themselves are in {@link SampleCommands}, shared with every
 * other toolkit binding, so that what Swing shows and what JavaFX shows are
 * the same declarations.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Demos {

	private Demos() {
		// prevent instantiation of utility class
	}

	// -- Launcher --

	private static final Map<String, Class<? extends Runnable>> DEMOS =
		new LinkedHashMap<>();

	static {
		DEMOS.put("Kitchen sink", SampleCommands.KitchenSink.class);
		DEMOS.put("Advanced toggle", SampleCommands.AdvancedToggle.class);
		DEMOS.put("Generated group", SampleCommands.GeneratedGroup.class);
		DEMOS.put("Computed choices", SampleCommands.ComputedChoices.class);
		DEMOS.put("Live validation", SampleCommands.LiveValidation.class);
		DEMOS.put("Linked values", SampleCommands.LinkedValues.class);
		DEMOS.put("Lock aspect ratio", SampleCommands.LockAspectRatio.class);
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
