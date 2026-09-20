/*
 * #%L
 * A typed event bus for loosely coupled notification.
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

package org.scijava.events;

/**
 * A handle on one subscription to an {@link EventBus}.
 * <p>
 * Subscribers are held by strong reference, so a subscription lasts until it
 * is closed - there is no silent disappearance when the garbage collector
 * decides a handler is unreachable. Closing is idempotent, and an
 * {@link AutoCloseable} so that a scoped subscription can be a
 * try-with-resources.
 * </p>
 * <p>
 * Most code never closes one itself: a container that registers handlers on
 * behalf of an object keeps the subscriptions and closes them when that object
 * is disposed.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface Subscription extends AutoCloseable {

	/** Cancels this subscription. Calling it more than once does nothing. */
	@Override
	void close();

	/** Gets whether this subscription has been closed. */
	boolean isClosed();
}
