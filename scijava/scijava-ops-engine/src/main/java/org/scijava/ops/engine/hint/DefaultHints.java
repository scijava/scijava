
package org.scijava.ops.engine.hint;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;

/**
 * Default Implementation of {@link Hints}
 *
 * @author Gabriel Selzer
 */
public class DefaultHints implements Hints {

	private final UUID uuid;

	// Hints are stored by their hint type (the middle term)
	final Set<String> hints;

	public DefaultHints(final String... startingHints) {
		this(null, startingHints);
	}

	public DefaultHints(final UUID historyHash, final String... startingHints) {
		this(historyHash, Arrays.asList(startingHints));
	}

	private DefaultHints(final UUID historyHash, final Collection<String> hints) {
		this.hints = new HashSet<>(hints);
		this.uuid = historyHash != null ? historyHash : UUID.randomUUID();
	}

	@Override
	public boolean contains(String hint) {
		return hints.contains(hint);
	}

	@Override
	public Hints copy() {
		return new DefaultHints(uuid(), hints);
	}

	@Override
	public Hints copyRandomUUID() {
		return new DefaultHints(null, hints);
	}

	/**
	 * {@link Hints} should be equal iff their {@link Map}s of hints are equal. We
	 * do not (and should not) consider the {@link UUID} in equality checking, as
	 * two calls to the {@link OpEnvironment} should use {@link Hints} with
	 * different {@code UUID}s. This should not prevent, for matching purposes,
	 * two {@link Hints} from being equal.
	 */
	@Override
	public boolean equals(Object that) {
		if(!(that instanceof DefaultHints)) return false;
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
		return new DefaultHints(uuid(), newHints);
	}

	@Override
	public Hints plus(String... h) {
		Set<String> newHints = new HashSet<>(this.hints);
		newHints.addAll(Arrays.asList(h));
		return new DefaultHints(uuid(), newHints);
	}

	@Override
	public UUID uuid() {
		return uuid;
	}


}
