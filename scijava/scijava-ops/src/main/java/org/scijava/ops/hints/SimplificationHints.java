
package org.scijava.ops.hints;

import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.hints.DefaultOpHints.Simplification;

public class SimplificationHints extends AbstractHints {

	private SimplificationHints(Map<String, String> map) {
		super(map);
	}

	public static SimplificationHints generateHints(Hints hints) {
		// collect all old hints that are not Adaptable
		Map<String, String> map = new HashMap<>();
		hints.getHints().entrySet().parallelStream().filter(e -> e
			.getKey() != Simplification.prefix).forEach(e -> map.put(e.getKey(), e
				.getValue()));

		// add Simplifiable.NO
		SimplificationHints newHints = new SimplificationHints(map);
		newHints.setHint(Simplification.FORBIDDEN);

		return newHints;
	}

	@Override
	public String setHint(String hint) {
		if (hint.equals(Simplification.ALLOWED)) throw new IllegalArgumentException(
			"We cannot allow simplification during simplification; this would cause a recursive loop!");
		return super.setHint(hint);
	}

	@Override
	public Hints getCopy() {
		return SimplificationHints.generateHints(this);
	}

}
