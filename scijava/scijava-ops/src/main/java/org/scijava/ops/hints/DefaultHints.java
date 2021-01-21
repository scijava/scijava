package org.scijava.ops.hints;

import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.hints.DefaultOpHints.Adaptation;
import org.scijava.ops.hints.DefaultOpHints.Simplification;

/**
 * Default Implementation of 
 * @author G
 *
 */
public class DefaultHints extends AbstractHints {

	public DefaultHints() {
		super(new String[] {Simplification.ALLOWED, Adaptation.ALLOWED});
	}

	private DefaultHints(Map<String, String> hints) {
		super(hints);
	}

	@Override
	public Hints getCopy() {
		return new DefaultHints(new HashMap<>(getHints()));
	}

}
