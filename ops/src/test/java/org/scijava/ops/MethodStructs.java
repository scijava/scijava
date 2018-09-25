package org.scijava.ops;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.param.FieldParameterMember;
import org.scijava.param.FunctionalParameterMember;
import org.scijava.param.Parameter;
import org.scijava.param.ParameterMember;
import org.scijava.param.Parameters;
import org.scijava.param.ValidityException;
import org.scijava.param.ValidityProblem;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.util.ClassUtils;
import org.scijava.util.Types;

public final class MethodStructs {

	public static Struct function2(final BiFunction<?, ?, ?> function)
		throws ValidityException
	{
		final List<Member<?>> items = parse(function);
		return () -> items;
	}

	public static Struct function(final Function<?, ?> function)
		throws ValidityException
	{
		final List<Member<?>> items = parse(function);
		return () -> items;
	}

	public static List<Member<?>> parse(final Method m)
		throws ValidityException
	{
		if (m == null) return null;

		m.getGenericParameterTypes();

		final ArrayList<Member<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		// NB: Reject abstract classes.
		if (Modifier.isAbstract(type.getModifiers())) {
			problems.add(new ValidityProblem("Struct class is abstract"));
		}

		final Set<String> names = new HashSet<>();

		// Parse class level (i.e., generic) @Parameter annotations.

		final Class<?> paramsClass = findParametersDeclaration(type);
		if (paramsClass != null) {
			final Parameters params = paramsClass.getAnnotation(Parameters.class);
			final Class<?> functionalType = findFunctionalInterface(paramsClass);
			final Parameter[] p = params.value();
			final int paramCount = functionalType.getTypeParameters().length;
			// TODO: Consider allowing partial override of class @Parameters.
			if (p.length == paramCount) {
				for (int i=0; i<p.length; i++) {
					String key = p[i].key();
					final Type itemType = Types.param(type, functionalType, i);
					final Class<?> rawItemType = Types.raw(itemType);
					final boolean valid = checkValidity(p[i], key, rawItemType, false,
						names, problems);
					if (!valid) continue; // NB: Skip invalid parameters.

					// add item to the list
					// TODO make more DRY
					try {
						final ParameterMember<?> item = //
							new FunctionalParameterMember<>(itemType, p[i]);
						names.add(key);
						items.add(item);
					}
					catch (final ValidityException exc) {
						problems.addAll(exc.problems());
					}
				}
			}
			else {
				problems.add(new ValidityProblem("Need " + paramCount +
					" parameters for " + functionalType.getName() + " but got " +
					p.length));
			}
		}

		// Parse field level @Parameter annotations.

		final List<Field> fields = ClassUtils.getAnnotatedFields(type,
			Parameter.class);

		for (final Field f : fields) {
			f.setAccessible(true); // expose private fields

			final Parameter param = f.getAnnotation(Parameter.class);

			final String name = f.getName();
			final boolean isFinal = Modifier.isFinal(f.getModifiers());
			final boolean valid = checkValidity(param, name, f.getType(),
				isFinal, names, problems);
			if (!valid) continue; // NB: Skip invalid parameters.

			// add item to the list
			try {
				final ParameterMember<?> item = new FieldParameterMember<>(f, type);
				names.add(name);
				items.add(item);
			}
			catch (final ValidityException exc) {
				problems.addAll(exc.problems());
			}
		}

		// Fail if there were any problems.

		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}
}
