
package org.scijava.ops.engine.impl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.api.OpCandidate;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.api.features.OpMatcher;

public class ManualOpCandidate extends OpCandidate {

	public ManualOpCandidate(OpEnvironment env, OpRef ref, OpInfo info,
		OpMatcher matcher)
	{
		super(env, ref, info, generateTypeVarAssigns(ref, info));
		if (!matcher.typesMatch(this)) throw new IllegalArgumentException(
			"OpInfo " + info +
				" cannot satisfy the requirements contained within OpRef " + ref);
	}

	private static Map<TypeVariable<?>, Type> generateTypeVarAssigns(OpRef ref,
		OpInfo info)
	{
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		if (!ref.typesMatch(info.opType(), typeVarAssigns))
			throw new IllegalArgumentException("OpInfo " + info +
				" cannot satisfy the requirements contained within OpRef " + ref);
		return typeVarAssigns;
	}

}
