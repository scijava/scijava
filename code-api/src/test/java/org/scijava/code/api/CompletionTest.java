/*-
 * #%L
 * Core API for SciJava code intelligence features.
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

package org.scijava.code.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Completion} and {@link CompletionResult}.
 *
 * @author Gabriel Selzer
 */
public class CompletionTest {

	@Test
	public void testLazyDescription() {
		final AtomicInteger calls = new AtomicInteger();
		final Completion c = Completion.builder("zeros").lazyDescription(() -> {
			calls.incrementAndGet();
			return "Return a new array of zeros.";
		}).build();
		// Not computed until requested...
		assertEquals(0, calls.get());
		assertEquals("Return a new array of zeros.", c.description());
		// ...and then only once.
		assertEquals("Return a new array of zeros.", c.description());
		assertEquals(1, calls.get());
		// An eager description wins over a lazy one.
		assertEquals("eager", Completion.builder("x").description("eager")
			.lazyDescription(() -> "lazy").build().description());
		assertNull(Completion.builder("x").lazyDescription(() -> null).build()
			.description());
	}

	@Test
	public void testUpdate() {
		final CompletionResult first = new CompletionResult(Collections
			.singletonList(Completion.of("x")), 3);
		assertNull(first.update());
		final CompletableFuture<CompletionResult> later = new CompletableFuture<>();
		final CompletionResult announced = first.withUpdate(later);
		assertSame(later, announced.update());
		assertEquals(first.completions(), announced.completions());
		assertEquals(3, announced.replaceStart());
	}
}
