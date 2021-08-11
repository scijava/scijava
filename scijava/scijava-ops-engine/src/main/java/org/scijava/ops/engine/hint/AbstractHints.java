package org.scijava.ops.engine.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;

/**
 * Abstract class containing behavior common to most {@link Hints}
 * implementations
 * 
 * @author Gabriel Selzer
 */
public abstract class AbstractHints implements Hints {

	final UUID historyHash;

	// Hints are stored by their hint type (the middle term)
	final Map<String, String> hints;

	public AbstractHints(final String... startingHints) {
		this(null, startingHints);
	}

	AbstractHints(final Map<String, String> hints) {
		this(null, hints);
	}

	public AbstractHints(final UUID historyHash, final String... startingHints) {
		hints = new HashMap<>();
		for(String hint : startingHints)
			setHint(hint);
		this.historyHash = historyHash != null ? historyHash : UUID.randomUUID();
	}

	AbstractHints(final UUID historyHash, final Map<String, String> hints) {
		this.hints = hints;
		this.historyHash = historyHash != null ? historyHash : UUID.randomUUID();
	}

	@Override
	public String setHint(String hint) {
		String prefix = getPrefix(hint);
		return hints.put(prefix, hint);
	}

	@Override
	public boolean containsHintType(String prefix) {
		return hints.containsKey(prefix);
	}

	@Override
	public boolean containsHint(String hint) {
		String prefix = getPrefix(hint);
		return hints.containsKey(prefix) && hint.equals(hints.get(prefix));
	}

	@Override
	public String getHint(String prefix) {
		if (!hints.containsKey(prefix)) throw new NoSuchElementException(
			"No hint of type " + prefix + " is contained!");
		return hints.get(prefix);
	}

	protected String getPrefix(String hint) {
		return hint.split("\\.")[0];
	}

	@Override
	public Map<String, String> getHints() {
		return hints;
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
		if(!(that instanceof Hints)) return false;
		Hints thoseHints = (Hints) that;
		return getHints().equals(thoseHints.getHints());
	}

	@Override
	public int hashCode() {
		return hints.hashCode();
	}

@Override
	public UUID executionChainID() {
		return historyHash;
	}

}
