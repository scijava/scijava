package org.scijava.ops.hints;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.scijava.ops.hints.DefaultOpHints.Adaptable;
import org.scijava.ops.hints.DefaultOpHints.Simplifiable;

/**
 * Default Implementation of 
 * @author G
 *
 */
public class DefaultHints extends AbstractHints {

	public DefaultHints() {
		super(new String[] {Simplifiable.YES, Adaptable.YES});
	}

	private DefaultHints(Map<String, String> hints) {
		super(hints);
	}

	@Override
	public Hints getCopy() {
		return new DefaultHints(new HashMap<>(getHints()));
	}

}
