/*
 * #%L
 * The model behind a parameter dialog: groups, dependencies, validation.
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

package org.scijava.harvest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.scijava.struct.MemberInstance;
import org.scijava.struct.StructInstance;

/**
 * A {@link StructInstance} that reports when its values change.
 * <p>
 * A dialog needs this for a reason that only becomes obvious once callbacks
 * exist: a callback may set <em>any</em> parameter, not merely the one that
 * triggered it, so after running one the only way to know what moved is to
 * re-read everything and compare. {@link #refresh()} does that and reports
 * every difference. At dialog sizes the cost is irrelevant, and there is no
 * cleverer answer - SciJava Common dirty-checks for the same reason.
 * </p>
 * <p>
 * This is a decorator rather than a change to {@link MemberInstance}: values
 * are only observable where somebody is watching, which is to say in a user
 * interface.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ObservableStruct {

	/** What happened to one parameter. */
	public static class Change {

		private final MemberInstance<?> member;
		private final Object oldValue;
		private final Object newValue;

		public Change(final MemberInstance<?> member, final Object oldValue,
			final Object newValue)
		{
			this.member = member;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public MemberInstance<?> member() {
			return member;
		}

		public String key() {
			return member.member().key();
		}

		public Object oldValue() {
			return oldValue;
		}

		public Object newValue() {
			return newValue;
		}

		@Override
		public String toString() {
			return key() + ": " + oldValue + " -> " + newValue;
		}
	}

	private final StructInstance<?> instance;
	private final Map<String, Object> lastSeen = new LinkedHashMap<>();
	private final List<Consumer<Change>> listeners = new ArrayList<>();

	public ObservableStruct(final StructInstance<?> instance) {
		this.instance = instance;
		snapshot();
	}

	/** Gets the underlying parameters. */
	public StructInstance<?> instance() {
		return instance;
	}

	/** Registers a listener, notified once per changed parameter. */
	public void onChange(final Consumer<Change> listener) {
		listeners.add(listener);
	}

	/**
	 * Sets a parameter and reports what changed, including anything a callback
	 * altered along the way.
	 *
	 * @param key the parameter to set
	 * @param value its new value
	 * @return every parameter that differs from before
	 */
	public List<Change> set(final String key, final Object value) {
		final MemberInstance<?> member = instance.member(key);
		if (member == null) {
			throw new IllegalArgumentException("No such parameter: " + key);
		}
		member.set(value);
		return refresh();
	}

	/**
	 * Re-reads every parameter, reporting those that differ from the last time
	 * anyone looked.
	 * <p>
	 * Call this after anything that might have changed values behind the
	 * dialog's back - a callback, an initializer, the command mutating itself.
	 * </p>
	 *
	 * @return every parameter that changed
	 */
	public List<Change> refresh() {
		final List<Change> changes = new ArrayList<>();
		for (final MemberInstance<?> member : instance.members()) {
			final String key = member.member().key();
			final Object now = value(member);
			final Object before = lastSeen.get(key);
			if (!equal(before, now)) {
				lastSeen.put(key, now);
				changes.add(new Change(member, before, now));
			}
		}
		// NB: notify only after the whole scan, so that a listener sees a
		// settled state rather than one being read out from under it.
		for (final Change change : changes) {
			for (final Consumer<Change> listener : listeners)
				listener.accept(change);
		}
		return changes;
	}

	/** Gets a parameter's current value. */
	public Object get(final String key) {
		final MemberInstance<?> member = instance.member(key);
		return member == null ? null : value(member);
	}

	// -- Helper methods --

	private void snapshot() {
		for (final MemberInstance<?> member : instance.members())
			lastSeen.put(member.member().key(), value(member));
	}

	private static Object value(final MemberInstance<?> member) {
		return member.isReadable() ? member.get() : null;
	}

	private static boolean equal(final Object a, final Object b) {
		return a == null ? b == null : a.equals(b);
	}
}
