
package org.scijava.ops.engine.hint;

import java.util.Map;
import java.util.UUID;

import org.scijava.ops.api.Hints;

public class BasicHints extends AbstractHints {

	public BasicHints(String... startingHints) {
		super(startingHints);
	}

	public BasicHints(UUID historyHash, String... startingHints) {
		super(historyHash, startingHints);
	}

	public BasicHints(Map<String, String> hints) {
		super(hints);
	}

	public BasicHints(UUID historyHash, Map<String, String> hints) {
		super(historyHash, hints);
	}

	@Override
	public Hints copy() {
		return new BasicHints(executionChainID(), getHints());
	}

	@Override
	public Hints copyRandomUUID() {
		return new BasicHints(getHints());
	}

}
