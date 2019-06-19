package org.scijava.ops.matcher;

import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.OpUtils;

/**
 * Class representing the result from type matching done by the
 * {@link OpMatcher}. Contains the original candidates which match
 * the types specified by {@link OpRef} and the final matches that match all
 * inputs, outputs, and arguments.
 * 
 * @author David Kolb
 */
public class MatchingResult {

	private final List<OpCandidate> candidates;
	private final List<OpCandidate> matches;
	private final List<OpRef> originalQueries;

	public static MatchingResult empty(final List<OpRef> originalQueries) {
		return new MatchingResult(new ArrayList<OpCandidate>(), new ArrayList<OpCandidate>(), originalQueries);
	}
	
	public MatchingResult(final List<OpCandidate> candidates, final List<OpCandidate> matches, final List<OpRef> originalQueries) {
		this.candidates = candidates;
		this.matches = matches;
		this.originalQueries = originalQueries;
	}

	public List<OpRef> getOriginalQueries() {
		return originalQueries;
	}

	public List<OpCandidate> getCandidates() {
		return candidates;
	}

	public List<OpCandidate> getMatches() {
		return matches;
	}

	public OpCandidate singleMatch() throws OpMatchingException {
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
		} else {
			final String analysis = OpUtils.matchInfo(this);
			throw new OpMatchingException(analysis);
		}
	}
}
