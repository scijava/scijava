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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.junit.jupiter.api.Test;
import org.scijava.priority.Priority;

/**
 * Tests {@link DefaultEventBus}.
 *
 * @author Curtis Rueden
 */
public class EventBusTest {

	// -- Test events --

	private static class BaseEvent {}

	private static class DerivedEvent extends BaseEvent {}

	private static class Keystroke implements Consumable {

		private boolean consumed;

		@Override
		public void consume() {
			consumed = true;
		}

		@Override
		public boolean isConsumed() {
			return consumed;
		}
	}

	@Test
	public void testPublishAndSubscribe() {
		final EventBus bus = EventBus.create();
		final List<Object> seen = new ArrayList<>();
		bus.subscribe(BaseEvent.class, seen::add);

		final BaseEvent event = new BaseEvent();
		bus.publish(event);
		assertEquals(List.of(event), seen);
	}

	/** A subscriber to a supertype hears subtypes. */
	@Test
	public void testHierarchyDispatch() {
		final EventBus bus = EventBus.create();
		final List<String> seen = new ArrayList<>();
		bus.subscribe(BaseEvent.class, e -> seen.add("base"));
		bus.subscribe(DerivedEvent.class, e -> seen.add("derived"));
		bus.subscribe(Object.class, e -> seen.add("object"));

		bus.publish(new DerivedEvent());
		assertEquals(3, seen.size());

		seen.clear();
		bus.publish(new BaseEvent());
		// NB: the derived subscriber must not hear a supertype event.
		assertEquals(List.of("base", "object"), seen);
	}

	/** Any object can be an event; no marker interface is required. */
	@Test
	public void testEventNeedsNoMarkerType() {
		final EventBus bus = EventBus.create();
		final List<String> seen = new ArrayList<>();
		bus.subscribe(String.class, seen::add);
		bus.publish("hello");
		assertEquals(List.of("hello"), seen);
	}

	@Test
	public void testPriorityOrder() {
		final EventBus bus = EventBus.create();
		final List<String> order = new ArrayList<>();
		bus.subscribe(BaseEvent.class, Priority.LOW, e -> order.add("low"));
		bus.subscribe(BaseEvent.class, Priority.VERY_HIGH, e -> order.add("very high"));
		bus.subscribe(BaseEvent.class, e -> order.add("normal"));
		bus.subscribe(BaseEvent.class, Priority.HIGH, e -> order.add("high"));

		bus.publish(new BaseEvent());
		assertEquals(List.of("very high", "high", "normal", "low"), order);
	}

	/** Ties in priority are broken by subscription order. */
	@Test
	public void testSubscriptionOrderBreaksTies() {
		final EventBus bus = EventBus.create();
		final List<String> order = new ArrayList<>();
		bus.subscribe(BaseEvent.class, e -> order.add("first"));
		bus.subscribe(BaseEvent.class, e -> order.add("second"));
		bus.subscribe(BaseEvent.class, e -> order.add("third"));

		bus.publish(new BaseEvent());
		assertEquals(List.of("first", "second", "third"), order);
	}

	/** A consumed event stops reaching later subscribers. */
	@Test
	public void testConsumptionStopsDelivery() {
		final EventBus bus = EventBus.create();
		final List<String> seen = new ArrayList<>();
		bus.subscribe(Keystroke.class, Priority.HIGH, e -> {
			seen.add("high");
			e.consume();
		});
		bus.subscribe(Keystroke.class, Priority.LOW, e -> seen.add("low"));

		bus.publish(new Keystroke());
		assertEquals(List.of("high"), seen);
	}

	/** An event that nobody consumes reaches everyone. */
	@Test
	public void testUnconsumedEventReachesAll() {
		final EventBus bus = EventBus.create();
		final List<String> seen = new ArrayList<>();
		bus.subscribe(Keystroke.class, Priority.HIGH, e -> seen.add("high"));
		bus.subscribe(Keystroke.class, Priority.LOW, e -> seen.add("low"));

		bus.publish(new Keystroke());
		assertEquals(List.of("high", "low"), seen);
	}

	/**
	 * One broken subscriber must not truncate delivery, and no failure may be
	 * lost.
	 */
	@Test
	public void testFailuresAreAggregated() {
		final EventBus bus = EventBus.create();
		final List<String> seen = new ArrayList<>();
		bus.subscribe(BaseEvent.class, Priority.VERY_HIGH, e -> {
			throw new IllegalStateException("first");
		});
		bus.subscribe(BaseEvent.class, Priority.HIGH, e -> seen.add("survivor"));
		bus.subscribe(BaseEvent.class, Priority.LOW, e -> {
			throw new UnsupportedOperationException("second");
		});

		final EventDeliveryException exc = assertThrows(
			EventDeliveryException.class, () -> bus.publish(new BaseEvent()));

		// Everyone still got the event.
		assertEquals(List.of("survivor"), seen);
		// Neither failure was lost.
		assertEquals("first", exc.getCause().getMessage());
		assertEquals(1, exc.getSuppressed().length);
		assertEquals("second", exc.getSuppressed()[0].getMessage());
	}

