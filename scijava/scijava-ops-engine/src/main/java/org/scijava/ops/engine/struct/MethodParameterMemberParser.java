
package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.common3.validity.ValidityException;
import org.scijava.common3.validity.ValidityProblem;
import org.scijava.ops.engine.util.internal.OpMethodUtils;
import org.scijava.struct.MemberParser;
import org.scijava.types.Types;

public class MethodParameterMemberParser implements
	MemberParser<Method, SynthesizedParameterMember<?>>
{
	@Override
	public List<SynthesizedParameterMember<?>> parse(Method source, Type structType)
		throws ValidityException
	{
		if (source == null) return null;

		source.setAccessible(true);

		// obtain a parameterData
		ParameterData paramData = new LazilyGeneratedMethodParameterData(source);

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		// Determine functional type
		Type functionalType;
		try {
			functionalType = OpMethodUtils.getOpMethodType(Types.raw(structType), source);
		}
		catch (IllegalArgumentException e) {
			problems.add(new ValidityProblem(e.getMessage()));
			functionalType = Types.parameterizeRaw(Types.raw(structType));
		}

		// Parse method level @Parameter annotations.
		FunctionalParameters.parseFunctionalParameters(items, problems, functionalType,
			paramData);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}



}
