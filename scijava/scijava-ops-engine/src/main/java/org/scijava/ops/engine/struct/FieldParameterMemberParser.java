
package org.scijava.ops.engine.struct;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.common3.validity.ValidityException;
import org.scijava.common3.validity.ValidityProblem;
import org.scijava.struct.MemberParser;
import org.scijava.struct.Structs;

public class FieldParameterMemberParser implements
	MemberParser<FieldInstance, SynthesizedParameterMember<?>>
{

	@Override
	public List<SynthesizedParameterMember<?>> parse(FieldInstance source, Type structType)
		throws ValidityException
	{
		if (source == null) return null;

		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData = new LazilyGeneratedFieldParameterData(source);

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		Structs.checkModifiers(source.toString() + ": ", problems, source.field()
			.getModifiers(), false, Modifier.FINAL);
		FunctionalParameters.parseFunctionalParameters(items, problems, structType,
			paramData);
		// Fail if there were any problems.
		if (!problems.isEmpty()) {
			throw new ValidityException(problems);
		}
		return items;
	}

}