	/** Cancellation is not an ordinary failure: it stops delivery at once. */
	@Test
	public void testCancellationStopsDeliveryImmediately() {
		final EventBus bus = EventBus.create();
		final List<String> seen = new ArrayList<>();
		bus.subscribe(BaseEvent.class, Priority.HIGH, e -> {
			throw new CancellationException("cancelled");
		});
		bus.subscribe(BaseEvent.class, Priority.LOW, e -> seen.add("later"));

		assertThrows(CancellationException.class, () -> bus.publish(
			new BaseEvent()));
		assertTrue(seen.isEmpty(), "delivery continued past a cancellation");
	}

	@Test
	public void testUnsubscribe() {
		final EventBus bus = EventBus.create();
		final List<Object> seen = new ArrayList<>();
		final Subscription subscription = bus.subscribe(BaseEvent.class, seen::add);

		bus.publish(new BaseEvent());
		assertEquals(1, seen.size());

		subscription.close();
		assertTrue(subscription.isClosed());
		bus.publish(new BaseEvent());
		assertEquals(1, seen.size(), "a closed subscription still received events");

		// Closing twice is harmless.
		subscription.close();
	}

	/** Closing the bus is what a context does on dispose. */
	@Test
	public void testCloseDropsEverySubscription() {
		final EventBus bus = EventBus.create();
		final List<Object> seen = new ArrayList<>();
		final Subscription a = bus.subscribe(BaseEvent.class, seen::add);
		final Subscription b = bus.subscribe(Object.class, seen::add);

		bus.close();
		assertTrue(a.isClosed());
		assertTrue(b.isClosed());

		bus.publish(new BaseEvent());
		assertTrue(seen.isEmpty(), "a closed bus still delivered events");
	}

	/**
	 * Two buses are wholly independent, which is how two application contexts
	 * keep their events straight. It is also why there is no shared instance.
	 */
	@Test
	public void testBusesAreIsolated() {
		final EventBus first = EventBus.create();
		final EventBus second = EventBus.create();
		final List<Object> heardByFirst = new ArrayList<>();
		final List<Object> heardBySecond = new ArrayList<>();
		first.subscribe(BaseEvent.class, heardByFirst::add);
		second.subscribe(BaseEvent.class, heardBySecond::add);

		first.publish(new BaseEvent());
		assertEquals(1, heardByFirst.size());
		assertTrue(heardBySecond.isEmpty(), "events crossed between buses");

		// Closing one must not disturb the other.
		first.close();
		second.publish(new BaseEvent());
		assertEquals(1, heardBySecond.size());
	}

	/** Subscribing during delivery must not disturb the delivery under way. */
	@Test
	public void testSubscribeDuringDelivery() {
		final EventBus bus = EventBus.create();
		final List<String> seen = new ArrayList<>();
		bus.subscribe(BaseEvent.class, e -> {
			seen.add("first");
			bus.subscribe(BaseEvent.class, e2 -> seen.add("added"));
		});

		bus.publish(new BaseEvent());
		assertEquals(List.of("first"), seen);

		// The late subscriber takes effect from the next event.
		seen.clear();
		bus.publish(new BaseEvent());
		assertEquals(List.of("first", "added"), seen);
	}

	@Test
	public void testNullsAreRejected() {
		final EventBus bus = EventBus.create();
		assertThrows(NullPointerException.class, () -> bus.publish(null));
		assertThrows(NullPointerException.class, () -> bus.subscribe(null,
			e -> {}));
		assertThrows(NullPointerException.class, () -> bus.subscribe(
			BaseEvent.class, null));
	}

	/**
	 * A handler that cancels another subscription mid-delivery must stop that
	 * subscriber from receiving the event in flight.
	 */
	@Test
	public void testUnsubscribeDuringDelivery() {
		final EventBus bus = EventBus.create();
		final List<String> seen = new ArrayList<>();
		final Subscription[] later = new Subscription[1];
		bus.subscribe(BaseEvent.class, Priority.HIGH, e -> {
			seen.add("first");
			later[0].close();
		});
		later[0] = bus.subscribe(BaseEvent.class, Priority.LOW, //
			e -> seen.add("later"));

		bus.publish(new BaseEvent());
		assertEquals(List.of("first"), seen);
	}
}
