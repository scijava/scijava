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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Turns a {@link URI} into a {@link Location}.
 * <p>
 * Resolvers are discovered with {@link java.util.ServiceLoader}, so a module
 * contributing one declares it in its {@code module-info.java}:
 * </p>
 *
 * <pre>
 * provides org.scijava.io3.location.LocationResolver with com.example.MyResolver;
 * </pre>
 * <p>
 * or, on the class path, in
 * {@code META-INF/services/org.scijava.io3.location.LocationResolver}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Einsdorf
 * @see Locations#resolve(URI)
 */
public interface LocationResolver {

	/** Gets whether this resolver can handle the given URI. */
	boolean supports(URI uri);

	/**
	 * Resolves the given URI to a location.
	 *
	 * @param uri the URI to resolve
	 * @return the resolved location
	 * @throws URISyntaxException if the URI is malformed for this resolver
	 */
	Location resolve(URI uri) throws URISyntaxException;

	/**
	 * Gets this resolver's priority. Where several resolvers support the same
	 * URI, the one with the highest priority wins.
	 * <p>
	 * The value is a plain {@code double} so that this component stays
	 * standalone; the constants of {@code org.scijava.priority.Priority} are
	 * compatible with it.
	 * </p>
	 */
	default double priority() {
		return 0;
	}
}
