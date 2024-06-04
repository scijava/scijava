/*-
 * #%L
 * A library for building and introspecting structs.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

/**
 * {@link Member} backed by a {@link Field} annotated by {@link Parameter}.
 *
 * @author Curtis Rueden
 * @param <T>
 */
public abstract class AnnotatedParameterMember<T> implements ParameterMember<T> {

	/** Type, or a subtype thereof, which houses the field. */
	private final Type itemType;

	/** Annotation describing the item. */
	private final Parameter annotation;

	public AnnotatedParameterMember(final Type itemType,
		final Parameter annotation)
	{
		this.itemType = itemType;
		this.annotation = annotation;
	}

	// -- AnnotatedParameterMember methods --

	public Parameter getAnnotation() {
		return annotation;
	}

	// -- ParameterMember methods --

	@Override
	public ItemVisibility getVisibility() {
		return getAnnotation().visibility();
	}

	@Override
	public boolean isAutoFill() {
		return getAnnotation().autoFill();
	}

	@Override
	public boolean isRequired() {
		return getAnnotation().required();
	}

	@Override
	public boolean isPersisted() {
		return getAnnotation().persist();
	}

	@Override
	public String getPersistKey() {
		return getAnnotation().persistKey();
	}

	@Override
	public String getInitializer() {
		return getAnnotation().initializer();
	}

	@Override
	public String getValidater() {
		return getAnnotation().validater();
	}

	@Override
	public String getCallback() {
		return getAnnotation().callback();
	}

	@Override
	public String getWidgetStyle() {
		return getAnnotation().style();
	}

	@Override
	public Object getMinimumValue() {
		return getAnnotation().min();
	}

	@Override
	public Object getMaximumValue() {
		return getAnnotation().max();
	}

	@Override
	public Object getStepSize() {
		return getAnnotation().stepSize();
	}

	@Override
	public List<Object> getChoices() {
		final String[] choices = getAnnotation().choices();
		if (choices.length == 0) return ParameterMember.super.getChoices();
		return Arrays.asList((Object[]) choices);
	}

	// -- Member methods --

	@Override
	public String key() {
		return getAnnotation().key();
	}

	@Override
	public Type type() {
		return itemType;
	}

	@Override
	public ItemIO getIOType() {
		return getAnnotation().type();
	}
	
	@Override
	public boolean isStruct() {
		return getAnnotation().struct();
	}
}
