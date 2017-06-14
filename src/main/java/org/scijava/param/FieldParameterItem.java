
package org.scijava.param;

import java.lang.reflect.Field;

import org.scijava.ValidityException;
import org.scijava.struct.StructInfo;
import org.scijava.struct.StructItem;
import org.scijava.struct.ValueAccessible;
import org.scijava.util.ClassUtils;
import org.scijava.util.GenericUtils;

/**
 * {@link StructItem} backed by a {@link Field} annotated by {@link Parameter}.
 *
 * @author Curtis Rueden
 * @param <T>
 */
public class FieldParameterItem<T> extends AnnotatedParameterItem<T> implements
	ValueAccessible<T>
{

	private final Field field;
	private final Class<?> structType;
	private final StructInfo<ParameterItem<?>> info;

	public FieldParameterItem(final Field field, final Class<?> structType)
		throws ValidityException
	{
		super(GenericUtils.getFieldType(field, structType), //
			field.getAnnotation(Parameter.class));
		this.field = field;
		this.structType = structType;
		info = isStruct() ? ParameterStructs.infoOf(getRawType()) : null;
	}

	// -- FieldParameterItem methods --

	public Field getField() {
		return field;
	}

	// -- ItemValueAccessible methods --

	@Override
	public T get(final Object o) {
		@SuppressWarnings("unchecked")
		final T value = (T) ClassUtils.getValue(ParameterStructs.field(this), o);
		return value;
	}

	@Override
	public void set(final T value, final Object o) {
		ClassUtils.setValue(ParameterStructs.field(this), o, value);
	}

	// -- ParameterItem methods --

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
			final Object dummy = structType.newInstance();
			@SuppressWarnings("unchecked")
			final T value = (T) getField().get(dummy);
			return value;
		}
		catch (final InstantiationException | IllegalAccessException exc) {
			throw new IllegalStateException("Missing no-args constructor", exc);
		}
	}

	// -- StructItem methods --

	@Override
	public String getKey() {
		final String key = getAnnotation().key();
		return key == null || key.isEmpty() ? field.getName() : key;
	}

	@Override
	public StructInfo<? extends ParameterItem<?>> childInfo() {
		return info;
	}
}
