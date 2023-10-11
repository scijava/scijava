
package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ops.engine.exceptions.impl.OpDependencyPositionException;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.MemberParser;

/**
 * Looks for {@link OpDependency} annotations on
 * {@link java.lang.reflect.Parameter}s. For each such parameter with an
 * {@link OpDependency} annotation, this class creates a
 * {@link MethodParameterOpDependencyMember}.
 *
 * @author Gabriel Selzer
 */
public class MethodOpDependencyMemberParser implements
	MemberParser<Method, MethodParameterOpDependencyMember<?>>
{

	/**
	 * Parses out the {@link MethodParameterOpDependencyMember}s from {@code
	 * source}
	 * 
	 * @param source the {@link Object} to parse
	 * @param structType TODO
	 * @return a {@link List} of all {@link MethodParameterOpDependencyMember}s
	 *         described via the {@link OpDependency} annotation in {@code source}
	 */
	@Override
	public List<MethodParameterOpDependencyMember<?>> parse(Method source,
		Type structType)
	{
		if (source == null) return null;

		source.setAccessible(true);

		final ArrayList<MethodParameterOpDependencyMember<?>> items = new ArrayList<>();

		// Parse method level @OpDependency annotations.
		parseMethodOpDependencies(items, source);

		return items;
	}

	private static void parseMethodOpDependencies(
		final List<MethodParameterOpDependencyMember<?>> items,
		final Method annotatedMethod)
	{
		Boolean[] isDependency = Arrays.stream(annotatedMethod.getParameters()) //
			.map(param -> param.isAnnotationPresent(OpDependency.class)).toArray(Boolean[]::new);
		for (int i = 0; i < isDependency.length - 1; i++) {
			if (!isDependency[i] && isDependency[i + 1]) {
				// OpDependencies must come first so that they can be curried within
				// LambdaMetafactory
				throw new OpDependencyPositionException(annotatedMethod);
			}
		}

		final java.lang.reflect.Parameter[] methodParams = annotatedMethod
			.getParameters();

		for (java.lang.reflect.Parameter methodParam : methodParams) {
			final OpDependency dependency =
					methodParam.getAnnotation(OpDependency.class);
			if (dependency == null) continue;

			final String name = methodParam.getName();
			final Type methodParamType = methodParam.getParameterizedType();
			items.add(new MethodParameterOpDependencyMember<>( //
					name, //
					"", //
					methodParamType, //
					dependency //
			));
		}
	}

}
