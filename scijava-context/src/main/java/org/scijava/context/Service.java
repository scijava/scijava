/*
 * #%L
 * An application container: services, discovered and wired.
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

package org.scijava.context;

import org.scijava.priority.Priority;
import org.scijava.spi.Disposable;

/**
 * A long-lived object owned by a {@link Context}, discoverable by the type it
 * provides.
 * <p>
 * Services are found with {@link java.util.ServiceLoader}, so a module
 * contributing one declares it in its {@code module-info.java}:
 * </p>
 *
 * <pre>
 * provides org.scijava.context.Service with com.example.MyLogService;
 * </pre>
 * <p>
 * Note that the declared service type is always {@code Service} itself, never
 * the interface the implementation provides. {@link java.util.ServiceLoader}
 * resolves {@code uses} against the module of its <em>caller</em>, so a
 * container cannot look up an arbitrary third-party interface on that module's
 * behalf: it would have to declare {@code uses com.example.MyLogService},
 * which it cannot know. Declaring one base type that the container does
 * {@code uses} is what makes discovery work under JPMS at all; the container
 * then sorts implementations by the interfaces they implement.
 * </p>
 * <p>
 * Because {@code provides} grants {@link java.util.ServiceLoader} the access
 * it needs, the implementation's package need be neither exported nor opened:
 * a service can stay entirely encapsulated.
 * </p>
 * <h2>Lifecycle</h2>
 * <p>
 * The container constructs a service with its no-argument constructor, then
 * calls {@link #initialize(Context)}. <strong>A constructor must do nothing
 * else.</strong> Where several implementations provide the same interface the
 * container constructs each to compare {@link #priority()}, and discards all
 * but the winner without ever initializing them - so construction must be free
 * of side effects. Everything real belongs in {@code initialize}, where the
 * context is available to look up dependencies.
 * </p>
 * <p>
 * {@link #dispose()} is called when the context is disposed, in reverse order
 * of creation, so that a service is still able to use its dependencies while
 * shutting down.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface Service extends Disposable {

	/**
	 * Prepares this service for use. Dependencies are available here, via
	 * {@link Context#service(Class)}.
	 *
	 * @param context the context that owns this service
	 */
	default void initialize(final Context context) {
		// NB: nothing to do by default.
	}

	/**
	 * Gets this service's priority, used to choose between implementations of
	 * the same interface. See {@link Priority}.
	 */
	default double priority() {
		return Priority.NORMAL;
	}

	/**
	 * Releases anything this service holds. Called when the context is
	 * disposed.
	 */
	@Override
	default void dispose() {
		// NB: nothing to do by default.
	}
}
