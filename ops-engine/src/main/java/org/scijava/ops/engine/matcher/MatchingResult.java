/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.matcher;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.matcher.OpCandidate.StatusCode;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.util.Infos;

/**
 * Class representing the result from type matching done by a
 * {@link MatchingRoutine}. Contains the original candidates which match the
 * types specified by {@link OpRequest} and the final matches that match all
 * inputs, outputs, and arguments.
 *
 * @author David Kolb
 */
public class MatchingResult {

	private final List<OpCandidate> candidates;
	private final List<OpCandidate> matches;
	private final List<OpRequest> originalQueries;

	public static MatchingResult empty(final List<OpRequest> originalQueries) {
		return new MatchingResult(new ArrayList<>(), new ArrayList<>(),
			originalQueries);
	}

	public MatchingResult(final List<OpCandidate> candidates,
		final List<OpCandidate> matches, final List<OpRequest> originalQueries)
	{
		this.candidates = candidates;
		this.matches = matches;
		this.originalQueries = originalQueries;
	}

	public List<OpRequest> getOriginalQueries() {
		return originalQueries;
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

		// There is no clear matching Op
		final var analysis = MatchingResult.matchInfo(this);
		throw new OpMatchingException(analysis);
	}

	/**
	 * Gets a string with an analysis of a particular match request failure.
	 * <p>
	 * This method is used to generate informative exception messages when no
	 * matches, or too many matches, are found.
	 * </p>
	 *
	 * @param res The result of type matching
	 * @return A multi-line string describing the situation: 1) the type of match
	 *         failure; 2) the list of matching ops (if any); 3) the request
	 *         itself; and 4) the list of candidates including status (i.e.,
	 *         whether it matched, and if not, why not).
	 */
	public static String matchInfo(final MatchingResult res) {
		final var sb = new StringBuilder();

        var candidates = res.getCandidates();
        var matches = res.getMatches();

		final var request = res.getOriginalQueries().get(0);
		if (matches.isEmpty()) {
			// no matches
			sb.append("No matching '" + request.label() + "' op\n");
		}
		else {
			// multiple matches
			final var priority = matches.get(0).priority();
			sb.append("Multiple '" + request.label() + "' ops of priority " +
				priority + ":\n");
			if (typeCheckingIncomplete(matches)) {
				sb.append("Incomplete output type checking may have occurred!\n");
			}
            var count = 0;
			for (final var match : matches) {
				sb.append(++count + ". ");
				sb.append(match.toString() + "\n");
			}
		}

		// fail, with information about the request and candidates
		sb.append("\n");
		sb.append("Request:\n");
		sb.append("-\t" + request + "\n");
		sb.append("\n");
		sb.append("Candidates:\n");
		if (candidates.isEmpty()) {
			sb.append("-\t No candidates found!");
		}
        var count = 0;
		for (final var candidate : candidates) {
			sb.append(++count + ". ");
			sb.append("\t" + Infos.describe(candidate.opInfo()) + "\n");
			final var status = candidate.getStatus();
			if (status != null) sb.append("\t" + status + "\n");
			if (candidate.getStatusCode() == StatusCode.DOES_NOT_CONFORM) {
				// TODO: Conformity not yet implemented
				// // show argument values when a contingent op rejects them
				// for (final ModuleItem<?> item : inputs(info)) {
				// final Object value = item.getValue(candidate.getModule());
				// sb.append("\t\t" + item.getName() + " = " + value + "\n");
				// }
			}
		}
		return sb.toString();
	}

	/**
	 * Checks if incomplete type matching could have occurred. If we have several
	 * matches that do not have equal output types, the output type may not
	 * completely match the request as only raw type assignability will be checked
	 * at the moment.
	 *
	 * @param matches the {@link List} of {@link OpCandidate}s to check
	 * @return true iff incomplete type matching could have occurred.
	 */
	private static boolean typeCheckingIncomplete(List<OpCandidate> matches) {
		Type outputType = null;
		for (var match : matches) {
            var ts = match.opInfo().outputType();
			if (outputType == null || Objects.equals(outputType, ts)) {
				outputType = ts;
			}
			else {
				return true;
			}
		}
		return false;
	}
}
