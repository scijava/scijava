
package org.scijava.ops.engine.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.common3.Annotations;
import org.scijava.ops.engine.exceptions.impl.FinalOpDependencyFieldException;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.MemberParser;

public class ClassOpDependencyMemberParser implements
	MemberParser<Class<?>, FieldOpDependencyMember<?>>
{

	@Override
	public List<FieldOpDependencyMember<?>> parse(Class<?> source, Type structType)
	{
				if (source == null) return null;

				final ArrayList<FieldOpDependencyMember<?>> items = new ArrayList<>();

				// NB: Reject abstract classes.
				org.scijava.struct.Structs.checkModifiers(source.getName() + ": ", source.getModifiers(), true, Modifier.ABSTRACT);

				// Parse field level @OpDependency annotations.
				parseFieldOpDependencies(items, source);

				return items;
	}

	private static void parseFieldOpDependencies(final List<FieldOpDependencyMember<?>> items,
		Class<?> annotatedClass)
	{
		final List<Field> fields = Annotations.getAnnotatedFields(annotatedClass,
			OpDependency.class);
		for (final Field f : fields) {
			f.setAccessible(true);
			if (Modifier.isFinal(f.getModifiers())) {
				// Final fields are bad because they cannot be modified.
				throw new FinalOpDependencyFieldException(f);
			}
			final FieldOpDependencyMember<?> item = new FieldOpDependencyMember<>(f,
				annotatedClass);
			items.add(item);
		}
	}

}
