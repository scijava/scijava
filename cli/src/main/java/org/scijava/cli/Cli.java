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

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.scijava.command3.CommandInfo;
import org.scijava.command3.Commands;
import org.scijava.context.Context;
import org.scijava.execute.ExecutionResult;
import org.scijava.execute.ParameterMember;
import org.scijava.execute.Runner;
import org.scijava.script3.ScriptFinder;
import org.scijava.script3.Scripts;
import org.scijava.struct.Member;

/**
 * Running commands and scripts from a command line.
 * <p>
 * The same commands the menus show, run without a display:
 * </p>
 *
 * <pre>
 * scijava "Gaussian Blur" --sigma 2.5 --edges Wrap
 * scijava blur.groovy --sigma=2.5
 * scijava --list filters
 * scijava --help "Gaussian Blur"
 * </pre>
 * <p>
 * NB: there is no separate notion of a "headless command" here. A command
 * declares its inputs, the command line supplies them, and a run that needs
 * something it cannot get is declined with a reason - the same mechanism a
 * dismissed dialog uses. SciJava Common needed {@code canRunHeadless()}
 * because harvesting was tangled with the UI; here the harvester is simply one
 * preprocessor among several, and this runner does not install it.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Cli {

	private final List<CommandInfo> commands;
	private final PrintStream out;
	private final PrintStream err;

	/** Creates a command line over exactly the given commands. */
	public Cli(final List<CommandInfo> commands, final PrintStream out,
		final PrintStream err)
	{
		this.commands = List.copyOf(commands);
		this.out = out;
		this.err = err;
	}

	/**
	 * Runs a command line, and exits with what it returns.
	 *
	 * @param args the command line
	 */
	public static void main(final String... args) {
		System.exit(run(args));
	}

	/**
	 * Runs a command line.
	 *
	 * @param args the command line
	 * @return the exit code: 0 for a completed run, 1 for a failed or declined
	 *         one, 2 for a command line that could not be understood
	 */
	public static int run(final String... args) {
		final Arguments parsed = Arguments.parse(args);
		try (Context context = Context.create()) {
			final List<CommandInfo> commands = gather(context, parsed.take(
				"scripts"));
			return new Cli(commands, System.out, System.err).execute(parsed);
		}
	}

	/**
	 * Runs an already-parsed command line.
	 *
	 * @param args the command line, with any global options already taken
	 * @return the exit code
	 */
	public int execute(final Arguments args) {
		if (args.has("list")) {
			final String filter = args.take("list");
			list("true".equals(filter) ? null : filter);
			return 0;
		}

		if (args.has("help")) {
			// NB: `--help blur` gives "blur" as the option's value, while
			// `blur --help` leaves it a bare flag with "blur" among the words.
			// Both mean the same thing and both are what people type.
			final String value = args.take("help");
			final String target = "true".equals(value) ? args.identifier() : value;
			if (target.isEmpty()) {
				usage();
				return 0;
			}
			final CommandInfo command = resolve(target);
			if (command == null) return 2;
			describe(command);
			return 0;
		}

		final String identifier = args.identifier();
		if (identifier.isEmpty()) {
			usage();
			return 2;
		}

		final CommandInfo command = resolve(identifier);
		if (command == null) return 2;
		return execute(command, inputs(args));
	}

	// -- Helper methods --

	/**
	 * Gathers what this command line can run.
	 * <p>
	 * NB: the annotation index, and a scripts directory if one was named. The
	 * legacy bridge is deliberately not wired in here: an application decides
	 * which worlds it contains, and a command line for SciJava3 alone should
	 * not drag SciJava Common onto the classpath.
	 * </p>
	 */
	private static List<CommandInfo> gather(final Context context,
		final String scriptsDirectory)
	{
		final List<CommandInfo> commands = new ArrayList<>(Commands.discover(
			context));
		if (scriptsDirectory != null) {
			final Path directory = Paths.get(scriptsDirectory);
			new ScriptFinder().find(directory).forEach(found -> commands.add(
				CommandInfo.of(found.script(), found.metadata())));
		}
		return commands;
	}

	/** Finds the command, or explains why it could not. */
	private CommandInfo resolve(final String identifier) {
		// NB: a file that exists is a script, whatever else it might name.
		final Path path = Paths.get(identifier);
		if (Files.isRegularFile(path)) {
			try {
				return CommandInfo.of(Scripts.get().of(path), Map.of());
			}
			catch (final RuntimeException exc) {
				err.println("Cannot read script: " + identifier);
				err.println("  " + exc.getMessage());
				return null;
			}
		}

		final CommandLookup.Result found = new CommandLookup(commands).find(
			identifier);
		if (found.isFound()) return found.command();
		if (found.isAmbiguous()) {
			err.println("Which did you mean?");
			found.matches().forEach(c -> err.println("  " + summarize(c)));
			return null;
		}
		err.println("No such command: " + identifier);
		err.println("Try --list to see what there is.");
		return null;
	}

	/** Runs a command with the given inputs, and reports what came back. */
	private int execute(final CommandInfo command,
		final Map<String, Object> inputs)
	{
		final Runner runner = Runner.of(List.of(), List.of());
		try {
			final ExecutionResult result = runner.run(command, inputs).get();
			if (result.isDeclined()) {
				err.println("Declined: " + result.reason().orElse("no reason given"));
				return 1;
			}
			result.outputs().forEach((key, value) -> out.println(key + " = " +
				value));
			return 0;
		}
		catch (final InterruptedException exc) {
			Thread.currentThread().interrupt();
			err.println("Interrupted");
			return 1;
		}
		catch (final ExecutionException exc) {
			final Throwable cause = exc.getCause() == null ? exc : exc.getCause();
			err.println(cause.getMessage() == null ? cause.toString() : cause
				.getMessage());
			return 1;
		}
	}

	/** Gets the options as inputs, which conversion will type on binding. */
	private static Map<String, Object> inputs(final Arguments args) {
		return new LinkedHashMap<>(args.options());
	}

	private void list(final String filter) {
		final List<CommandInfo> found = new CommandLookup(commands).list(filter);
		found.forEach(c -> out.println(summarize(c)));
		out.println();
		out.println(found.size() + " of " + commands.size() + " commands");
	}

	/** Prints what a command takes and what it produces. */
	private void describe(final CommandInfo command) {
		out.println(summarize(command));
		final List<Member<?>> members = command.struct().members();
		if (members.isEmpty()) {
			out.println("  (no parameters)");
			return;
		}
		for (final Member<?> member : members) {
			final StringBuilder line = new StringBuilder("  ");
			line.append(member.isInput() ? "--" + member.key() : "output " + member
				.key());
			line.append(" <").append(typeName(member)).append(">");
			if (member instanceof ParameterMember) {
				final ParameterMember<?> p = (ParameterMember<?>) member;
				p.attr(ParameterMember.LABEL).ifPresent(l -> line.append("  ")
					.append(l));
				p.attr(ParameterMember.CHOICES).ifPresent(c -> line.append("  one of: ")
					.append(c));
				if (member.isInput() && !member.isRequired()) line.append("  (optional)");
			}
			out.println(line);
		}
	}

	private static String typeName(final Member<?> member) {
		final Class<?> raw = org.scijava.common3.Types.raw(member.type());
		return raw == null ? String.valueOf(member.type()) : raw.getSimpleName();
	}

	/** Gets one line naming a command: where it lives, and what it is. */
	private static String summarize(final CommandInfo command) {
		// NB: something in no menu is listed by name and said to be so, rather
		// than by a label that falls back to the very name beside it.
		return command.menuPath().map(p -> p + "  [" + command.name() + "]") //
			.orElseGet(() -> command.name() + "  (not in any menu)");
	}

	private void usage() {
		out.println("Usage: scijava [options] <command> [--parameter value ...]");
		out.println();
		out.println("  --list [text]      list the commands, optionally filtered");
		out.println("  --help <command>   describe a command's parameters");
		out.println("  --scripts <dir>    also offer the scripts in a directory");
		out.println();
		out.println("A command is named by its menu path, its label, or its");
		out.println("class; a script, by its file name.");
	}
}
