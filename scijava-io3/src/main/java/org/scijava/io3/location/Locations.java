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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Resolves {@link URI}s to {@link Location}s, using the
 * {@link LocationResolver}s available to the runtime.
 * <p>
 * Resolvers come from {@link ServiceLoader}, so this needs no container and no
 * configuration: a module that provides a resolver is enough. For an embedded
 * or tested setting where implicit discovery is unwanted, construct an
 * instance with an explicit list of resolvers instead.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Locations {

	private static class DefaultHolder {

		static final Locations INSTANCE = new Locations(discoverResolvers());
	}

	private final List<LocationResolver> resolvers;

	/** Creates an instance that uses exactly the given resolvers. */
	public Locations(final List<LocationResolver> resolvers) {
		this.resolvers = new ArrayList<>(resolvers);
		// NB: highest priority first, so the first supporting resolver wins.
		this.resolvers.sort(Comparator.comparingDouble(LocationResolver::priority)
			.reversed());
	}

	/** Gets the shared instance, backed by {@link ServiceLoader} discovery. */
	public static Locations get() {
		return DefaultHolder.INSTANCE;
	}

	/**
	 * Resolves the given URI string to a location.
	 * <p>
	 * A string that is not a valid URI is treated as a file path. In general
	 * file names are not valid URIs - on Windows especially, backslashes are
	 * not - so this fallback is the common case rather than an edge case.
	 * </p>
	 *
	 * @param uriString the URI or file path to resolve
	 * @return the resolved location
	 * @throws URISyntaxException if the string resolves to no location
	 */
	public Location resolve(final String uriString) throws URISyntaxException {
		try {
			final Location location = resolve(new URI(uriString));
			if (location != null) return location;
		}
		catch (final URISyntaxException exc) {
			// NB: fall through to the file interpretation below.
		}
		return resolve(new File(uriString).toURI());
	}

	/**
	 * Resolves the given URI to a location.
	 *
	 * @param uri the URI to resolve
	 * @return the resolved location, or null if no resolver supports the URI
	 * @throws URISyntaxException if the URI is malformed for its resolver
	 */
	public Location resolve(URI uri) throws URISyntaxException {
		if (uri.getScheme() == null) {
			// NB: a scheme-less URI is a local file path.
			uri = new File(uri.getPath()).toURI();
		}
		final URI resolved = uri;
		final Optional<LocationResolver> resolver = resolvers.stream() //
			.filter(r -> r.supports(resolved)) //
			.findFirst();
		return resolver.isPresent() ? resolver.get().resolve(uri) : null;
	}

	/** Gets the resolvers in use, highest priority first. */
	public List<LocationResolver> resolvers() {
		return List.copyOf(resolvers);
	}

	private static List<LocationResolver> discoverResolvers() {
		final List<LocationResolver> list = new ArrayList<>();
		ServiceLoader.load(LocationResolver.class).forEach(list::add);
		return list;
	}
}
