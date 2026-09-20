/*
 * #%L
 * Locations and data handles: a uniform way to address and read bytes.
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

package org.scijava.io3.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Locations}.
 *
 * @author Curtis Rueden
 */
public class LocationsTest {

	/** Resolvers are found via ServiceLoader, with no container involved. */
	@Test
	public void testDiscoveryNeedsNoContainer() throws URISyntaxException {
		final Location location = Locations.get().resolve(URI.create(
			"file:/tmp/example.txt"));
		final FileLocation file = assertInstanceOf(FileLocation.class, location);
		assertEquals(new File("/tmp/example.txt"), file.getFile());
	}

	/** An unsupported scheme yields null rather than throwing. */
	@Test
	public void testUnsupportedScheme() throws URISyntaxException {
		assertNull(Locations.get().resolve(URI.create("nosuchscheme:/whatever")));
	}

	/** A scheme-less URI is interpreted as a local file path. */
	@Test
	public void testSchemelessURI() throws URISyntaxException {
		final Location location = Locations.get().resolve(URI.create(
			"/tmp/example.txt"));
		assertInstanceOf(FileLocation.class, location);
	}

	/**
	 * A string that is not a valid URI is treated as a file path. Windows paths
	 * are the reason this matters: a backslash is not legal in a URI.
	 */
	@Test
	public void testNonURIStringIsAFilePath() throws URISyntaxException {
		final Location location = Locations.get().resolve(
			"C:\\Users\\example\\data.txt");
		final FileLocation file = assertInstanceOf(FileLocation.class, location);
		assertTrue(file.getFile().getPath().contains("data.txt"));
	}

	/** An explicit resolver list makes discovery unnecessary. */
	@Test
	public void testExplicitResolvers() throws URISyntaxException {
		final Locations locations = new Locations(List.of(
			new FileLocationResolver()));
		assertNotNull(locations.resolve(URI.create("file:/tmp/x")));
		assertNull(locations.resolve(URI.create("bogus:/tmp/x")));
	}

	/** Where two resolvers support a URI, the higher priority one wins. */
	@Test
	public void testPriorityOrdering() throws URISyntaxException {
		final LocationResolver low = new AbstractLocationResolver("file") {

			@Override
			public Location resolve(final URI uri) {
				return new DummyLocation();
			}
		};
		final LocationResolver high = new AbstractLocationResolver("file") {

			@Override
			public Location resolve(final URI uri) {
				return new FileLocation(uri);
			}

			@Override
			public double priority() {
				return 100;
			}
		};
		// NB: given in the "wrong" order, to prove sorting is what decides.
		final Locations locations = new Locations(List.of(low, high));
		assertInstanceOf(FileLocation.class, locations.resolve(URI.create(
			"file:/tmp/x")));
		assertEquals(high, locations.resolvers().get(0));
	}
}
