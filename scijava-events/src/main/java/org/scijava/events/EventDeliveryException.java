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

import java.util.List;

/**
 * Reports that one or more subscribers failed while handling an event.
 * <p>
 * Delivery does not stop when a subscriber throws: every remaining subscriber
 * still sees the event, and the failures are reported together afterward. One
 * broken subscriber therefore cannot quietly truncate delivery to the others,
 * and no failure is swallowed.
 * </p>
 *
 * @author Curtis Rueden
 */
public class EventDeliveryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final transient Object event;

	public EventDeliveryException(final Object event,
		final List<Throwable> failures)
	{
		super(failures.size() + " subscriber(s) failed handling " + //
			(event == null ? "null" : event.getClass().getName()), failures.get(0));
		this.event = event;
		// NB: the first failure is the cause; the rest ride along, so that none
		// is lost.
		for (int i = 1; i < failures.size(); i++)
			addSuppressed(failures.get(i));
	}

	/** Gets the event whose delivery failed. */
	public Object event() {
		return event;
	}
}
