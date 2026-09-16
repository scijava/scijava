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

import java.util.function.Consumer;

import org.scijava.priority.Priority;

/**
 * Notifies subscribers of events, without publisher and subscriber knowing
 * about each other.
 * <p>
 * A subscription names the event type it wants, and events reach subscribers
 * of that type <em>and of its supertypes</em>: publishing a
 * {@code DisplayUpdatedEvent} reaches subscribers of {@code DisplayEvent}. An
 * event may be any object; no marker interface is required, so a type from
 * another library can be an event.
 * </p>
 * <p>
 * <strong>There is deliberately no shared instance.</strong> A bus is nothing
 * but shared mutable subscriber state, so an ambient singleton would let two
 * application contexts hear each other's events. Each context owns one bus,
 * and code reaches it through its context; standalone callers construct their
 * own with {@link #create()}.
 * </p>
 * <p>
 * Subscribers are held by strong reference. A subscription ends when its
 * {@link Subscription} is closed, or when the whole bus is {@link #close()}d -
 * which a context does when it is disposed, so that code registering handlers
 * on behalf of a service never has to think about cleanup.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface EventBus extends AutoCloseable {

	/**
	 * Subscribes to events of the given type, at {@link Priority#NORMAL}.
	 *
	 * @param eventType the type of event to receive, subtypes included
	 * @param handler what to do with each event
	 * @return a handle that cancels the subscription
	 */
	default <E> Subscription subscribe(final Class<E> eventType,
		final Consumer<? super E> handler)
	{
		return subscribe(eventType, Priority.NORMAL, handler);
	}

	/**
	 * Subscribes to events of the given type.
	 * <p>
	 * Subscribers run in descending priority order, ties broken by
	 * subscription order. Priority matters most for {@link Consumable} events,
	 * where an earlier subscriber can stop delivery to the rest.
	 * </p>
	 *
	 * @param eventType the type of event to receive, subtypes included
	 * @param priority the subscriber's priority; see {@link Priority}
	 * @param handler what to do with each event
	 * @return a handle that cancels the subscription
	 */
	<E> Subscription subscribe(Class<E> eventType, double priority,
		Consumer<? super E> handler);

	/**
	 * Delivers an event to its subscribers, on the calling thread.
	 * <p>
	 * Delivery stops early only if the event is {@link Consumable} and a
	 * subscriber consumes it. A subscriber that throws does not stop delivery
	 * to the others; the failures are gathered and thrown together afterward as
	 * an {@link EventDeliveryException}.
	 * </p>
	 * <p>
	 * Cancellation is the exception to that: if a subscriber is interrupted, or
	 * throws {@link java.util.concurrent.CancellationException}, delivery stops
	 * at once and the exception propagates. An interrupt means this thread is
	 * being cancelled, and must not be buried among other failures.
	 * </p>
	 *
	 * @param event the event to deliver
	 * @throws EventDeliveryException if any subscriber failed
	 */
	void publish(Object event);

	/**
	 * Cancels every subscription on this bus.
	 * <p>
	 * A context calls this when disposing, which is what lets services
	 * subscribe without arranging their own cleanup.
	 * </p>
	 */
	@Override
	void close();

	/** Creates a bus. */
	static EventBus create() {
		return new DefaultEventBus();
	}
}
