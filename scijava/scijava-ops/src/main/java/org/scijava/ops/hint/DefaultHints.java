package org.scijava.ops.hint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.BaseOpHints.Adaptation;
import org.scijava.ops.BaseOpHints.DependencyMatching;
import org.scijava.ops.BaseOpHints.Simplification;
import org.scijava.ops.Hints;

/**
 * Default Implementation of {@link Hints}
 *
 * @author Gabriel Selzer
 */
public class DefaultHints extends AbstractHints {

	public DefaultHints() {
		super(new String[] {Simplification.ALLOWED, Adaptation.ALLOWED, DependencyMatching.NOT_IN_PROGRESS});
	}

	public DefaultHints(Map<String, String> hints) {
		super(hints);
	}

	public DefaultHints(UUID historyHash, Map<String, String> hints) {
		super(historyHash, hints);
	}

	@Override
	public Hints getCopy(boolean generateID) {
		if (generateID) {
			return new DefaultHints(new HashMap<>(getHints()));
		}
		return new DefaultHints(historyHash, new HashMap<>(getHints()));
	}

}
