package org.scijava.ops.python;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.scijava.ops.api.Hints;

public class TempHints  implements Hints {

	// Hints are stored by their hint type (the middle term)
	final Set<String> hints;

	public TempHints(final String... startingHints) {
		this(Arrays.asList(startingHints));
	}

	private TempHints(final Collection<String> hints) {
		this.hints = new HashSet<>(hints);
	}

	@Override
	public boolean contains(String hint) {
		return hints.contains(hint);
	}

	@Override
	public Hints copy() {
		return new TempHints(hints);
	}

	/**
	 * {@link Hints} should be equal iff their {@link Map}s of hints are equal.
	 */
	@Override
	public boolean equals(Object that) {
		if (!(that instanceof TempHints)) return false;
		TempHints thoseHints = (TempHints) that;
		return hints.equals(thoseHints.hints);
	}

	@Override
	public int hashCode() {
		return hints.hashCode();
	}

	@Override
	public Hints minus(String... h) {
		Set<String> newHints = new HashSet<>(this.hints);
		newHints.removeAll(Arrays.asList(h));
		return new TempHints(newHints);
	}

	@Override
	public Hints plus(String... h) {
		Set<String> newHints = new HashSet<>(this.hints);
		newHints.addAll(Arrays.asList(h));
		return new TempHints(newHints);
	}

}
