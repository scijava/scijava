/*
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link MutableParameterMember} implementation.
 * 
 * @author Curtis Rueden
 */
public class DefaultMutableParameterMember<T> implements
	MutableParameterMember<T>
{

	private final String key;
	private final Type type;
	private final ItemIO ioType;

	private ItemVisibility visibility;
	private boolean required;
	private boolean persisted;
	private String persistKey;
	private String initializer;
	private String validater;
	private String callback;
	private String widgetStyle;
	private Object defaultValue;
	private Object minimumValue;
	private Object maximumValue;
	private Object softMinimum;
	private Object softMaximum;
	private Object stepSize;
	private final List<Object> choices = new ArrayList<>();
	private String label;
	private String description;

	/** Table of extra key/value pairs. */
	private final Map<String, String> values = new HashMap<>();

	public DefaultMutableParameterMember(final String key, final Class<T> type, final ItemIO ioType) {
		this(key, (Type) type, ioType);
	}
	public DefaultMutableParameterMember(final String key, final Type type, final ItemIO ioType) {
		this.key = key;
		this.type = type;
		this.ioType = ioType;
		visibility = MutableParameterMember.super.getVisibility();
		required = MutableParameterMember.super.isRequired();
		persisted = MutableParameterMember.super.isPersisted();
		persistKey = MutableParameterMember.super.getPersistKey();
		initializer = MutableParameterMember.super.getInitializer();
		callback = MutableParameterMember.super.getCallback();
		widgetStyle = MutableParameterMember.super.getWidgetStyle();
		minimumValue = MutableParameterMember.super.getMinimumValue();
		maximumValue = MutableParameterMember.super.getMaximumValue();
		stepSize = MutableParameterMember.super.getStepSize();
		final List<Object> superChoices = MutableParameterMember.super.getChoices();
		if (superChoices != null) choices.addAll(superChoices);
		label = MutableParameterMember.super.getLabel();
		description = MutableParameterMember.super.description();
	}

	/** Creates a new parameter with the same values as the given one. */
	public DefaultMutableParameterMember(final ParameterMember<T> member) {
		key = member.key();
		type = member.type();
		ioType = member.getIOType();
		visibility = member.getVisibility();
		required = member.isRequired();
		persisted = member.isPersisted();
		persistKey = member.getPersistKey();
		initializer = member.getInitializer();
		callback = member.getCallback();
		widgetStyle = member.getWidgetStyle();
		minimumValue = member.getMinimumValue();
		maximumValue = member.getMaximumValue();
		softMinimum = member.getSoftMinimum();
		softMaximum = member.getSoftMaximum();
		stepSize = member.getStepSize();
		final List<Object> memberChoices = member.getChoices();
		if (memberChoices != null) choices.addAll(memberChoices);
		label = member.getLabel();
		description = member.description();
	}

	// -- MutableParameterMember methods --

	@Override
	public void setVisibility(final ItemVisibility visibility) {
		this.visibility = visibility;
	}

	@Override
	public void setRequired(final boolean required) {
		this.required = required;
	}

	@Override
	public void setPersisted(final boolean persisted) {
		this.persisted = persisted;
	}

	@Override
	public void setPersistKey(final String persistKey) {
		this.persistKey = persistKey;
	}

	@Override
	public void setInitializer(final String initializer) {
		this.initializer = initializer;
	}

	@Override
	public void setValidater(final String validater) {
		this.validater = validater;
	}

	@Override
	public void setCallback(final String callback) {
		this.callback = callback;
	}

	@Override
	public void setWidgetStyle(final String widgetStyle) {
		this.widgetStyle = widgetStyle;
	}

	@Override
	public void setDefaultValue(final Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public void setMinimumValue(final Object minimumValue) {
		this.minimumValue = minimumValue;
	}

	@Override
	public void setMaximumValue(final Object maximumValue) {
		this.maximumValue = maximumValue;
	}

	@Override
	public void setSoftMinimum(final Object softMinimum) {
		this.softMinimum = softMinimum;
	}

	@Override
	public void setSoftMaximum(final Object softMaximum) {
		this.softMaximum = softMaximum;
	}

	@Override
	public void setStepSize(final Object stepSize) {
		this.stepSize = stepSize;
	}

	@Override
	public void setChoices(final List<Object> choices) {
		this.choices.clear();
		this.choices.addAll(choices);
	}

	@Override
	public void set(final String key, final String value) {
		values.put(key, value);
	}

	// -- MutableBasicDetails methods --

	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	// -- ParameterMember methods --

	@Override
	public ItemVisibility getVisibility() {
		return visibility;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isPersisted() {
		return persisted;
	}

	@Override
	public String getPersistKey() {
		return persistKey;
	}

	@Override
	public String getInitializer() {
		return initializer;
	}

	@Override
	public String getValidater() {
		return validater;
	}

	@Override
	public String getCallback() {
		return callback;
	}

	@Override
	public String getWidgetStyle() {
		return widgetStyle;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public Object getMinimumValue() {
		return minimumValue;
	}

	@Override
	public Object getMaximumValue() {
		return maximumValue;
	}

	@Override
	public Object getSoftMinimum() {
		return softMinimum;
	}

	@Override
	public Object getSoftMaximum() {
		return softMaximum;
	}

	@Override
	public Object getStepSize() {
		return stepSize;
	}

	@Override
	public List<Object> getChoices() {
		return Collections.unmodifiableList(choices);
	}

	// -- BasicDetails methods --

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String description() {
		return description;
	}

	// -- Member methods --

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
		return ioType;
	}
}
