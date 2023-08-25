
package org.scijava.ops.api;

/**
 * API-level hints to store in a {@link Hints} object.
 *
 * @author Gabriel Selzer
 */
public class APIHints {

	/**
	 * This hint can be used to pause the recording of Op executions within
	 * the {@link OpHistory}
	 */
	public static class History {

		public static final String PREFIX = "history";
		public static final String SKIP_RECORDING = PREFIX + "SKIP_RECORDING";
	}

}
