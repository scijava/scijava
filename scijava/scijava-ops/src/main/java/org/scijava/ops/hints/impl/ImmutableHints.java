package org.scijava.ops.hints.impl;

import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.hints.Hints;

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

	@Override
	public String setHint(String hint) {
		throw new UnsupportedOperationException("ImmutableHints cannot alter the original set of Hints!");
	}

	@Override
	public Hints getCopy() {
		return new ImmutableHints(new HashMap<>(getHints()));
	}

}
