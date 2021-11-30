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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DefaultLogger}
 *
 * @author Matthias Arzt
 */
public class DefaultLoggerTest {

	private static Logger logger;
	private static TestLogListener listener;

	@BeforeAll
	public static void setup() {
		logger = new DefaultLogger(message -> {
		}, LogSource.newRoot(), LogLevel.INFO);
		listener = new TestLogListener();
		logger.addLogListener(listener);
	}

	@Test
	public void test() {
		listener.clear();

		logger.error("Hello World!");

		assertTrue(listener.hasLogged(m -> m.text().equals("Hello World!")));
		assertTrue(listener.hasLogged(m -> m.level() == LogLevel.ERROR));
	}

	@Test
	public void testSubLogger() {
		listener.clear();
		Logger sub = logger.subLogger("sub");

		sub.error("Hello World!");

		assertTrue(listener.hasLogged(m -> m.source().path().contains("sub")));
	}

	@Test
	public void testHierarchicLogger() {
		listener.clear();
		Logger subA = logger.subLogger("subA");
		Logger subB = subA.subLogger("subB");

		subB.error("Hello World!");

		assertTrue(listener.hasLogged(m -> m.source().equals(subB.getSource())));
		assertEquals(List.of("subA"), subA.getSource().path());
		assertEquals(Arrays.asList("subA", "subB"), subB.getSource().path());
	}

	@Test
	public void testLogForwarding() {
		listener.clear();
		Logger sub = logger.subLogger("xyz");
		TestLogListener subListener = new TestLogListener();
		sub.addLogListener(subListener);

		sub.error("Hello World!");
		logger.error("Goodbye!");

		assertTrue(subListener.hasLogged(m -> m.text().equals("Hello World!")));
		assertFalse(subListener.hasLogged(m -> m.text().equals("Goodbye!")));
		assertTrue(listener.hasLogged(m -> m.text().equals("Hello World!")));
		assertTrue(listener.hasLogged(m -> m.text().equals("Goodbye!")));
	}
}
