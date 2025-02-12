/*-
 * #%L
 * A library for building and introspecting structs.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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
package org.scijava.struct;

import java.util.Arrays;
import java.util.List;

/**
 * A {@link Member} with extra commonly useful metadata.
 * <p>
 * This metadata is aligned with&mdash;but not explicitly bound to&mdash;the
 * fields of the {@code @Parameter} annotation type. As such, other code such as
 * the {@code org.scijava.script} package can use this interface without being
 * bound to the Java-specific {@code @Parameter} annotation type.
 * </p>
 * 
 * @author Curtis Rueden
 * @see Parameter
 */
public interface ParameterMember<T> extends Member<T> {

	/** Gets the visibility of the parameter. */
	// TODO: fork ItemVisibility and rename to MemberVisibility
	default ItemVisibility getVisibility() {
		return ItemVisibility.NORMAL;
	}

	/** Gets whether the parameter value is allowed to be auto-filled. */
	default boolean isAutoFill() {
		return true;
	}

	/** Gets whether the parameter value must be specified (i.e., no default). */
	default boolean isRequired() {
		return true;
	}

	/** Gets whether to remember the most recent value of the parameter. */
	default boolean isPersisted() {
		return getPersistKey() != null;
	}

	/** Gets the key to use for saving the value persistently. */
	default String getPersistKey() {
		return null;
	}

	/** Gets the function that is called to initialize the parameter's value. */
	default String getInitializer() {
		return null;
	}

	/** Gets the function that is called to validate the parameter's value. */
	default String getValidater() {
		return null;
	}

	/**
	 * Gets the function that is called whenever this parameter changes.
	 * <p>
	 * This mechanism enables interdependent parameters of various types. For
	 * example, two int parameters "width" and "height" could update each other
	 * when another boolean "Preserve aspect ratio" flag is set.
	 * </p>
	 */
	default String getCallback() {
		return null;
	}

	/**
	 * Gets the preferred widget style to use when rendering the parameter in a
	 * user interface.
	 */
	default String getWidgetStyle() {
		return null;
	}

	/** Gets the default value. */
	default Object getDefaultValue() {
		return null;
	}

	/** Gets the minimum allowed value (if applicable). */
	default Object getMinimumValue() {
		return null;
	}

	/** Gets the maximum allowed value (if applicable). */
	default Object getMaximumValue() {
		return null;
	}

	/**
	 * Gets the "soft" minimum value (if applicable).
	 * <p>
	 * The soft minimum is a hint for use in bounded scenarios, such as rendering
	 * in a user interface with a slider or scroll bar widget; the parameter value
	 * will not actually be clamped to the soft bounds, but they may be used in
	 * certain circumstances where appropriate.
	 * </p>
	 */
	default Object getSoftMinimum() {
		// NB: Return hard minimum by default.
		return getMinimumValue();
	}

	/**
	 * Gets the "soft" maximum value (if applicable).
	 * <p>
	 * The soft maximum is a hint for use in bounded scenarios, such as rendering
	 * in a user interface with a slider or scroll bar widget; the parameter value
	 * will not actually be clamped to the soft bounds, but they may be used in
	 * certain circumstances where appropriate.
	 * </p>
	 */
	default Object getSoftMaximum() {
		// NB: Return hard maximum by default.
		return getMaximumValue();
	}

	/**
	 * Gets the preferred step size to use when rendering the parameter in a user
	 * interface (if applicable).
	 */
	default Object getStepSize() {
		return null;
	}

	/** Gets the list of possible values. */
	default List<Object> getChoices() {
		final Class<T> rawType = rawType();
		final T[] choices = rawType.getEnumConstants();
		return choices == null ? null : Arrays.asList(choices);
	}

	// -- BasicDetails methods --

	// TODO: Reconcile with BasicDetails.
	// We need BasicDetails to be read-only, and then have
	// MutableBasicDetails(?) which adds the setters.

	/** Gets a human-readable label. */
	default String getLabel() {
		return key();
	}

	/** Gets a string describing the object. */
	default String description() {
		return null;
	}

	/** Returns true iff the given key is defined. */
	default boolean has(@SuppressWarnings("unused") String key) {
		return false;
	}

	/** Gets the value of the given key, or null if undefined. */
	default String get(@SuppressWarnings("unused") String key) {
		return null;
	}
}
