package org.scijava.ops.hints;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.scijava.ops.hints.DefaultOpHints.Adaptable;
import org.scijava.ops.hints.DefaultOpHints.Simplifiable;

/**
 * Default Implementation of 
 * @author G
 *
 */
public class DefaultHints implements Hints {

	// Hints are stored by their hint type (the middle term)
	private final Map<String, String> hints;

	public DefaultHints() {
		this(new String[] {Simplifiable.YES, Adaptable.YES});
	}

	public DefaultHints(String... startingHints) {
		hints = new HashMap<>();
		for(String hint : startingHints)
			setHint(hint);
	}

	private DefaultHints(Map<String, String> hints) {
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
		return hints.containsKey(prefix) && hints.get(prefix).equals(
			hint);
	}

	@Override
	public String getHint(String prefix) {
		if (!hints.containsKey(prefix)) throw new NoSuchElementException(
			"No hint of type " + prefix + " is contained!");
		return hints.get(prefix);
	}

	private String getPrefix(String hint) {
		return hint.split("\\.")[0];
	}

	@Override
	public Map<String, String> getHints() {
		return hints;
	}

	@Override
	public Hints getCopy() {
		return new DefaultHints(new HashMap<>(getHints()));
	}

}
