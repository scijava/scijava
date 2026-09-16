/*-
 * #%L
 * Integration tests for the scijava-discovery library.
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

package org.scijava.discovery.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;
import org.scijava.ops.spi.Op;

/**
 * Tests that {@link Discovery} reports what is available without constructing
 * it - the property that lets metadata-only consumers, such as menu building,
 * avoid paying for objects they will never use.
 *
 * @author Curtis Rueden
 */
public class DiscoveryLazinessTest {

	/**
	 * A {@link Discoverer} backed by {@link ServiceLoader} must report the
	 * implementation class name without constructing the implementation.
	 */
	@Test
	public void testServiceLoaderDiscoveryIsLazy() {
		final Discoverer d = Discoverer.usingProviders( //
			c -> ServiceLoader.load(c).stream());

		final List<Discovery<Op>> discoveries = d.discover(Op.class);
		Assertions.assertEquals(1, discoveries.size());

		final Discovery<Op> discovery = discoveries.get(0);
		Assertions.assertEquals(ServiceBasedAdder.class.getName(), //
			discovery.implClassName());
		Assertions.assertFalse(ServiceBasedAdder.constructed,
			"Discovery must not construct the implementation");

		// Only now should the object come into existence.
		Assertions.assertNotNull(discovery.get());
		Assertions.assertTrue(ServiceBasedAdder.constructed);
	}

	/** Discoveries of an unknown type yield nothing rather than failing. */
	@Test
	public void testUnknownTypeYieldsNothing() {
		final Discoverer d = Discoverer.usingProviders( //
			c -> ServiceLoader.load(c).stream());
		Assertions.assertTrue(d.discover(Runnable.class).isEmpty());
	}
}
