package org.scijava.ops.hints;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.scijava.ops.hints.DefaultOpHints.Simplifiable;


public class SimplificationHints implements Hints {

	Map<String, String> hints;

	private SimplificationHints(Map<String, String> map) {
		this.hints = map;
	}

	public static SimplificationHints generateHints(Hints hints) {
		// collect all old hints that are not Adaptable
		Map<String, String> map = new HashMap<>();
		hints.getHints().entrySet().parallelStream().filter(e -> e
			.getKey() != Simplifiable.prefix).forEach(e -> map.put(e.getKey(), e
				.getValue()));

		// add Simplifiable.NO
		SimplificationHints newHints = new SimplificationHints(map);
		newHints.setHint(Simplifiable.NO);

		return newHints;
	}

	@Override
	public String setHint(String hint) {
		if (hint.equals(Simplifiable.YES)) throw new IllegalArgumentException(
			"We cannot allow simplification during simplification; this would cause a recursive loop!");
		String prefix = getPrefix(hint);
		return hints.put(prefix, hint);
	}

	@Override
	public String getHint(String prefix) {
		if (!hints.containsKey(prefix)) throw new NoSuchElementException(
			"No hint of type " + prefix + " is contained!");
		return hints.get(prefix);
	}

	@Override
	public boolean containsHint(String hint) {
		String prefix = getPrefix(hint);
		return containsHintType(prefix) && hint.equals(hints.get(prefix));
	}

	@Override
	public boolean containsHintType(String prefix) {
		return hints.containsKey(prefix);
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
		return SimplificationHints.generateHints(this);
	}

}
