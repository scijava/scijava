
package org.scijava.ops.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ValidityProblem;
import org.scijava.ops.MethodParameterOpDependencyMember;
import org.scijava.ops.OpDependency;
import org.scijava.ops.ValidityException;

public class MethodOpDependencyMemberParser implements
	MemberParser<Method, MethodParameterOpDependencyMember<?>>
{

	@Override
	public List<MethodParameterOpDependencyMember<?>> parse(Method source)
		throws ValidityException
	{
		if (source == null) return null;

		source.setAccessible(true);

		final ArrayList<MethodParameterOpDependencyMember<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		// Parse method level @OpDependency annotations.
		parseMethodOpDependencies(items, source);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}

	private static void parseMethodOpDependencies(final List<MethodParameterOpDependencyMember<?>> items,
		final Method annotatedMethod)
	{
		final java.lang.reflect.Parameter[] methodParams = annotatedMethod
			.getParameters();
		final java.lang.reflect.Parameter[] opDependencyParams = getOpDependencies(methodParams);

		for (java.lang.reflect.Parameter param : opDependencyParams) {
			final OpDependency dependency = param.getAnnotation(OpDependency.class);
			final Type methodParamType = param.getParameterizedType();
			final MethodParameterOpDependencyMember<?> item = new MethodParameterOpDependencyMember<>(
				param, methodParamType, dependency);
			items.add(item);
		}
	}

	private static java.lang.reflect.Parameter[] getOpDependencies(
		java.lang.reflect.Parameter[] methodParams)
	{
		return Arrays //
			.stream(methodParams) //
			.filter(param -> param.getAnnotation(OpDependency.class) != null) //
			.toArray(java.lang.reflect.Parameter[]::new);
	}

}
