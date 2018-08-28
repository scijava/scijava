package org.scijava.ops.base;

import java.util.List;

/**
 * Class representing the result from type matching done by the
 * {@link OpTypeMatchingService}. Contains the original candidates which match
 * the types specified by {@link OpRef} and the final matches that match all
 * inputs, outputs, and arguments.
 * 
 * @author David Kolb
 */
public class MatchingResult {

	private final List<OpCandidate> candidates;
	private final List<OpCandidate> matches;

	public MatchingResult(final List<OpCandidate> candidates, final List<OpCandidate> matches) {
		this.candidates = candidates;
		this.matches = matches;
	}

	public List<OpCandidate> getCandidates() {
		return candidates;
	}

	public List<OpCandidate> getMatches() {
		return matches;
	}

	public OpCandidate singleMatch() {
		if (matches.size() == 1) {
			// if (log.isDebug()) {
			// log.debug("Selected '" + match.getRef().getLabel() + "' op: " +
			// match.opInfo().opClass().getName());
			// }

			// TODO: DO we still need this initialization?
			// // initialize the op, if appropriate
			// if (m.object() instanceof Initializable) {
			// ((Initializable) m.object()).initialize();
			// }

			return matches.get(0);
		}
		final String analysis = OpUtils.matchInfo(this);
		throw new IllegalArgumentException(analysis);
	}
}
