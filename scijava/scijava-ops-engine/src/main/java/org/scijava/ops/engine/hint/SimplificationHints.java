
package org.scijava.ops.engine.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.engine.Hints;
import org.scijava.ops.engine.BaseOpHints.Simplification;

public class SimplificationHints extends AbstractHints {

	private SimplificationHints(UUID historyHash, Map<String, String> map) {
		super(historyHash, map);
		setHint(Simplification.IN_PROGRESS);
	}

	public static SimplificationHints generateHints(Hints hints, boolean generateID) {
		// collect all old hints that are not Adaptable
		Map<String, String> map = new HashMap<>();
		hints.getHints().entrySet().parallelStream().filter(e -> e
			.getKey() != Simplification.prefix).forEach(e -> map.put(e.getKey(), e
				.getValue()));

		// add Simplifiable.NO
		UUID id = generateID ? UUID.randomUUID() : hints.executionChainID();
		SimplificationHints newHints = new SimplificationHints(id, map);

		return newHints;
	}

	@Override
	public String setHint(String hint) {
		if (hint.equals(Simplification.ALLOWED))
			throw new IllegalArgumentException(
				"We cannot allow simplification during simplification; this would cause a recursive loop!");
		if (hint.equals(Simplification.FORBIDDEN))
			throw new IllegalArgumentException(
				"We cannot forbid simplification during simplification; to forbid simplification, use another Hint implementation!");
		return super.setHint(hint);
	}

	@Override
	public Hints getCopy(boolean generateID) {
		return SimplificationHints.generateHints(this, generateID);
	}

}
