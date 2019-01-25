
package org.scijava.param;

import java.lang.reflect.Field;

import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.ValueAccessible;
import org.scijava.struct.ValueAccessibleMemberInstance;
import org.scijava.util.Types;

/**
 * {@link Member} backed by a {@link Field} annotated by {@link Parameter}.
 *
 * @author Curtis Rueden
 * @param <T>
 */
public class FieldParameterMember<T> extends AnnotatedParameterMember<T>
	implements ValueAccessible<T>
{

	private final Field field;
	private final Class<?> structType;
	private final Struct struct;

	public FieldParameterMember(final Field field, final Class<?> structType)
		throws ValidityException
	{
		super(Types.fieldType(field, structType), //
			field.getAnnotation(Parameter.class));
		this.field = field;
		this.structType = structType;
		struct = isStruct() ? ParameterStructs.structOf(getRawType()) : null;
	}

	// -- FieldParameterItem methods --

	public Field getField() {
		return field;
	}

	// -- ValueAccessible methods --

	@Override
	public T get(final Object o) {
		try {
			@SuppressWarnings("unchecked")
			final T value = (T) field(this).get(o);
			return value;
		}
		catch (final IllegalAccessException exc) {
			return null; // FIXME
		}
	}

	@Override
	public void set(final T value, final Object o) {
		try {
			field(this).set(o, value);
		}
		catch (final IllegalAccessException exc) {
			// FIXME
		}
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

	// -- Member methods --
	
	public static <T> Field field(final Member<T> item) {
		if (item instanceof FieldParameterMember) {
			final FieldParameterMember<T> fpItem = (FieldParameterMember<T>) item;
			return fpItem.getField();
		}
		return null;
	}

	@Override
	public String getKey() {
		final String key = getAnnotation().key();
		return key == null || key.isEmpty() ? field.getName() : key;
	}

	@Override
	public Struct childStruct() {
		return struct;
	}

	@Override
	public MemberInstance<T> createInstance(final Object o) {
		return new ValueAccessibleMemberInstance<>(this, o);
	}
}
