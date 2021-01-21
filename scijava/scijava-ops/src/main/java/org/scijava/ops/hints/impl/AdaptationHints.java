
package org.scijava.ops.hints.impl;

import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.hints.Hints;
import org.scijava.ops.hints.impl.DefaultOpHints.Adaptation;

/**
 * A set of {@link Hints} governing Adaptation procedures.
 * @author G
 *
 */
public class AdaptationHints extends AbstractHints {

	private AdaptationHints(Map<String, String> map) {
		super(map);
		setHint(Adaptation.IN_PROGRESS);
	}

	public static AdaptationHints generateHints(Hints hints) {
		// collect all old hints that are not Adaptable
		Map<String, String> map = new HashMap<>();
		hints.getHints().entrySet().parallelStream().filter(e -> e
			.getKey() != Adaptation.prefix).forEach(e -> map.put(e.getKey(), e
				.getValue()));

		// add Adaptation.NO
		AdaptationHints newHints = new AdaptationHints(map);

		return newHints;
	}

	@Override
	public String setHint(String hint) {
		return super.setHint(hint);
	}

	@Override
	public Hints getCopy() {
		return AdaptationHints.generateHints(this);
	}

}
