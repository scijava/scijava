/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2021 SciJava developers.
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

package org.scijava.log2;

import static org.junit.Assert.assertTrue;
import static org.scijava.log2.LogLevel.WARN;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

/**
 * Tests {@link StderrLogFactory}.
 * 
 * @author Johannes Schindelin
 * @author Matthias Arzt
 */
public class StderrLogServiceTest {

	@Test
	public void testDefaultLevel() {
		final Logger log = new StderrLogFactory().create();
		int level = log.getLevel();
		assertTrue("default level (" + level + //
			") is at least INFO(" + WARN + ")", level >= WARN);
	}

	@Test
	public void testOutputToStream() {
		// setup
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final PrintStream p = new PrintStream(outputStream);
		final Logger log = new StderrLogFactory(ignore -> p).create();

		final String text1 = "Hello World!";
		final String text2 = "foo bar";

		// process
		log.warn(text1);
		log.subLogger("sub").error(text2);

		// test
		assertTrue(outputStream.toString().contains(text1));
		assertTrue(outputStream.toString().contains(text2));
	}
}
