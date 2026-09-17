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

import java.util.Map;
import java.util.Optional;

import org.scijava.struct.Member;

/**
 * A parameter that carries metadata beyond its name and type: a label, a
 * callback, a validator, which group it belongs to, how it should be
 * displayed.
 * <p>
 * The metadata is a string map rather than a set of typed accessors, and
 * deliberately so: it has to cross language boundaries. A Java command
 * declares it in annotations, a script declares it in its header, and neither
 * can see the other's types. Strings are what a script header actually holds,
 * so pretending otherwise would only move the parsing somewhere less obvious.
 * Typed accessors wrap the well-known keys.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface ParameterMember<T> extends Member<T> {

	/** Metadata key: the behavior run when this parameter changes. */
	String CALLBACK = "callback";

	/** Metadata key: the behavior that checks this parameter's value. */
	String VALIDATOR = "validator";

	/** Metadata key: the behavior deciding whether this parameter is shown. */
	String VISIBLE_WHEN = "visibleWhen";

	/** Metadata key: the group this parameter belongs to. */
	String GROUP = "group";

	/** Metadata key: a human-readable label. */
	String LABEL = "label";

	/** Metadata key: the values this parameter may take, comma-separated. */
	String CHOICES = "choices";

	/** Metadata key: the behavior producing the values it may take. */
	String CHOICES_FROM = "choicesFrom";

	/** Metadata key: the smallest value a numeric parameter may take. */
	String MIN = "min";

	/** Metadata key: the largest value a numeric parameter may take. */
	String MAX = "max";

	/** Metadata key: how far one step moves a numeric parameter. */
	String STEP_SIZE = "stepSize";

	/** Metadata key: the smallest value a slider should span. */
	String SOFT_MIN = "softMin";

	/** Metadata key: the largest value a slider should span. */
	String SOFT_MAX = "softMax";

	/** Metadata key: a hint about how to display this parameter. */
	String STYLE = "style";

	/** Gets this parameter's metadata. */
	Map<String, String> attrs();

	/** Gets one metadata value, absent if unset or empty. */
	default Optional<String> attr(final String key) {
		final String value = attrs().get(key);
		return value == null || value.isEmpty() ? Optional.empty() //
			: Optional.of(value);
	}

	/**
	 * Gets the label to display, falling back to the parameter's name split
	 * into words: {@code numDims} becomes "Num dims".
	 * <p>
	 * NB: SciJava Common merely capitalised the first letter, so a camel-case
	 * name arrived in the dialog as "NumDims". Splitting reads better and costs
	 * nothing, since an explicit label overrides it anyway.
	 * </p>
	 */
	default String label() {
		return attr(LABEL).orElseGet(() -> humanize(key()));
	}

	/** Turns a camel-case identifier into a readable label. */
	static String humanize(final String key) {
		if (key == null || key.isEmpty()) return key;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key.length(); i++) {
			final char c = key.charAt(i);
			if (i == 0) sb.append(Character.toUpperCase(c));
			else if (Character.isUpperCase(c) && !Character.isUpperCase(key.charAt(i -
				1)))
			{
				sb.append(' ').append(Character.toLowerCase(c));
			}
			else sb.append(c);
		}
		return sb.toString();
	}
}
