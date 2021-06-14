package org.scijava.ops.hints.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.scijava.ops.hints.Hints;
import org.scijava.ops.hints.BaseOpHints.Adaptation;
import org.scijava.ops.hints.BaseOpHints.Simplification;

/**
 * Default Implementation of {@link Hints}
 *
 * @author Gabriel Selzer
 */
public class DefaultHints extends AbstractHints {

	public DefaultHints() {
		super(new String[] {Simplification.ALLOWED, Adaptation.ALLOWED});
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
