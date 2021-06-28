package org.scijava.ops.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.Hints;

/**
 * A {@link Hints} not modifiable after creation.
 *
 * @author Gabe Selzer
 */
public class ImmutableHints extends AbstractHints {

	public ImmutableHints(String[] h) {
		for (String hint : h) {
			String prefix = getPrefix(hint);
			hints.put(prefix, hint);
		}
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
	public Hints getCopy(boolean generateID) {
		UUID id = generateID ? UUID.randomUUID() : historyHash;
		return new ImmutableHints(new HashMap<>(getHints()), id);
	}

}
