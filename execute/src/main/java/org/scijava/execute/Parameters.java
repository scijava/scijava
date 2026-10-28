/*
 * #%L
 * Running things that declare their inputs and outputs.
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

package org.scijava.execute;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * Parameters declared at runtime, with their values in a map.
 * <p>
 * Two quite different needs turn out to want the same thing. A script declares
 * its parameters in a header and its engine holds the values in bindings -
 * there is no Java field anywhere. And a group whose <em>shape</em> depends on
 * a value ("three dimension labels, or seven, according to the count above")
 * cannot be fields either, since a class's fields are fixed when it is
 * compiled.
 * </p>
 * <p>
 * So both are served by parameters built to order over a map. That the two
 * arrived at one primitive is the main thing prototyping the dialog use cases
 * taught us.
 * </p>
 *
 * <pre>
 * Parameters.builder() //
 * 	.add("dim0", String.class) //
 * 	.add("dim1", String.class) //
 * 	.build();
 * </pre>
 *
 * @author Curtis Rueden
 */
public final class Parameters {

	private Parameters() {
		// NB: prevent instantiation of utility class.
	}

	/** Starts building a set of parameters. */
	public static Builder builder() {
		return new Builder();
	}

	/** Collects parameter declarations, then binds them to a map of values. */
	public static class Builder {

		private final List<Member<?>> members = new ArrayList<>();
		private final Map<String, Object> values = new LinkedHashMap<>();

		/** Adds an input parameter. */
		public Builder add(final String key, final Type type) {
			return add(key, type, ItemIO.INPUT, Map.of(), null);
		}

		/** Adds an input parameter with an initial value. */
		public Builder add(final String key, final Type type,
			final Object initialValue)
		{
			return add(key, type, ItemIO.INPUT, Map.of(), initialValue);
		}

		/** Adds a parameter. */
		public Builder add(final String key, final Type type, final ItemIO io,
			final Map<String, String> attrs, final Object initialValue)
		{
			members.add(new MapMember<>(key, type, io, attrs));
			if (initialValue != null) values.put(key, initialValue);
			return this;
		}

		/** Gets the parameters, bound to a fresh map of values. */
		public StructInstance<Map<String, Object>> build() {
			final Struct struct = () -> members;
			return struct.createInstance(values);
		}

		/** Gets the map the built parameters read and write. */
		public Map<String, Object> values() {
			return values;
		}
	}

	/** A parameter whose value lives in a map rather than a field. */
	private static class MapMember<T> implements ParameterMember<T> {

		private final String key;
		private final Type type;
		private final ItemIO io;
		private final Map<String, String> attrs;

		MapMember(final String key, final Type type, final ItemIO io,
			final Map<String, String> attrs)
		{
			this.key = key;
			this.type = type;
			this.io = io;
			this.attrs = attrs;
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public Type type() {
			return type;
		}

		@Override
		public ItemIO getIOType() {
			return io;
		}

		@Override
		public Map<String, String> attrs() {
			return attrs;
		}

		@Override
		public MemberInstance<T> createInstance(final Object o) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> values = (Map<String, Object>) o;
			return new MemberInstance<>() {

				@Override
				public Member<T> member() {
					return MapMember.this;
				}

				@Override
				public boolean isReadable() {
					return true;
				}

				@Override
				public boolean isWritable() {
					return true;
				}

				@Override
				@SuppressWarnings("unchecked")
				public T get() {
					return (T) values.get(key);
				}

				@Override
				public void set(final Object value) {
					values.put(key, value);
				}
			};
		}
	}
}
