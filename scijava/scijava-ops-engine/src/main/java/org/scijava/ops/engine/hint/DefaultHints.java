
package org.scijava.ops.engine.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.api.Hints;

/**
 * Default Implementation of {@link Hints}
 *
 * @author Gabriel Selzer
 */
public class DefaultHints extends BasicHints {

	public DefaultHints() {
		super();
	}

	public DefaultHints(Map<String, String> hints) {
		super(hints);
	}

	public DefaultHints(UUID historyHash, Map<String, String> hints) {
		super(historyHash, hints);
	}

	@Override
	public Hints copy() {
		return new DefaultHints(historyHash, new HashMap<>(getHints()));
	}

	@Override
	public Hints copyRandomUUID() {
		return new DefaultHints(new HashMap<>(getHints()));
	}

}
