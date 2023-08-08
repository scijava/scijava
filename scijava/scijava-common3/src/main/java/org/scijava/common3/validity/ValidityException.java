package org.scijava.common3.validity;

import java.util.Collections;
import java.util.List;

/**
 * An {@link Exception} thrown once processing has been completed and
 * {@link ValidityProblem}s were generated.
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class ValidityException extends RuntimeException {

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
