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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.scijava.context.Context;
import org.scijava.discovery.Discovery;

/**
 * Tests dependency injection across a real module boundary.
 * <p>
 * Injecting a private field is deep reflection, so this passes only because
 * the implementation package is opened to the container - and the package is
 * still exported to nobody, so the encapsulation the plugin framework is
 * supposed to provide is intact.
 * </p>
 *
 * @author Curtis Rueden
 */
public class InjectionTest {

	@Test
	public void testPrivateFieldsAreInjectedAcrossModules() {
		try (final Context context = Context.create()) {
			final Discovery<Shouter> discovery = context.plugins(Shouter.class) //
				.stream() //
				.filter(d -> "injected".equals(d.attr("name").orElse(null))) //
				.findFirst().orElseThrow();

			final Shouter shouter = discovery.get();
			// The injected service is what makes this work.
			assertEquals("HELLO FROM A HIDDEN IMPLEMENTATION", shouter.shout());
		}
	}

	@Test
	public void testTheContextInjectsItself() {
		try (final Context context = Context.create()) {
			final Shouter shouter = context.plugins(Shouter.class).stream() //
				.filter(d -> "injected".equals(d.attr("name").orElse(null))) //
				.findFirst().orElseThrow().get();

			// NB: reached reflectively, since the class itself is not exported.
			final Object injected = shouter.getClass().getMethod("injectedContext")
				.invoke(shouter);
			assertSame(context, injected);
		}
		catch (final ReflectiveOperationException exc) {
			throw new AssertionError(exc);
		}
	}

	/** Injection must not require exporting anything. */
	@Test
	public void testInjectionDoesNotRequireExporting() {
		final Module module = Shouter.class.getModule();
		assertTrue(module.isNamed(), "not running on the module path");
		assertFalse(module.isExported("org.scijava.context.test.impl"),
			"injection must not require the package to be exported");
	}
}
