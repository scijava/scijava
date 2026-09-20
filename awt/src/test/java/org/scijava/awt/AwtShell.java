/*
 * #%L
 * AWT widgets and platform plumbing, with no Swing anywhere.
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

package org.scijava.awt;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.scijava.command3.Commands;
import org.scijava.command3.CommandInfo;
import org.scijava.command3.MenuTree;
import org.scijava.context.Context;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.Runner;

/**
 * The same small application as the Swing and JavaFX shells, in pure AWT.
 * <p>
 * Run it with:
 * </p>
 *
 * <pre>
 * mvn -o -pl scijava-awt test-compile exec:java -Denforcer.skip \
 *   -Dexec.classpathScope=test -Dexec.mainClass=org.scijava.awt.AwtShell
 * </pre>
 * <p>
 * It is the plainest of the three, because AWT is: no progress bar, no
 * scrolling list of matches beyond what {@link List} gives. What it shares
 * with the others is everything that matters - the same commands, the same
 * menu tree walked by the same {@code Menus}, the same dialogs built by the
 * same {@code WidgetPanels}.
 * </p>
 *
 * @author Curtis Rueden
 */
public class AwtShell {

	private final Context context = Context.create();
	private final java.util.List<CommandInfo> commands = entries();
	private final Runner runner;

	private final Frame frame = new Frame("SciJava");
	private final Label status = new Label("Ready");
	private final TextField search = new TextField(24);
	private final List matches = new List(8);

	private java.util.List<CommandInfo> shown = java.util.List.of();

	public AwtShell() {
		runner = Runner.of(java.util.List.of(AwtInputHarvester.of(context)),
			java.util.List.of());

		frame.setMenuBar(AwtMenus.create(MenuTree.of(commands), this::run));
		frame.setLayout(new BorderLayout(4, 4));

		final Panel searchPane = new Panel(new BorderLayout(4, 0));
		searchPane.add(new Label("Search:"), BorderLayout.WEST);
		searchPane.add(search, BorderLayout.CENTER);
		frame.add(searchPane, BorderLayout.NORTH);
		frame.add(matches, BorderLayout.CENTER);
		frame.add(status, BorderLayout.SOUTH);

		search.addTextListener(e -> filter());
		search.addActionListener(e -> runSelected());
		matches.addActionListener(e -> runSelected()); // NB: double-click
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});
		filter();
		frame.setSize(520, 300);
		frame.setLocationRelativeTo(null);
	}

	public static void main(final String... args) {
		Edt.later(() -> new AwtShell().frame.setVisible(true));
	}

	// -- Helper methods --

	/** Gathers what this application can run: commands, and scripts. */
	private java.util.List<CommandInfo> entries() {
		final java.util.List<CommandInfo> entries = new java.util.ArrayList<>(
			Commands.discover(context));
		final java.net.URL url = AwtShell.class.getResource("/scripts");
		if (url != null) {
			try {
				new org.scijava.script3.ScriptFinder().find(java.nio.file.Paths.get(url
					.toURI())).forEach(found -> entries.add(CommandInfo.of(found.script(),
						found.metadata())));
			}
			catch (final java.net.URISyntaxException exc) {
				// NB: no scripts to add, which is not a reason to fail to start.
			}
		}
		return entries;
	}

	/** Narrows the list to the commands whose name contains what was typed. */
	private void filter() {
		final String text = search.getText().trim().toLowerCase(Locale.ROOT);
		shown = commands.stream() //
			.filter(c -> describe(c).toLowerCase(Locale.ROOT).contains(text)) //
			.collect(Collectors.toList());
		matches.removeAll();
		shown.forEach(c -> matches.add(describe(c)));
		if (!shown.isEmpty()) matches.select(0);
		status.setText(shown.size() + " of " + commands.size() + " commands");
	}

	private void runSelected() {
		final int index = matches.getSelectedIndex();
		if (index >= 0 && index < shown.size()) run(shown.get(index));
	}

	/** Runs a command, and says what came back. */
	private void run(final CommandInfo command) {
		status.setText("Running " + command.label() + "...");
		final Future<ExecutionResult> future = runner.run(command, Map.of());
		new Thread(() -> {
			final String message = describe(future);
			Edt.later(() -> status.setText(message));
		}, "shell-" + command.name()).start();
	}

	private static String describe(final CommandInfo command) {
		return command.menuPath().map(path -> path.replace(">", " ▸ ")) //
			.orElseGet(() -> command.label() + " (not in any menu)");
	}

	private static String describe(final Future<ExecutionResult> future) {
		final ExecutionResult result;
		try {
			result = future.get();
		}
		catch (final Exception exc) {
			return "Failed: " + exc.getMessage();
		}
		if (result.isDeclined()) return result.reason().orElse("Declined");
		if (result.outputs().isEmpty()) return "Done";
		return result.outputs().values().stream().map(String::valueOf) //
			.collect(Collectors.joining("; "));
	}
}
