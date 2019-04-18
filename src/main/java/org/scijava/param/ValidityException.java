package org.scijava.param;

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
	
	@Override
	public String getMessage() {
		String message = super.getMessage();
		if (message == null) {
			message = "";
		} else {
			message += "\n";
		}
		for (ValidityProblem p : problems) {
			message += "* ";
			message += p.getMessage();
			message += "\n";
		}
		return message;
	}
}
