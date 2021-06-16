package org.scijava.ops.hints.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.scijava.ops.hints.Hints;

/**
 * Abstract class containing behavior common to most {@link Hints}
 * implementations
 * 
 * @author Gabriel Selzer
 */
public abstract class AbstractHints implements Hints {

	// Hints are stored by their hint type (the middle term)
	final Map<String, String> hints;

	public AbstractHints(String... startingHints) {
		hints = new HashMap<>();
		for(String hint : startingHints)
			setHint(hint);
	}

	AbstractHints(Map<String, String> hints) {
		this.hints = hints;
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

}
