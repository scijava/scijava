
package org.scijava.ops.engine;

import java.util.Objects;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpRequest;

public class MatchingConditions {

	private final OpRequest request;
	private final Hints hints;

	private MatchingConditions(OpRequest request, Hints hints) {
		this.request = request;
		this.hints = hints;
	}

	public static MatchingConditions from(OpRequest request, Hints h) {
		Hints hintCopy = h.copy();
		return new MatchingConditions(request, hintCopy);
	}

	public OpRequest request() {
		return request;
	}

	public Hints hints() {
		return hints;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof MatchingConditions)) return false;
		MatchingConditions thoseConditions = (MatchingConditions) that;
		return request().equals(thoseConditions.request()) && hints().equals(
			thoseConditions.hints());
	}

	@Override
	public int hashCode() {
		return Objects.hash(request(), hints());
	}

}
