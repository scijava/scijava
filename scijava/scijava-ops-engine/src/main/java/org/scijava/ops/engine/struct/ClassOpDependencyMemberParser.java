
package org.scijava.ops.engine.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.common3.validity.ValidityException;
import org.scijava.common3.validity.ValidityProblem;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.MemberParser;
import org.scijava.common3.Annotations;

public class ClassOpDependencyMemberParser implements
	MemberParser<Class<?>, FieldOpDependencyMember<?>>
{

	@Override
	public List<FieldOpDependencyMember<?>> parse(Class<?> source, Type structType)
		throws ValidityException
	{
				if (source == null) return null;

				final ArrayList<FieldOpDependencyMember<?>> items = new ArrayList<>();
				final ArrayList<ValidityProblem> problems = new ArrayList<>();

				// NB: Reject abstract classes.
				org.scijava.struct.Structs.checkModifiers(source.getName() + ": ", problems, source.getModifiers(), true, Modifier.ABSTRACT);

				// Parse field level @OpDependency annotations.
				parseFieldOpDependencies(items, problems, source);

				// Fail if there were any problems.
				if (!problems.isEmpty()) throw new ValidityException(problems);

				return items;
	}

	private static void parseFieldOpDependencies(final List<FieldOpDependencyMember<?>> items,
		final List<ValidityProblem> problems, Class<?> annotatedClass)
	{
		final List<Field> fields = Annotations.getAnnotatedFields(annotatedClass,
			OpDependency.class);
		for (final Field f : fields) {
			f.setAccessible(true);
			final boolean isFinal = Modifier.isFinal(f.getModifiers());
			if (isFinal) {
				final String name = f.getName();
				// Final fields are bad because they cannot be modified.
				final String error = "Invalid final Op dependency field: " + name;
				problems.add(new ValidityProblem(error));
				// Skip invalid Op dependencies.
				continue;
			}
			final FieldOpDependencyMember<?> item = new FieldOpDependencyMember<>(f,
				annotatedClass);
			items.add(item);
		}
	}

}
