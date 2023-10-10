
package org.scijava.ops.engine.matcher.simplify;

import java.util.HashSet;
import java.util.Set;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.MatchingConditions;
import org.scijava.ops.engine.matcher.impl.RuntimeSafeMatchingRoutine;
import org.scijava.priority.Priority;
import org.scijava.types.Types;

public class SimplificationMatchingRoutine extends RuntimeSafeMatchingRoutine {

	@Override
	public void checkSuitability(MatchingConditions conditions)
		throws OpMatchingException
	{
		if (conditions.hints().containsAny(BaseOpHints.Simplification.IN_PROGRESS,
			BaseOpHints.Simplification.FORBIDDEN)) //
			throw new OpMatchingException(
				"Simplification is not suitable: Simplification is disabled");
	}

	@Override
	protected Iterable<OpInfo> getInfos(OpEnvironment env,
		MatchingConditions conditions)
	{
		OpRequest req = conditions.request();
		Hints hints = conditions.hints().plus(BaseOpHints.Simplification.IN_PROGRESS);
		Iterable<OpInfo> suitableInfos = env.infos(req.getName(), hints);
		Set<OpInfo> simpleInfos = new HashSet<>();
		for (OpInfo info : suitableInfos) {
			boolean functionallyAssignable = Types.isAssignable(Types.raw(info
				.opType()), Types.raw(req.getType()));
			if (!functionallyAssignable) continue;
			try {
				InfoSimplificationGenerator gen = new InfoSimplificationGenerator(info,
					env);
				simpleInfos.add(gen.generateSuitableInfo(env, req, hints));
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
