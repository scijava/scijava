
package org.scijava.ops.impl;

import java.util.Objects;

import org.scijava.ops.Hints;
import org.scijava.ops.matcher.OpRef;

public class MatchingConditions {

	private final OpRef ref;
	private final Hints hints;

	private MatchingConditions(OpRef ref, Hints hints) {
		this.ref = ref;
		this.hints = hints;
	}

	public static MatchingConditions from(OpRef r, Hints h, boolean generateMatchingID) {
		return new MatchingConditions(r, h.getCopy(generateMatchingID));
	}

	public OpRef ref() {
		return ref;
	}

	public Hints hints() {
		return hints;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof MatchingConditions)) return false;
		MatchingConditions thoseConditions = (MatchingConditions) that;
		return ref().equals(thoseConditions.ref()) && hints().equals(
			thoseConditions.hints());
	}

	@Override
	public int hashCode() {
		return Objects.hash(ref(), hints());
	}

}
