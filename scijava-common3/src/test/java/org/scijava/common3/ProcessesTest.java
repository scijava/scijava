/*-
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Processes}.
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class ProcessesTest {

	@Test
	public void testStdout() {
		assumePOSIX();
		assertEquals("hi\n", Processes.exec(null, null, null, "echo", "hi"));
	}

	/**
	 * Feeding a process requires closing its standard input once the input is
	 * exhausted; otherwise {@code cat} would wait forever for more.
	 */
	@Test
	public void testStdin() {
		assumePOSIX();
		final String value = "Hello, World!\n";
		final InputStream input = new ByteArrayInputStream(value.getBytes());
		assertEquals(value, Processes.exec(null, input, null, null, "cat"));
	}

	/** A multi-byte character must survive being split across two reads. */
	@Test
	public void testMultiByteOutput() {
		assumePOSIX();
		final String value = "éèê";
		final InputStream input = new ByteArrayInputStream(value.getBytes());
		assertEquals(value, Processes.exec(null, input, null, null, "cat"));
	}

	@Test
	public void testNonZeroExitStatus() {
		assumePOSIX();
		assertThrows(RuntimeException.class, //
			() -> Processes.exec(null, null, null, "false"));
	}

	@Test
	public void testInterruptible() throws InterruptedException {
		assumePOSIX();
		final SleepThread thread = new SleepThread(5);
		thread.start();
		Thread.sleep(100);
		thread.interrupt();
		thread.join(10000);
		assertNotNull(thread.result, "exec did not return when interrupted");
	}

	private void assumePOSIX() {
		assumeTrue(Platforms.isPOSIX());
	}

	private static class SleepThread extends Thread {

		private final int seconds;
		private Throwable result;

		SleepThread(final int seconds) {
			this.seconds = seconds;
		}

		@Override
		public void run() {
			try {
				Processes.exec(null, null, null, "sleep", "" + seconds);
			}
			catch (final Throwable t) {
				result = t;
			}
		}
	}
}
