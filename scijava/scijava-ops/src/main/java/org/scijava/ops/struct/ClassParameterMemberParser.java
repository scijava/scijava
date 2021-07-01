
package org.scijava.ops.struct;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scijava.ValidityProblem;
import org.scijava.ops.ValidityException;

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
		final Set<String> names = new HashSet<>();

		// NB: Reject abstract classes.
		Structs.checkModifiers(source.getName() + ": ", problems, source.getModifiers(), true, Modifier.ABSTRACT);

		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData;
		try {
			paramData = new JavadocParameterData(source);
		} catch(NullPointerException | IllegalArgumentException e) {
			paramData = new SynthesizedParameterData();
		}

		Structs.parseFunctionalParameters(items, names, problems, source, paramData);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}

}
