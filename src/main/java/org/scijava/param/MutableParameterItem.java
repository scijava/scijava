package org.scijava.param;

import java.util.List;

import org.scijava.ItemVisibility;

/**
 * A {@link ParameterItem} whose metadata can be changed.
 * 
 * @author Curtis Rueden
 */
public interface MutableParameterItem<T> extends ParameterItem<T> {

	void setVisibility(ItemVisibility visibility);

	void setRequired(boolean required);

	void setPersisted(boolean persisted);

	void setPersistKey(String persistKey);

	void setInitializer(String initializer);

	void setValidater(String validater);

	void setCallback(String callback);

	void setWidgetStyle(String widgetStyle);

	void setDefaultValue(Object defaultValue);

	void setMinimumValue(Object minimumValue);

	void setMaximumValue(Object maximumValue);

	void setSoftMinimum(Object softMinimum);

	void setSoftMaximum(Object softMaximum);

	void setStepSize(Object stepSize);

	void setChoices(List<Object> choices);

	// -- MutableBasicDetails methods --

	// TODO: Factor out to MutableBasicDetails.

	void setLabel(String label);

	void setDescription(String description);

	default void set(final String key) {
		set(key, null);
	}

	void set(String key, String value);
}
