package org.scijava.ops.engine.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.scijava.ops.engine.Hints;

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
		hints = new HashMap<>();
		for(String hint : startingHints)
			setHint(hint);

		this.historyHash = UUID.randomUUID();
	}

	public AbstractHints(final UUID historyHash, final String... startingHints) {
		hints = new HashMap<>();
		for(String hint : startingHints)
			setHint(hint);
		this.historyHash = historyHash;
	}

	AbstractHints(final Map<String, String> hints) {
		this.hints = hints;
		this.historyHash = UUID.randomUUID();
	}

	AbstractHints(final UUID historyHash, final Map<String, String> hints) {
		this.hints = hints;
		this.historyHash = historyHash;
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

	@Override
	public boolean equals(Object that) {
		if(!(that instanceof AbstractHints)) return false;
		AbstractHints thoseHints = (AbstractHints) that;
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
