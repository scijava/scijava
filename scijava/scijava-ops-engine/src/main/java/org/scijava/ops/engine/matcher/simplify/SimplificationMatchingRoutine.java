
package org.scijava.ops.engine.matcher.simplify;

import java.util.HashSet;
import java.util.Set;

import org.scijava.ops.engine.matcher.impl.RuntimeSafeMatchingRoutine;
import org.scijava.priority.Priority;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.api.features.BaseOpHints.Simplification;
import org.scijava.ops.api.features.MatchingConditions;
import org.scijava.ops.api.features.OpMatchingException;
import org.scijava.ops.engine.matcher.simplify.InfoSimplificationGenerator;
import org.scijava.types.Types;

public class SimplificationMatchingRoutine extends RuntimeSafeMatchingRoutine {

	@Override
	public void checkSuitability(MatchingConditions conditions)
		throws OpMatchingException
	{
		if (conditions.hints().containsAny(Simplification.IN_PROGRESS,
			Simplification.FORBIDDEN)) //
			throw new OpMatchingException(
				"Simplification is not suitable: Simplification is disabled");
	}

	@Override
	protected Iterable<OpInfo> getInfos(OpEnvironment env,
		MatchingConditions conditions)
	{
		OpRef ref = conditions.ref();
		Hints hints = conditions.hints().plus(Simplification.IN_PROGRESS);
		Iterable<OpInfo> suitableInfos = env.infos(ref.getName(), hints);
		Set<OpInfo> simpleInfos = new HashSet<>();
		for (OpInfo info : suitableInfos) {
			boolean functionallyAssignable = Types.isAssignable(Types.raw(info
				.opType()), Types.raw(ref.getType()));
			if (!functionallyAssignable) continue;
			try {
				InfoSimplificationGenerator gen = new InfoSimplificationGenerator(info,
					env);
				simpleInfos.add(gen.generateSuitableInfo(env, ref, hints));
			}
			catch (Throwable t) {
				// NB: If we cannot generate the simplification,
				// move on to the next info
			}
		}
		return simpleInfos;
	}

	@Override
	public double priority() {
		return Priority.LOW - 1;
	}

}
