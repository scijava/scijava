
package org.scijava.ops.engine.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.BaseOpHints.Simplification;

public class SimplificationHints extends AbstractHints {

	private SimplificationHints(UUID historyHash, Map<String, String> map) {
		super(historyHash, map);
		setHint(Simplification.IN_PROGRESS);
	}

	public static SimplificationHints generateHints(Hints hints, boolean generateID) {
		// collect all old hints that are not Adaptable
		Map<String, String> map = new HashMap<>();
		hints.getHints().entrySet().parallelStream().filter(e -> e
			.getKey() != Simplification.PREFIX).forEach(e -> map.put(e.getKey(), e
				.getValue()));

		// add Simplifiable.NO
		if (generateID) {
			return new SimplificationHints(null, map);
		}
		return new SimplificationHints(hints.executionChainID(), map);
	}

	@Override
	public String setHint(String hint) {
		if (hint.equals(Simplification.FORBIDDEN))
			throw new IllegalArgumentException(
				"We cannot forbid simplification during simplification; to forbid simplification, use another Hint implementation!");
		return populateHint(hint);
	}

	@Override
	public Hints copy(boolean generateID) {
		return SimplificationHints.generateHints(this, generateID);
	}

}
