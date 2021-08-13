package org.scijava.ops.engine.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.api.Hints;

/**
 * A {@link Hints} not modifiable after creation.
 *
 * @author Gabe Selzer
 */
public class ImmutableHints extends AbstractHints {

	public ImmutableHints(String[] h) {
		super(h);
	}

	public ImmutableHints(Map<String, String> h) {
		super(h);
	}

	public ImmutableHints(Map<String, String> h, UUID historyHash) {
		super(historyHash, h);
		
	}

	@Override
	public String setHint(String hint) {
		throw new UnsupportedOperationException("ImmutableHints cannot alter the original set of Hints!");
	}

	@Override
	public UUID executionChainID() {
		return historyHash;
	}

	@Override
	public Hints copy() {
		Map<String, String> mapCopy = new HashMap<>(getHints());
		return new ImmutableHints(mapCopy, historyHash);
	}

	@Override
	public Hints copyRandomUUID() {
		Map<String, String> mapCopy = new HashMap<>(getHints());
		return new ImmutableHints(mapCopy, null);
	}

}
