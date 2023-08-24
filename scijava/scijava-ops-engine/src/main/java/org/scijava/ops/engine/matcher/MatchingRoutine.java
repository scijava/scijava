
package org.scijava.ops.engine.matcher;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpRetrievalException;
import org.scijava.ops.engine.MatchingConditions;
import org.scijava.ops.engine.OpCandidate;

/**
 * A plugin type employing a particular strategy to generate an
 * {@link OpCandidate}.
 * 
 * @author Gabriel Selzer
 */
public interface MatchingRoutine extends
	Comparable<MatchingRoutine>
{

	void checkSuitability(MatchingConditions conditions)
		throws OpRetrievalException;

	@Override
	default int compareTo(MatchingRoutine o) {
		return (int) (priority() - o.priority());
	}

	OpCandidate findMatch(MatchingConditions conditions, OpMatcher matcher,
		OpEnvironment env) throws OpRetrievalException;

	/**
	 * Generates an {@link OpCandidate} from the Ops in the provided
	 * {@link OpEnvironment}, conforming to the provided
	 * {@link MatchingConditions}
	 * 
	 * @param conditions the {@link MatchingConditions} the returned Op must
	 *          conform to
	 * @param matcher the {@link OpMatcher} responsible for matching
	 * @param env the {@link OpEnvironment} containing the Ops able to be matched
	 * @return an {@OpCandidate}
	 */
	default OpCandidate match(MatchingConditions conditions, OpMatcher matcher,
		OpEnvironment env)
	{
		checkSuitability(conditions);
		return findMatch(conditions, matcher, env);
	}

	/**
	 * The priority of this {@link MatchingRoutine}
	 *
	 * @return the priority
	 */
	double priority();

}
