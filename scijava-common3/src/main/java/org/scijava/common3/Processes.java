/*
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.common3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Useful methods for launching external processes.
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public final class Processes {

	private Processes() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Executes the given command, blocking until it completes.
	 *
	 * @param workingDirectory the working directory, or {@code null} to inherit
	 * @param err where to echo the process's standard error, or {@code null}
	 * @param out where to echo the process's standard output, or {@code null}
	 * @param args the command and its arguments
	 * @return everything the process wrote to standard output
	 * @throws RuntimeException if the process fails, or the thread is
	 *           interrupted while waiting for it
	 */
	public static String exec(final File workingDirectory,
		final PrintStream err, final PrintStream out, final String... args)
	{
		return exec(workingDirectory, null, err, out, args);
	}

	/**
	 * Executes the given command, blocking until it completes.
	 *
	 * @param workingDirectory the working directory, or {@code null} to inherit
	 * @param in fed to the process's standard input, or {@code null} for none
	 * @param err where to echo the process's standard error, or {@code null}
	 * @param out where to echo the process's standard output, or {@code null}
	 * @param args the command and its arguments
	 * @return everything the process wrote to standard output
	 * @throws RuntimeException if the process fails, or the thread is
	 *           interrupted while waiting for it
	 */
	public static String exec(final File workingDirectory, final InputStream in,
		final PrintStream err, final PrintStream out, final String... args)
	{
		final Process process;
		try {
			process = new ProcessBuilder(args).directory(workingDirectory).start();
		}
		catch (final IOException exc) {
			throw new RuntimeException(exc);
		}

		// NB: the process's stdin must be closed once the input is exhausted,
		// or the process will wait forever for more. The caller's streams, by
		// contrast, belong to the caller and must be left open.
		final Pump inPump = in == null ? null : //
			new Pump(in, process.getOutputStream(), true);
		final Pump errPump = new Pump(process.getErrorStream(), err, false);
		final Pump outPump = new Pump(process.getInputStream(), out, false);
		if (inPump != null) inPump.start();
		errPump.start();
		outPump.start();

		try {
			process.waitFor();
			if (inPump != null) inPump.join();
			errPump.join();
			outPump.join();
		}
		catch (final InterruptedException exc) {
			process.destroy();
			if (inPump != null) inPump.interrupt();
			errPump.interrupt();
			outPump.interrupt();
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while awaiting " + //
				Arrays.toString(args), exc);
		}

		if (process.exitValue() != 0) {
			throw new RuntimeException("exit status " + process.exitValue() + ": " +
				Arrays.toString(args) + "\n" + errPump);
		}
		return outPump.toString();
	}

	/**
	 * Copies one stream into another, accumulating what passes through so that
	 * it can be returned to the caller afterward.
	 */
	private static class Pump extends Thread {

		private final InputStream input;
		private final OutputStream output;
		private final boolean closeOutput;
		private final ByteArrayOutputStream accumulated =
			new ByteArrayOutputStream();

		Pump(final InputStream input, final OutputStream output,
			final boolean closeOutput)
		{
			setDaemon(true);
			this.input = input;
			this.output = output;
			this.closeOutput = closeOutput;
		}

		@Override
		public void run() {
			final byte[] buffer = new byte[8192];
			try {
				while (true) {
					final int count = input.read(buffer);
					if (count < 0) break;
					accumulated.write(buffer, 0, count);
					if (output != null) {
						output.write(buffer, 0, count);
						output.flush();
					}
				}
			}
			catch (final IOException exc) {
				// NB: the process died, or the stream was closed beneath us.
			}
			finally {
				if (closeOutput && output != null) {
					try {
						output.close();
					}
					catch (final IOException exc) {
						// NB: nothing we can do about it here.
					}
				}
			}
		}

		@Override
		public String toString() {
			// NB: decode once, at the end: a multi-byte character can straddle
			// two reads, and decoding each chunk separately would corrupt it.
			return new String(accumulated.toByteArray(), Charset.defaultCharset());
		}
	}
}
