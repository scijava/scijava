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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Default {@link EventBus} implementation.
 *
 * @author Curtis Rueden
 */
public class DefaultEventBus implements EventBus {

	private final List<Registration<?>> registrations = new CopyOnWriteArrayList<>();

	/** Resolved subscriber lists, keyed by published event class. */
	private final Map<Class<?>, List<Registration<?>>> cache =
		new ConcurrentHashMap<>();

	/** Ties in priority are broken by subscription order. */
	private final AtomicLong sequence = new AtomicLong();

	@Override
	public <E> Subscription subscribe(final Class<E> eventType,
		final double priority, final Consumer<? super E> handler)
	{
		if (eventType == null) throw new NullPointerException("eventType");
		if (handler == null) throw new NullPointerException("handler");
		final Registration<E> registration = new Registration<>(eventType, //
			priority, sequence.getAndIncrement(), handler);
		registrations.add(registration);
		cache.clear();
		return registration;
	}

	@Override
	public void publish(final Object event) {
		if (event == null) throw new NullPointerException("event");
		final Consumable consumable = event instanceof Consumable //
			? (Consumable) event : null;

		List<Throwable> failures = null;
		for (final Registration<?> registration : subscribersFor(event
			.getClass()))
		{
			if (registration.isClosed()) continue;
			if (consumable != null && consumable.isConsumed()) break;
			try {
				registration.deliver(event);
			}
			catch (final CancellationException exc) {
				// NB: this thread is being cancelled. Stop at once rather than
				// pressing on and burying it among unrelated failures.
				throw exc;
			}
			catch (final Throwable exc) {
				if (Thread.currentThread().isInterrupted()) throw exc;
				if (failures == null) failures = new ArrayList<>();
				failures.add(exc);
			}
		}
		if (failures != null) throw new EventDeliveryException(event, failures);
	}

	@Override
	public void close() {
		// NB: mark each closed, so that a delivery already under way stops
		// calling them, then drop the references.
		registrations.forEach(Registration::close);
		registrations.clear();
		cache.clear();
	}

	/** Gets the subscribers for an event class, in delivery order. */
	private List<Registration<?>> subscribersFor(final Class<?> eventClass) {
		return cache.computeIfAbsent(eventClass, c -> {
			final List<Registration<?>> matches = new ArrayList<>();
			for (final Registration<?> r : registrations) {
				// NB: subtypes count, so a subscriber to the supertype hears it.
				if (r.eventType.isAssignableFrom(c)) matches.add(r);
			}
			matches.sort(Comparator
				.comparingDouble((Registration<?> r) -> r.priority).reversed() //
				.thenComparingLong(r -> r.sequence));
			return matches;
		});
	}

	/** One subscription. */
	private class Registration<E> implements Subscription {

		private final Class<E> eventType;
		private final double priority;
		private final long sequence;
		private final Consumer<? super E> handler;
		private final AtomicBoolean closed = new AtomicBoolean();

		Registration(final Class<E> eventType, final double priority,
			final long sequence, final Consumer<? super E> handler)
		{
			this.eventType = eventType;
			this.priority = priority;
			this.sequence = sequence;
			this.handler = handler;
		}

		void deliver(final Object event) {
			handler.accept(eventType.cast(event));
		}

		@Override
		public void close() {
			if (closed.compareAndSet(false, true)) {
				registrations.remove(this);
				cache.clear();
			}
		}

		@Override
		public boolean isClosed() {
			return closed.get();
		}

		@Override
		public String toString() {
			return eventType.getName() + " @ " + priority;
		}
	}
}
