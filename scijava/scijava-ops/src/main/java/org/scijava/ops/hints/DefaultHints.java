package org.scijava.ops.hints;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.scijava.ops.hints.DefaultOpHints.Adaptable;
import org.scijava.ops.hints.DefaultOpHints.Simplifiable;

/**
 * Default Implementation of 
 * @author G
 *
 */
public class DefaultHints implements Hints {

	private final Map<String, String> hints;

	public DefaultHints() {
		this(new String[] {Simplifiable.YES, Adaptable.YES});
	}

	public DefaultHints(String... startingHints) {
		hints = new HashMap<>();
		for(String hint : startingHints)
			setHint(hint);
	}

	@Override
	public String setHint(String hint) {
		String[] hintParts = getHintParts(hint);
		validateHintParts(hint, hintParts);
		return hints.put(hintParts[0], hintParts[1]);
	}

	@Override
	public boolean containsHintType(String hintType) {
		String[] hintParts = getHintParts(hintType);
		return hints.containsKey(hintParts[1]);
	}

	@Override
	public boolean isActive(String hint) {
		return hints.containsKey(hint);
	}

	@Override
	public String getHint(String hintType) {
		String[] hintParts = getHintParts(hintType);
		if (!hints.containsKey(hintParts[1])) throw new NoSuchElementException(
			"No hint of type " + hintType + " is contained!");
		return buildHint(hintParts[0], hintParts[1], hints.get(hintParts[1]));
		
	}

	private String buildHint(String base, String prefix, String suffix) {
		return String.join(".", base, prefix, suffix);
	}

	private String[] getHintParts(String hint) {
		return hint.split(".");
	}

	private void validateHintParts(String hint, String[] hintParts) {
		if (hintParts.length != 3) throw new IllegalArgumentException("Hint " +
			hint +
			" does not conform to hint structure <Hints.BASE>.<hint_type>.<hint>!");
		if (!hintParts[0].equals(Hints.BASE)) throw new IllegalArgumentException(
			"Hint " + hint + " must begin with " + Hints.BASE);
	}

}
