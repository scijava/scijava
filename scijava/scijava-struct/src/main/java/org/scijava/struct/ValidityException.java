package org.scijava.struct;

import java.util.Collections;
import java.util.List;

import org.scijava.ValidityProblem;

/**
 * hasdjf
 *
 * TODO: move upstream with {@link ValidityProblem} 
 * @author Curtis Rueden
 */
public final class ValidityException extends Exception {

	private final List<ValidityProblem> problems;

	public ValidityException(final String message) {
		this(Collections.singletonList(new ValidityProblem(message)));
	}

	public ValidityException(@SuppressWarnings("exports") final List<ValidityProblem> problems) {
		this.problems = problems;
	}

	@SuppressWarnings("exports")
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
