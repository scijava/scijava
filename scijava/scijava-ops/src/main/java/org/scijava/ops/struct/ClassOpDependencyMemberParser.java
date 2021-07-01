
package org.scijava.ops.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ValidityProblem;
import org.scijava.ops.FieldOpDependencyMember;
import org.scijava.ops.OpDependency;
import org.scijava.ops.ValidityException;
import org.scijava.util.ClassUtils;

public class ClassOpDependencyMemberParser implements
	MemberParser<Class<?>, FieldOpDependencyMember<?>>
{

	@Override
	public List<FieldOpDependencyMember<?>> parse(Class<?> source)
		throws ValidityException
	{
				if (source == null) return null;

				final ArrayList<FieldOpDependencyMember<?>> items = new ArrayList<>();
				final ArrayList<ValidityProblem> problems = new ArrayList<>();

				// NB: Reject abstract classes.
				Structs.checkModifiers(source.getName() + ": ", problems, source.getModifiers(), true, Modifier.ABSTRACT);

				// Parse field level @OpDependency annotations.
				parseFieldOpDependencies(items, problems, source);

				// Fail if there were any problems.
				if (!problems.isEmpty()) throw new ValidityException(problems);

				return items;
	}

	private static void parseFieldOpDependencies(final List<FieldOpDependencyMember<?>> items,
		final List<ValidityProblem> problems, Class<?> annotatedClass)
	{
		final List<Field> fields = ClassUtils.getAnnotatedFields(annotatedClass,
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
