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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scijava.command.Commands;
import org.scijava.command.CommandInfo;
import org.scijava.command.MenuTree;
import org.scijava.context.Context;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.Runner;
import org.scijava.script3.ScriptFinder;

/**
 * A small application: menus, a search bar, a status line.
 * <p>
 * Run it with:
 * </p>
 *
 * <pre>
 * mvn -o -pl scijava-swing test-compile exec:java -Denforcer.skip \
 *   -Dexec.classpathScope=test -Dexec.mainClass=org.scijava.swing.Shell
 * </pre>
 * <p>
 * It is the first thing to put the whole stack together: commands declared in
 * {@code SampleCommands} and found through the annotation index - and Groovy
 * scripts found in a directory, which arrive in the same menus through the
 * same {@code CommandInfo} - arranged by
 * {@link MenuTree}, rendered by {@link SwingMenus}, run by
 * {@link org.scijava.execute.Runner} and filled in by
 * {@link SwingInputHarvester}. Starting it loads no command class; choosing
 * one loads that one.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Shell {

	private final Context context = Context.create();
	private final List<CommandInfo> commands = entries();
	private final Runner runner;

	private final JFrame frame = new JFrame("SciJava");
	private final JLabel status = new JLabel("Ready");
	private final JProgressBar progress = new JProgressBar();
	private final JTextField search = new JTextField(20);
	private final DefaultListModel<CommandInfo> matchModel =
		new DefaultListModel<>();
	private final JList<CommandInfo> matches = new JList<>(matchModel);

	public Shell() {
		final SwingInputHarvester harvester = SwingInputHarvester.of(context);
		runner = Runner.of(List.of(harvester), List.of());

		frame.setJMenuBar(SwingMenus.create(MenuTree.of(commands), this::run));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(content());
		frame.setPreferredSize(new Dimension(520, 260));
		frame.pack();
		frame.setLocationRelativeTo(null);
	}

	public static void main(final String... args) {
		SwingUtilities.invokeLater(() -> new Shell().frame.setVisible(true));
	}

	// -- Helper methods --

	/**
	 * Gathers what this application can run, from every source it has.
	 * <p>
	 * NB: this is the shape the legacy bridge wants too. A menu is built from
	 * {@code CommandInfo}s, and where each came from - the annotation index, a
	 * directory of scripts, in time a SciJava Common {@code ModuleInfo} - is
	 * the application's business and nobody else's.
	 * </p>
	 */
	private List<CommandInfo> entries() {
		final List<CommandInfo> entries = new ArrayList<>(Commands.discover(
			context));
		scriptsDirectory().ifPresent(dir -> new ScriptFinder().find(dir).forEach( //
			found -> entries.add(CommandInfo.of(found.script(), found.metadata()))));
		return entries;
	}

	/** Finds the demo scripts, wherever the test resources landed. */
	private static Optional<Path> scriptsDirectory() {
		final URL url = Shell.class.getResource("/scripts");
		if (url == null) return Optional.empty();
		try {
			return Optional.of(Paths.get(url.toURI()));
		}
		catch (final URISyntaxException exc) {
			return Optional.empty();
		}
	}

	private JPanel content() {
		matches.setVisibleRowCount(6);
		matches.setCellRenderer((list, value, index, selected, focused) -> {
			final JLabel label = new JLabel(describe(value));
			label.setOpaque(true);
			if (selected) {
				label.setBackground(list.getSelectionBackground());
				label.setForeground(list.getSelectionForeground());
			}
			return label;
		});

		search.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(final DocumentEvent e) {
				filter();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				filter();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				filter();
			}
		});
		// NB: down from the field into the list, Enter to run - the whole point
		// being that a command need not be in a menu to be reachable.
		search.addActionListener(e -> runSelected());
		search.addKeyListener(new java.awt.event.KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DOWN && !matchModel.isEmpty()) {
					matches.setSelectedIndex(Math.min(matches.getSelectedIndex() + 1,
						matchModel.size() - 1));
				}
				else if (e.getKeyCode() == KeyEvent.VK_UP) {
					matches.setSelectedIndex(Math.max(matches.getSelectedIndex() - 1, 0));
				}
			}
		});
		matches.addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mouseClicked(final java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) runSelected();
			}
		});
		filter();

		final JPanel searchPane = new JPanel(new BorderLayout(4, 4));
		searchPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		searchPane.add(new JLabel("Search:"), BorderLayout.WEST);
		searchPane.add(search, BorderLayout.CENTER);

		final JPanel south = new JPanel(new BorderLayout());
		progress.setVisible(false);
		south.add(progress, BorderLayout.NORTH);
		status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		south.add(status, BorderLayout.SOUTH);

		final JPanel content = new JPanel(new BorderLayout());
		content.add(searchPane, BorderLayout.NORTH);
		content.add(new JScrollPane(matches), BorderLayout.CENTER);
		content.add(south, BorderLayout.SOUTH);
		return content;
	}

	/** Narrows the list to the commands whose name contains what was typed. */
	private void filter() {
		final String text = search.getText().trim().toLowerCase(Locale.ROOT);
		final List<CommandInfo> found = commands.stream() //
			.filter(c -> describe(c).toLowerCase(Locale.ROOT).contains(text)) //
			.collect(Collectors.toList());
		matchModel.clear();
		found.forEach(matchModel::addElement);
		if (!matchModel.isEmpty()) matches.setSelectedIndex(0);
		status.setText(found.size() + " of " + commands.size() + " commands");
	}

	private void runSelected() {
		final CommandInfo command = matches.getSelectedValue();
		if (command != null) run(command);
	}

	/** Runs a command, and says what came back. */
	private void run(final CommandInfo command) {
		status.setText("Running " + command.label() + "...");
		progress.setIndeterminate(true);
		progress.setVisible(true);
		final Future<ExecutionResult> future = runner.run(command, Map.of());
		new Thread(() -> {
			final String message = describe(future);
			SwingUtilities.invokeLater(() -> {
				progress.setVisible(false);
				progress.setIndeterminate(false);
				status.setText(message);
			});
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
		if (result.isDeclined()) {
			return result.reason().orElse("Declined");
		}
		if (result.outputs().isEmpty()) return "Done";
		return result.outputs().entrySet().stream() //
			.map(e -> String.valueOf(e.getValue())) //
			.collect(Collectors.joining("; "));
	}
}
