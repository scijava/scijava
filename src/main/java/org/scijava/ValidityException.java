package org.scijava;

import java.util.Collections;
import java.util.List;

/**
 * hasdjf
 *
 * @author Curtis Rueden
 */
public final class ValidityException extends Exception {

	private final List<ValidityProblem> problems;

	public ValidityException(final String message) {
		this(Collections.singletonList(new ValidityProblem(message)));
	}

	public ValidityException(final List<ValidityProblem> problems) {
		this.problems = problems;
	}

	public List<ValidityProblem> problems() {
		return problems;
	}
}
