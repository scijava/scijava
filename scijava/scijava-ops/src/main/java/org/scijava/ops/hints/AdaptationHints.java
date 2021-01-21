
package org.scijava.ops.hints;

import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.hints.DefaultOpHints.Adaptable;

public class AdaptationHints extends AbstractHints {

	private AdaptationHints(Map<String, String> map) {
		super(map);
	}

	public static AdaptationHints generateHints(Hints hints) {
		// collect all old hints that are not Adaptable
		Map<String, String> map = new HashMap<>();
		hints.getHints().entrySet().parallelStream().filter(e -> e
			.getKey() != Adaptable.prefix).forEach(e -> map.put(e.getKey(), e
				.getValue()));

		// add Adaptable.NO
		AdaptationHints newHints = new AdaptationHints(map);
		newHints.setHint(Adaptable.NO);

		return newHints;
	}

	@Override
	public String setHint(String hint) {
		if (hint.equals(Adaptable.YES)) throw new IllegalArgumentException(
			"We cannot allow adaptation during adaptation; this would cause a recursive loop!");
		return super.setHint(hint);
	}

	@Override
	public Hints getCopy() {
		return AdaptationHints.generateHints(this);
	}

}
