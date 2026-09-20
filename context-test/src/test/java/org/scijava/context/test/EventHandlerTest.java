/*-
 * #%L
 * Integration tests for the scijava-context library.
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

package org.scijava.context.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.scijava.context.Context;

/**
 * Tests that a service's {@code @EventHandler} methods are subscribed for it,
 * across a real module boundary and with the handler private.
 *
 * @author Curtis Rueden
 */
public class EventHandlerTest {

	/**
	 * The convenience that matters: a service author annotates a method and
	 * writes no subscription or cleanup code at all.
	 */
	@Test
	public void testServiceHandlerNeedsNoCleanupCode() {
		final Context context = Context.create();
		final RecordingService recorder = context.service(RecordingService.class);

		context.events().publish(new ShoutRecorded("one"));
		context.events().publish(new ShoutRecorded("two"));
		assertEquals(List.of("one", "two"), recorder.recorded());

		// Disposing the context ends the subscription; the author wrote nothing.
		context.dispose();
		context.events().publish(new ShoutRecorded("three"));
		assertEquals(List.of("one", "two"), recorder.recorded());
	}

	/** Subscribing a private method needs no export, only the container's opens. */
	@Test
	public void testHandlersStayEncapsulated() {
		try (final Context context = Context.create()) {
			context.service(RecordingService.class);
			final Module module = RecordingService.class.getModule();
			assertTrue(module.isNamed(), "not running on the module path");
			assertTrue(module.isOpen("org.scijava.context.test.impl", //
				Context.class.getModule()));
			assertTrue(!module.isExported("org.scijava.context.test.impl"));
		}
	}
}
