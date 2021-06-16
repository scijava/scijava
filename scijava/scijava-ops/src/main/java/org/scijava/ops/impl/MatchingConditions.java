
package org.scijava.ops.impl;

import java.util.Objects;

import org.scijava.ops.hints.Hints;
import org.scijava.ops.matcher.OpRef;

public class MatchingConditions {

	private final OpRef ref;
	private final Hints hints;

	public MatchingConditions(OpRef ref, Hints hints) {
		this.ref = ref;
		this.hints = hints;
	}

	public OpRef getRef() {
		return ref;
	}

	public Hints getHints() {
		return hints;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof MatchingConditions)) return false;
		MatchingConditions thoseConditions = (MatchingConditions) that;
		return getRef().equals(thoseConditions.getRef()) && getHints().equals(
			thoseConditions.getHints());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getRef(), getHints());
	}

}
