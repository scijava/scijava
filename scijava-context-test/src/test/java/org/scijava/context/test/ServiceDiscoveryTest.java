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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.scijava.context.Context;

/**
 * Tests that a {@link Context} discovers services declared with
 * {@code provides}, including ones whose package is neither exported nor
 * opened.
 * <p>
 * This lives in its own module because that is the only way to exercise the
 * claim: a module must really declare {@code provides} for
 * {@link java.util.ServiceLoader} to see its implementation, and the
 * implementation package must really be unexported for encapsulation to mean
 * anything.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ServiceDiscoveryTest {

	@Test
	public void testDiscoveryFindsTheService() {
		try (final Context context = Context.create()) {
			final GreeterService greeter = context.service(GreeterService.class);
			assertNotNull(greeter);
			assertEquals("hello from a hidden implementation", greeter.greet());
		}
	}

	/**
	 * The implementation stays encapsulated: callers cannot reach its type,
	 * only the interface it provides. A {@code provides} declaration grants
	 * ServiceLoader the access it needs without exporting or opening anything.
	 */
	@Test
	public void testImplementationStaysEncapsulated() {
		try (final Context context = Context.create()) {
			final GreeterService greeter = context.service(GreeterService.class);
			final Class<?> implClass = greeter.getClass();
			final Module module = implClass.getModule();

			// NB: this test is only meaningful on the module path, so assert
			// that rather than skipping quietly if the build stops being modular.
			assertTrue(module.isNamed(), "not running on the module path, so " +
				"this test proves nothing about encapsulation");

			final String implPackage = implClass.getPackageName();
			assertEquals("org.scijava.context.test.impl", implPackage);
			assertFalse(module.isExported(implPackage),
				"the implementation package must not be exported");
			assertFalse(module.isOpen(implPackage),
				"the implementation package must not be opened");
			assertTrue(module.isExported("org.scijava.context.test"),
				"the service interface must be exported");
		}
	}

	/** Each context gets its own instance of a discovered service. */
	@Test
	public void testEachContextGetsItsOwnInstance() {
		try (final Context first = Context.create();
				final Context second = Context.create())
		{
			assertFalse(first.service(GreeterService.class) == second.service(
				GreeterService.class), "two contexts shared one service instance");
		}
	}
}
