
package org.scijava.ops.adapt;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import org.scijava.log.Logger;
import org.scijava.ops.OpEnvironment;
import org.scijava.ops.OpInfo;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpMatcher;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.OpRef;
import org.scijava.struct.StructInstance;

/**
 * Container class for a possible operation match between an {@link OpRef} and
 * an <b>adapted</b> {@link OpInfo}, as computed by the {@link OpMatcher}.
 *
 * @author Gabriel Selzer
 * @see OpEnvironment
 * @see OpMatcher
 */
public class AdaptedOpCandidate extends OpCandidate {

	public AdaptedOpCandidate(OpEnvironment env, Logger log, OpRef ref,
		OpInfo info, Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		super(env, log, ref, info, typeVarAssigns);
	}

	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies)
		throws OpMatchingException
	{
		StructInstance<?> inst = opInfo().createOpInstance(dependencies);
		return inst;
	}

}
