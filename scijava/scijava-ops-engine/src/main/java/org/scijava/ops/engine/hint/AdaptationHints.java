
package org.scijava.ops.engine.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.BaseOpHints.Adaptation;

/**
 * A set of {@link Hints} governing Adaptation procedures.
 * @author G
 *
 */
public class AdaptationHints extends AbstractHints {

	private AdaptationHints(UUID historyHash, Map<String, String> map) {
		super(historyHash, map);
		setHint(Adaptation.IN_PROGRESS);
	}

	public static AdaptationHints generateHints(Hints hints, boolean generateID) {
		// collect all old hints that are not Adaptable
		Map<String, String> map = new HashMap<>();
		hints.getHints().entrySet().parallelStream().filter(e -> e
			.getKey() != Adaptation.PREFIX).forEach(e -> map.put(e.getKey(), e
				.getValue()));

		// add Adaptation.NO
		if (generateID) {
			return new AdaptationHints(null, map);
		}
		return new AdaptationHints(hints.executionChainID(), map);
	}

	@Override
	public String setHint(String hint) {
		if (hint.equals(Adaptation.FORBIDDEN))
			throw new IllegalArgumentException(
				"We cannot forbid simplification during simplification; to forbid simplification, use another Hint implementation!");
		return populateHint(hint);
	}

	@Override
	public Hints copy(boolean generateID) {
		return AdaptationHints.generateHints(this, generateID);
	}

}
