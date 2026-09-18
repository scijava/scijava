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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.scijava.command3.CommandInfo;
import org.scijava.command3.Commands;
import org.scijava.context.Context;

/**
 * Tests running commands from a command line.
 *
 * @author Curtis Rueden
 */
public class CliTest {

	/** A command is named by its menu path, and its inputs are options. */
	@Test
	public void testRunByMenuPath() {
		final Run run = cli("Process>Filters>Gaussian Blur...", "--sigma", "2.5",
			"--edges", "Wrap");

		assertEquals(0, run.code);
		assertEquals("result = blurred sigma=2.5 edges=Wrap", run.out.trim());
	}

	/** Or by its label, in whatever case, ellipsis or no ellipsis. */
	@Test
	public void testRunByLabel() {
		assertEquals("result = blurred sigma=2.0 edges=Reflect", cli(
			"gaussian blur").out.trim());
	}

	/** Or by the tail of its class name, which is what a script would use. */
	@Test
	public void testRunByClassName() {
		assertEquals("ran = the hidden command ran", cli("Hidden").out.trim());
	}

	/** An option's value may follow it or be joined with an equals sign. */
	@Test
	public void testEqualsSyntax() {
		assertEquals("result = blurred sigma=3.0 edges=Reflect", cli(
			"gaussian blur", "--sigma=3").out.trim());
	}

	/** A bare option is true, which is what a boolean parameter wants. */
	@Test
	public void testFlagSyntax() {
		final Run run = cli("File>Save", "--fail");

		assertEquals(1, run.code, run.err);
		assertTrue(run.err.contains("Nothing to save"), run.err);
	}

	/** Strings from the shell become whatever the parameters declared. */
	@Test
	public void testInputsAreConverted() {
		// NB: "2.5" reaches a double, and convert3 did it -- the same conversion
		// a script or a dialog gets.
		assertTrue(cli("gaussian blur", "--sigma", "2.5").out.contains(
			"sigma=2.5"));
	}

	/** An unknown command says so, and says how to look. */
	@Test
	public void testUnknownCommand() {
		final Run run = cli("no-such-thing");

		assertEquals(2, run.code);
		assertTrue(run.err.contains("No such command"), run.err);
		assertTrue(run.err.contains("--list"), run.err);
	}

	/** An ambiguous one lists the candidates rather than guessing. */
	@Test
	public void testAmbiguousCommand() {
		final Run run = cli("Bright");

		assertEquals(2, run.code);
		assertTrue(run.err.contains("Which did you mean?"), run.err);
		assertTrue(run.err.contains("Brightness..."), run.err);
		assertTrue(run.err.contains("Brightness/Contrast..."), run.err);
	}

	/**
	 * An exact label wins over a partial one, so the more specific command
	 * being present does not make the plainer one unreachable.
	 */
	@Test
	public void testExactLabelBeatsPartial() {
		final Run run = cli("Brightness", "--level", "10");

		assertEquals(0, run.code, run.err);
		assertEquals("adjusted = brightness 10", run.out.trim());
	}

	/** Listing shows what there is, filtered if asked. */
	@Test
	public void testList() {
		final Run all = cli("--list");
		assertTrue(all.out.contains("Gaussian Blur..."), all.out);
		assertTrue(all.out.contains("Measure..."), all.out);

		final Run filtered = cli("--list", "filters");
		assertTrue(filtered.out.contains("Gaussian Blur..."), filtered.out);
		assertTrue(!filtered.out.contains("Measure..."), filtered.out);
	}

	/** Help describes a command's parameters, from the same metadata. */
	@Test
	public void testHelpForACommand() {
		final Run run = cli("--help", "gaussian blur");

		assertEquals(0, run.code);
		assertTrue(run.out.contains("--sigma <double>"), run.out);
		assertTrue(run.out.contains("Blur radius"), run.out);
		assertTrue(run.out.contains("one of: Reflect,Zero,Wrap"), run.out);
		assertTrue(run.out.contains("(optional)"), run.out);
	}

	/** A script file is run by its path, no menu or index involved. */
	@Test
	public void testRunAScript(@TempDir final Path dir) throws Exception {
		final Path script = dir.resolve("greet.groovy");
		Files.write(script, ("" + //
			"#@ String name\n" + //
			"#@output String greeting\n" + //
			"greeting = \"hello \" + name\n").getBytes(StandardCharsets.UTF_8));

		final Run run = cli(script.toString(), "--name", "ada");

		assertEquals(0, run.code, run.err);
		assertEquals("greeting = hello ada", run.out.trim());
	}

	/** Scripts in a directory join the commands, and are named as commands. */
	@Test
	public void testScriptsDirectory(@TempDir final Path dir) throws Exception {
		final Path scripts = Files.createDirectories(dir.resolve("Plugins"));
		Files.write(scripts.resolve("Say_Hello.groovy"), ("" + //
			"#@ String(value = \"world\") name\n" + //
			"#@output String greeting\n" + //
			"greeting = \"hello \" + name\n").getBytes(StandardCharsets.UTF_8));

		final Run run = cli(dir, "Plugins>Say Hello");

		assertEquals(0, run.code, run.err);
		assertEquals("greeting = hello world", run.out.trim());
	}

	/** With nothing to run, it explains itself rather than doing nothing. */
	@Test
	public void testUsage() {
		final Run run = cli();

		assertEquals(2, run.code);
		assertTrue(run.out.contains("Usage: scijava"), run.out);
	}

	// -- Helper methods --

	/** What a run produced. */
	private static class Run {

		int code;
		String out;
		String err;
	}

	private static Run cli(final String... args) {
		return cli(null, args);
	}

	/** Runs a command line over the test commands, and captures its output. */
	private static Run cli(final Path scriptsDirectory, final String... args) {
		final Run run = new Run();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ByteArrayOutputStream err = new ByteArrayOutputStream();
		try (Context context = Context.create();
				PrintStream outStream = new PrintStream(out, true,
					StandardCharsets.UTF_8);
				PrintStream errStream = new PrintStream(err, true,
					StandardCharsets.UTF_8))
		{
			final List<CommandInfo> commands = new java.util.ArrayList<>(Commands
				.discover(context));
			if (scriptsDirectory != null) {
				new org.scijava.script3.ScriptFinder().find(scriptsDirectory).forEach(
					found -> commands.add(CommandInfo.of(found.script(), found
						.metadata())));
			}
			run.code = new Cli(commands, outStream, errStream).execute(Arguments
				.parse(args));
		}
		run.out = new String(out.toByteArray(), StandardCharsets.UTF_8);
		run.err = new String(err.toByteArray(), StandardCharsets.UTF_8);
		return run;
	}
}
