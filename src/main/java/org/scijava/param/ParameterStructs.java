
package org.scijava.param;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.ValidityException;
import org.scijava.ValidityProblem;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInfo;
import org.scijava.struct.StructItem;
import org.scijava.util.ClassUtils;
import org.scijava.util.GenericUtils;

/**
 * Utility functions for working with {@link org.scijava.param} classes.
 * 
 * @author Curtis Rueden
 */
public final class ParameterStructs {

	public static StructInfo<ParameterItem<?>> infoOf(final Class<?> type)
		throws ValidityException
	{
		final List<ParameterItem<?>> items = parse(type);
		return new StructInfo<ParameterItem<?>>() {

			@Override
			public List<ParameterItem<?>> items() {
				return items;
			}

			@Override
			public <C> Struct<C> structOf(final C o) {
				return new Struct<>(this, o, new ParameterAccessor<>(),
					new ParameterMutator<>());
			}
		};
	}

	public static List<ParameterItem<?>> parse(final Class<?> type)
		throws ValidityException
	{
		if (type == null) return null;

		final ArrayList<ParameterItem<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		// NB: Reject abstract classes.
		if (Modifier.isAbstract(type.getModifiers())) {
			problems.add(new ValidityProblem("Delegate class is abstract"));
		}

		final List<Field> fields = ClassUtils.getAnnotatedFields(type,
			Parameter.class);

		final Set<String> inputs = new HashSet<>();
		final Set<String> outputs = new HashSet<>();

		for (final Field f : fields) {
			f.setAccessible(true); // expose private fields

			final Parameter param = f.getAnnotation(Parameter.class);

			boolean valid = true;

			final boolean isFinal = Modifier.isFinal(f.getModifiers());
			final boolean isMessage = param.visibility() == ItemVisibility.MESSAGE;
			if (isFinal && !isMessage) {
				// NB: Final parameters are bad because they cannot be modified.
				final String error = "Invalid final parameter: " + f;
				problems.add(new ValidityProblem(error));
				valid = false;
			}

			final String name = f.getName();
			if (inputs.contains(name) || outputs.contains(name)) {
				// NB: Shadowed parameters are bad because they are ambiguous.
				final String error = "Invalid duplicate parameter: " + f;
				problems.add(new ValidityProblem(error));
				valid = false;
			}

			if (param.type() == ItemIO.BOTH && isImmutable(f.getType())) {
				// NB: The BOTH type signifies that the parameter will be changed
				// in-place somehow. But immutable parameters cannot be changed in
				// such a manner, so it makes no sense to label them as BOTH.
				final String error = "Immutable BOTH parameter: " + f;
				problems.add(new ValidityProblem(error));
				valid = false;
			}

			if (!valid) {
				// NB: Skip invalid parameters.
				continue;
			}


			// add item to the relevant lists (inputs or outputs)
			try {
				f.getType();
				final ParameterItem<?> item = createItem(type, f, param.struct());
				if (item.isInput()) inputs.add(name);
				if (item.isOutput()) outputs.add(name);
				items.add(item);
			}
			catch (final ValidityException exc) {
				problems.addAll(exc.problems());
			}
		}

		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}

	public static <T> Field field(final StructItem<T> item) {
		if (item instanceof FieldParameterItem) {
			final FieldParameterItem<T> fpItem = (FieldParameterItem<T>) item;
			return fpItem.getField();
		}
		if (item instanceof StructParameterItem) {
			final StructParameterItem<T> spItem = (StructParameterItem<T>) item;
			return field(spItem.unwrap());
		}
		return null;
	}

	// -- Helper methods --

	// TODO: Consider generalizing this into the org.scijava.struct package
	// by passing a factory / item supplier. Only when we need it.

	private static <T> ParameterItem<T> createItem(final Class<T> type,
		final Field f, final boolean structured) throws ValidityException
	{
		final FieldParameterItem<T> item = new FieldParameterItem<>(f, type);
		if (!structured) return item;

		// TODO: Use new SciJava Types class after it is ready.
		final Class<?> fieldType = //
			GenericUtils.getClass(GenericUtils.getFieldType(f, type));
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final StructInfo<StructItem<?>> info = (StructInfo) infoOf(fieldType);
		return new StructParameterItem<>(item, info);
	}

	private static boolean isImmutable(final Class<?> type) {
		// NB: All eight primitive types, as well as the boxed primitive
		// wrapper classes, as well as strings, are immutable objects.
		return ClassUtils.isNumber(type) || ClassUtils.isText(type) || //
			ClassUtils.isBoolean(type);
	}
}
