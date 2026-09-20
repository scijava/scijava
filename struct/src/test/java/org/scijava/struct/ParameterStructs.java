/*-
 * #%L
 * A library for building and introspecting structs.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scijava.common3.Annotations;
import org.scijava.common3.Classes;
import org.scijava.common3.Types;

/**
 * Utility functions for working with {@link Parameter}-based structs.
 * 
 * @author Curtis Rueden
 */
public final class ParameterStructs {

	public static <C> StructInstance<C> create(final C object)
		throws ValidityException
	{
		return structOf(object.getClass()).createInstance(object);
	}

	public static Struct structOf(final Class<?> type)
		throws ValidityException
	{
		final List<Member<?>> items = parse(type);
		return () -> items;
	}

	public static List<Member<?>> parse(final Class<?> type)
		throws ValidityException
	{
		if (type == null) return null;

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
			final Type[] itemTypes = Types.typeParamsOf(type, functionalType);
			// TODO: Consider allowing partial override of class @Parameters.
			if (p.length == paramCount) {
				for (int i=0; i<p.length; i++) {
					String key = p[i].key();
					final Type itemType = i < itemTypes.length ? itemTypes[i] : null;
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

		final List<Field> fields = Annotations.annotatedFields(type,
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

	public static <T> Field field(final Member<T> item) {
		if (item instanceof FieldParameterMember) {
			final FieldParameterMember<T> fpItem = (FieldParameterMember<T>) item;
			return fpItem.getField();
		}
		return null;
	}

	// -- Helper methods --

	private static boolean isImmutable(final Class<?> type) {
		// NB: All eight primitive types, as well as the boxed primitive
		// wrapper classes, as well as strings, are immutable objects.
		return Classes.isNumber(type) || Classes.isText(type) || //
			Classes.isBoolean(type);
	}

	/**
	 * Finds the class declaring {@code @Parameter} annotations. They might be on
	 * this type, on a supertype, or an implemented interface.
	 */
	private static Class<?> findParametersDeclaration(final Class<?> type) {
		if (type == null) return null;
		final Deque<Class<?>> types = new ArrayDeque<>();
		types.add(type);
		while (!types.isEmpty()) {
			final Class<?> candidate = types.pop();
			if (candidate.getAnnotation(Parameters.class) != null) return candidate;
			final Class<?> superType = candidate.getSuperclass() ;
			if (superType != null) types.add(superType);
			types.addAll(Arrays.asList(candidate.getInterfaces()));
		}
		return null;
	}

	private static Class<?> findFunctionalInterface(Class<?> type) {
		if (type == null) return null;
		if (type.getAnnotation(FunctionalInterface.class) != null) return type;
		for (Class<?> iface : type.getInterfaces()) {
			final Class<?> result = findFunctionalInterface(iface);
			if (result != null) return result;
		}
		return findFunctionalInterface(type.getSuperclass());
	}

	private static boolean checkValidity(Parameter param, String name,
		Class<?> type, boolean isFinal, Set<String> names,
		ArrayList<ValidityProblem> problems)
	{
		boolean valid = true;

		final boolean isMessage = param.visibility() == ItemVisibility.MESSAGE;
		if (isFinal && !isMessage) {
			// NB: Final parameters are bad because they cannot be modified.
			final String error = "Invalid final parameter: " + name;
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		if (names.contains(name)) {
			// NB: Shadowed parameters are bad because they are ambiguous.
			final String error = "Invalid duplicate parameter: " + name;
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		if ((param.type() == ItemIO.CONTAINER || param.type() == ItemIO.MUTABLE) && isImmutable(type)) {
			// NB: The CONTAINER and MUTABLE types are intended to be changed
			// in-place somehow. But immutable parameters cannot be changed in
			// such a manner, so it makes no sense to label them as BOTH.
			final String error = "Immutable BOTH parameter: " + name;
			problems.add(new ValidityProblem(error));
			valid = false;
		}

		return valid;
	}
}
