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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link LogMessage}.
 * 
 * @author Matthias Arzt
 */
public class LogMessageTest {

	/** Tests {@link LogMessage#toString()}. */
	@Test
	public void testToString() {
		String nameOfThisMethod = "testToString";
		// setup
		LogMessage message = new LogMessage(LogSource.newRoot(), LogLevel.DEBUG, 42, new NullPointerException());
		// process
		String s = message.toString();
		// test
		assertTrue(s.contains(LogLevel.prefix(message.level())), "Log message contains level");
		assertTrue(s.contains(message.text()), "Log message contains msg");
		assertTrue(s.contains(message.throwable().toString()), "Log message contains throwable");
		assertTrue(s.contains(nameOfThisMethod), "Log message contains stack trace");
	}

	@Test
	public void testToStringOptionalParameters() {
		// setup
		LogMessage message = new LogMessage(LogSource.newRoot(), LogLevel.WARN, null, null);

		// process
		// Can it still format the message if optional parameters are null?
		String s = message.toString();

		// test
		Assertions.assertTrue(s.contains(LogLevel.prefix(message.level())), "Log message contains level");
	}

	@Test
	public void testAttachments() {
		LogMessage message = new LogMessage(LogSource.newRoot(), LogLevel.ERROR, "Message");
		assertTrue(message.attachments().isEmpty());
		Object object = new Object();
		message.attach(object);
		assertEquals(Collections.singletonList(object), new ArrayList<>(message.attachments()));
	}
}
