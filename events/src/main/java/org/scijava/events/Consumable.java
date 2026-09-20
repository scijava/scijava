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
 * An event that a handler can mark as handled, stopping its delivery.
 * <p>
 * This is opt-in: an event type implements it only if consumption makes sense
 * for it, and every other event is delivered to all subscribers regardless.
 * The model is {@code java.awt.event.InputEvent.consume()} - named in prose,
 * not linked, so that this component need not require {@code java.desktop} -
 * so that
 * toolkit-style input events - where one handler claims a keystroke and the
 * rest must not see it - can be expressed without every event paying for the
 * concept.
 * </p>
 * <p>
 * Delivery order is therefore meaningful for consumable events: subscribers
 * run in priority order, so a higher-priority handler gets first refusal.
 * </p>
 *
 * @author Curtis Rueden
 * @see EventBus#publish(Object)
 */
public interface Consumable {

	/** Marks this event handled, so that remaining subscribers do not see it. */
	void consume();

	/** Gets whether this event has been consumed. */
	boolean isConsumed();
}
