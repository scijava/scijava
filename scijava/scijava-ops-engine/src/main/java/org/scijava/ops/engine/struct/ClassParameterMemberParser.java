
package org.scijava.ops.engine.struct;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ValidityProblem;
import org.scijava.struct.MemberParser;
import org.scijava.struct.ValidityException;

public class ClassParameterMemberParser implements
	MemberParser<Class<?>, SynthesizedParameterMember<?>>
{

	@Override
	public List<SynthesizedParameterMember<?>> parse(Class<?> source)
		throws ValidityException
	{
		if (source == null) return null;

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		// NB: Reject abstract classes.
		org.scijava.struct.Structs.checkModifiers(source.getName() + ": ", problems, source.getModifiers(), true, Modifier.ABSTRACT);

		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData;
		try {
			paramData = new JavadocParameterData(source);
		} catch(NullPointerException | IllegalArgumentException e) {
			paramData = new SynthesizedParameterData();
		}

		FunctionalParameters.parseFunctionalParameters(items, problems, source, paramData);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}

}
