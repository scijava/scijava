/*
 * #%L
 * Presenting SciJava Common's modules as SciJava3 commands.
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

package org.scijava.bridge;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.execute.ParameterMember;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.struct.ItemIO;
import org.scijava.struct.MemberInstance;

/**
 * A SciJava Common {@link ModuleItem}, seen as a SciJava3 parameter.
 * <p>
 * The metadata translates almost exactly, which is the point: both describe
 * the same thing, and SciJava3's attributes were named after SciJava Common's
 * on purpose. A legacy command therefore arrives in a SciJava3 dialog with its
 * labels, bounds, choices and widget styles intact, and the widgets have no
 * idea they are rendering something from the old world.
 * </p>
 *
 * @author Curtis Rueden
 */
class LegacyMember<T> implements ParameterMember<T> {

	private final ModuleItem<T> item;

	LegacyMember(final ModuleItem<T> item) {
		this.item = item;
	}

	/** Gets the module item behind this parameter. */
	ModuleItem<T> item() {
		return item;
	}

	@Override
	public String key() {
		return item.getName();
	}

	@Override
	public Type type() {
		return item.getGenericType();
	}

	@Override
	public String description() {
		return item.getDescription();
	}

	@Override
	public boolean isRequired() {
		return item.isRequired();
	}

	@Override
	public ItemIO getIOType() {
		switch (item.getIOType()) {
			case OUTPUT:
				return ItemIO.OUTPUT;
			case BOTH:
				return ItemIO.MUTABLE;
			default:
				return ItemIO.INPUT;
		}
	}

	@Override
	public Map<String, String> attrs() {
		final Map<String, String> attrs = new LinkedHashMap<>();
		put(attrs, LABEL, item.getLabel());
		put(attrs, STYLE, item.getWidgetStyle());
		put(attrs, MIN, string(item.getMinimumValue()));
		put(attrs, MAX, string(item.getMaximumValue()));
		put(attrs, SOFT_MIN, string(item.getSoftMinimum()));
		put(attrs, SOFT_MAX, string(item.getSoftMaximum()));
		put(attrs, STEP_SIZE, string(item.getStepSize()));
		if (item.getChoices() != null && !item.getChoices().isEmpty()) {
			put(attrs, CHOICES, item.getChoices().stream().map(String::valueOf) //
				.collect(Collectors.joining(",")));
		}
		// NB: the callback's *name*, because that is how SciJava3 asks for one:
		// the dialog model reads this attribute and then calls
		// ExecutableInstance.behavior(name). What that name means is the
		// executable's business, and for a legacy command it means a method on
		// the module's delegate object, invoked through ModuleItem.callback --
		// see LegacyCommandInfo. Both worlds name a callback; only the
		// resolution differs.
		put(attrs, CALLBACK, item.getCallback());
		// NB: and the validator, which SciJava Common spells with an 'e'.
		put(attrs, VALIDATOR, item.getValidater());
		return attrs;
	}

	/** Reads this parameter out of the given module. */
	MemberInstance<T> instance(final Module module) {
		return new MemberInstance<>() {

			@Override
			public LegacyMember<T> member() {
				return LegacyMember.this;
			}

			@Override
			public T get() {
				return item.getValue(module);
			}

			@Override
			public void set(final Object value) {
				@SuppressWarnings("unchecked")
				final T typed = (T) value;
				item.setValue(module, typed);
			}

			@Override
			public boolean isReadable() {
				return true;
			}

			@Override
			public boolean isWritable() {
				return true;
			}
		};
	}

	// -- Helper methods --

	private static String string(final Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private static void put(final Map<String, String> attrs, final String key,
		final String value)
	{
		if (value != null && !value.isEmpty()) attrs.put(key, value);
	}
}
