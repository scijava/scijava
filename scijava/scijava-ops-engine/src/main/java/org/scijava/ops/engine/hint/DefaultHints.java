
package org.scijava.ops.engine.hint;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.scijava.ops.api.Hints;

/**
 * Default Implementation of {@link Hints}
 *
 * @author Gabriel Selzer
 */
public class DefaultHints implements Hints {

	// Hints are stored by their hint type (the middle term)
	final Set<String> hints;

	public DefaultHints(final String... startingHints) {
		this(Arrays.asList(startingHints));
	}

	private DefaultHints(final Collection<String> hints) {
		this.hints = new HashSet<>(hints);
	}

	@Override
	public boolean contains(String hint) {
		return hints.contains(hint);
	}

	@Override
	public Hints copy() {
		return new DefaultHints(hints);
	}

	/**
	 * {@link Hints} should be equal iff their {@link Map}s of hints are equal.
	 */
	@Override
	public boolean equals(Object that) {
		if (!(that instanceof DefaultHints)) return false;
		DefaultHints thoseHints = (DefaultHints) that;
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
		return new DefaultHints(newHints);
	}

	@Override
	public Hints plus(String... h) {
		Set<String> newHints = new HashSet<>(this.hints);
		newHints.addAll(Arrays.asList(h));
		return new DefaultHints(newHints);
	}

}
