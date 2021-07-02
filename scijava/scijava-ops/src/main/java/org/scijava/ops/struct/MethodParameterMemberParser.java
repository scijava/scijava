
package org.scijava.ops.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scijava.ValidityProblem;
import org.scijava.ops.OpMethod;
import org.scijava.ops.util.OpMethodUtils;
import org.scijava.struct.MemberParser;
import org.scijava.struct.ValidityException;
import org.scijava.types.Types;

public class MethodParameterMemberParser implements
	MemberParser<Method, SynthesizedParameterMember<?>>
{

	@Override
	public List<SynthesizedParameterMember<?>> parse(Method source)
		throws ValidityException
	{
		if (source == null) return null;

		source.setAccessible(true);

		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData;
		try {
			paramData = new JavadocParameterData(source);
		}
		catch (IllegalArgumentException e) {
			paramData = new SynthesizedParameterData();
		}

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();
		final Set<String> names = new HashSet<>();
		final OpMethod methodAnnotation = source.getAnnotation(OpMethod.class);

		// Determine functional type
		Type functionalType;
		try {
			functionalType = OpMethodUtils.getOpMethodType(methodAnnotation.type(), source);
		}
		catch (IllegalArgumentException e) {
			problems.add(new ValidityProblem(e.getMessage()));
			functionalType = Types.parameterizeRaw(methodAnnotation.type());
		}

		// Parse method level @Parameter annotations.
		FunctionalParameters.parseFunctionalParameters(items, names, problems, functionalType,
			paramData);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}



}
