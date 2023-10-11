
package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.engine.util.internal.OpMethodUtils;
import org.scijava.struct.MemberParser;
import org.scijava.types.Types;

public class MethodParameterMemberParser implements
	MemberParser<Method, SynthesizedParameterMember<?>>
{
	@Override
	public List<SynthesizedParameterMember<?>> parse(Method source, Type structType)
	{
		if (source == null) return null;

		source.setAccessible(true);

		// obtain a parameterData
		ParameterData paramData = new LazilyGeneratedMethodParameterData(source, Types.raw(structType));

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();

		// Determine functional type
		Type functionalType = OpMethodUtils.getOpMethodType(Types.raw(structType), source);

		// Parse method level @Parameter annotations.
		try {
			FunctionalParameters.parseFunctionalParameters(items, functionalType,
					paramData);
		} catch (IllegalArgumentException exc) {
			throw new FunctionalTypeOpException(source, exc);
		}

		return items;
	}



}
