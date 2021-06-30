
package org.scijava.ops.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scijava.param.ValidityException;
import org.scijava.param.ValidityProblem;
import org.scijava.struct.Member;
import org.scijava.types.Types;

public class FieldParameterMemberParser implements
	MemberParser<Field, SynthesizedParameterMember<?>>
{

	@Override
	public List<SynthesizedParameterMember<?>> parse(Field source) {
		Class<?> c = source.getDeclaringClass();
		if (c == null || source == null) return null;
		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData;
		try {
			paramData = new JavadocParameterData(source);
		}
		catch (IllegalArgumentException e) {
			paramData = new SynthesizedParameterData();
		}
		source.setAccessible(true);

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();
		final Set<String> names = new HashSet<>();
		final Type fieldType = Types.fieldType(source, c);

		Structs.checkModifiers(source.toString() + ": ", problems, source.getModifiers(),
			false, Modifier.FINAL);
		Structs.parseFunctionalParameters(items, names, problems, fieldType, paramData);
		// Fail if there were any problems.
		if (!problems.isEmpty()) {
			throw new ValidityException(problems);
		}
		return items;
	}

}
