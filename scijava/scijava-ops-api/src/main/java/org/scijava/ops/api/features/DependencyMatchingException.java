
package org.scijava.ops.api.features;

import org.scijava.ops.api.OpRef;

/**
 * An {@link OpMatchingException} caused by another {@code OpMatchingException}
 * thrown when resolving dependencies. We illustrate the need for
 * {@code DependencyMatchingException} with an example: Suppose Op {@code A} has
 * an dependency on Op {@code B}, which in turn has a dependency on Op
 * {@code C}. Suppose further that {@code A} and {@code B} are found, but no
 * {@code C} is found to satisfy {@code B}. This will result in an
 * {@link OpMatchingException} thrown for Op {@code A}. We would want to know:
 * <ol>
 * <li>that the request for {@code A} was not fulfilled
 * <li>that no match could be found for {@code C}, which was the cause for (1)
 * </ol>
 * This logic can be generalized for an arbitrarily long Op chain.
 *
 * TODO: Consider supporting non-DependencyMatchingException {@code cause}s.
 *
 * @author Gabriel Selzer
 */
public class DependencyMatchingException extends OpMatchingException {

	public DependencyMatchingException(final String message) {
		super(message);
	}

	public static String message(String dependentOp, final String dependencyName, OpRef dependency) {
		return "Could not instantiate dependency of: " + dependentOp + 
				"\nDependency identifier: " + dependencyName + //
				"\n\nAttempted request:\n" + dependency;
	}

	public DependencyMatchingException(final String message,
		final DependencyMatchingException cause)
	{
		super(message, cause);
	}

	@Override
	public String getMessage() {
		if (getCause() == null) return super.getMessage();
		return super.getMessage() + "\n\nCause:\n\n" +
			getCause().getMessage();
	}
}
