
package org.scijava.param;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.struct.StructItem;

/**
 * {@link StructItem} backed by a {@link Field} annotated by {@link Parameter}.
 *
 * @author Curtis Rueden
 * @param <T>
 */
public abstract class AnnotatedParameterItem<T> implements ParameterItem<T> {

	/** Type, or a subtype thereof, which houses the field. */
	private final Type itemType;

	/** Annotation describing the item. */
	private final Parameter annotation;

	public AnnotatedParameterItem(final Type itemType,
		final Parameter annotation)
	{
		this.itemType = itemType;
		this.annotation = annotation;
	}

	// -- AnnotatedParameterItem methods --

	public Parameter getAnnotation() {
		return annotation;
	}

	// -- ParameterItem methods --

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
		if (choices.length == 0) return ParameterItem.super.getChoices();
		return Arrays.asList((Object[]) choices);
	}

	// -- StructItem methods --

	@Override
	public String getKey() {
		return getAnnotation().key();
	}

	@Override
	public Type getType() {
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
