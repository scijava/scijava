
package org.scijava.ops.engine.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.BaseOpHints.Adaptation;

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
		UUID id = generateID ? UUID.randomUUID() : hints.executionChainID();
		AdaptationHints newHints = new AdaptationHints(id, map);

		return newHints;
	}

	@Override
	public String setHint(String hint) {
		return super.setHint(hint);
	}

	@Override
	public Hints getCopy(boolean generateID) {
		return AdaptationHints.generateHints(this, generateID);
	}

}
