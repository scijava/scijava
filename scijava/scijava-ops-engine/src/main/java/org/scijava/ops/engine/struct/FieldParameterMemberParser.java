
package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.struct.MemberParser;

public class FieldParameterMemberParser implements
	MemberParser<FieldInstance, SynthesizedParameterMember<?>>
{

	@Override
	public List<SynthesizedParameterMember<?>> parse(FieldInstance source, Type structType)
	{
		if (source == null) return null;

		// obtain a parameterData (preferably one that scrapes the javadoc)
		ParameterData paramData = new LazilyGeneratedFieldParameterData(source);

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();

		try {
			FunctionalParameters.parseFunctionalParameters(items, structType,
					paramData);
		} catch (IllegalArgumentException exc) {
			throw new FunctionalTypeOpException(source.field(), exc);
		}
		return items;
	}

}
