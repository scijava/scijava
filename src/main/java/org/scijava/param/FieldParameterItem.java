
package org.scijava.param;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.struct.StructItem;
import org.scijava.util.GenericUtils;

/**
 * {@link StructItem} backed by a {@link Field} annotated by {@link Parameter}.
 *
 * @author Curtis Rueden
 * @param <T>
 */
public class FieldParameterItem<T> implements ParameterItem<T> {

	private final Field field;

	/** Type, or a subtype thereof, which houses the field. */
	private final Class<?> type;

	public FieldParameterItem(final Field field, final Class<?> type) {
		this.field = field;
		this.type = type;
	}

	// -- ParameterItem methods --

	public Field getField() {
		return field;
	}

	public Parameter getParameter() {
		return field.getAnnotation(Parameter.class);
	}

	// -- FatStructItem methods --

	// 1) Let matcher work on Struct/StructInfo/StructItem
	//
	// 2) Think about best layer for implicit vs. explicit items

	@Override
	public ItemVisibility getVisibility() {
		return getParameter().visibility();
	}

	@Override
	public boolean isAutoFill() {
		return getParameter().autoFill();
	}

	@Override
	public boolean isRequired() {
		return getParameter().required();
	}

	@Override
	public boolean isPersisted() {
		return getParameter().persist();
	}

	@Override
	public String getPersistKey() {
		return getParameter().persistKey();
	}

	@Override
	public String getInitializer() {
		return getParameter().initializer();
	}

	@Override
	public String getValidater() {
		return getParameter().validater();
	}

	@Override
	public String getCallback() {
		return getParameter().callback();
	}

	@Override
	public String getWidgetStyle() {
		return getParameter().style();
	}

	@Override
	public Object getMinimumValue() {
		return getParameter().min();
	}

	@Override
	public Object getMaximumValue() {
		return getParameter().max();
	}

	@Override
	public T getDefaultValue() {
		// NB: The default value is the initial field value.
		// E.g.:
		//
		//   @Parameter
		//   private int weekdays = 5;
		//
		// To obtain this information, we need to instantiate the object, then
		// extract the value of the associated field.
		//
		// Of course, the command might do evil things like:
		//
		//   @Parameter
		//   private long time = System.currentTimeMillis();
		//
		// In which case the default value will vary by instance. But there is
		// nothing we can really do about that. This is only a best effort.

		try {
			final Object dummy = type.newInstance();
			@SuppressWarnings("unchecked")
			final T value = (T) getField().get(dummy);
			return value;
		}
		catch (final InstantiationException | IllegalAccessException exc) {
			throw new IllegalStateException("Missing no-args constructor", exc);
		}
	}

	@Override
	public Object getStepSize() {
		return getParameter().stepSize();
	}

	@Override
	public List<Object> getChoices() {
		final String[] choices = getParameter().choices();
		if (choices.length == 0) return ParameterItem.super.getChoices();
		return Arrays.asList((Object[]) choices);
	}

	// -- Item methods --

	@Override
	public String getKey() {
		final String key = getParameter().key();
		return key == null || key.isEmpty() ? field.getName() : key;
	}

	@Override
	public Type getType() {
		return GenericUtils.getFieldType(field, type);
	}

	@Override
	public ItemIO getIOType() {
		return getParameter().type();
	}
}
