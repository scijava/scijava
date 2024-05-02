/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine;

/**
 * A set of common hints useful for manipulating SciJava Ops Engine internals.
 * Their usage is not recommended unless necessary to e.g. prevent some Op from
 * matching.
 *
 * @author Gabriel Selzer
 */
public final class BaseOpHints {

	private BaseOpHints() {
		// Prevent instantiation of static utility class
	}

	public static final class Reduction {

		private Reduction() {
			// Prevent instantiation of static utility class
		}

		public static final String PREFIX = "reduction";
		public static final String FORBIDDEN = PREFIX + ".FORBIDDEN";
	}

	public static final class Conversion {

		private Conversion() {
			// Prevent instantiation of static utility class
		}

		public static final String PREFIX = "conversion";
		public static final String FORBIDDEN = PREFIX + ".FORBIDDEN";
		public static final String IN_PROGRESS = PREFIX + ".IN_PROGRESS";
	}

	public static final class Adaptation {

		private Adaptation() {
			// Prevent instantiation of static utility class
		}

		public static final String PREFIX = "adaptation";
		public static final String FORBIDDEN = PREFIX + ".FORBIDDEN";
		public static final String IN_PROGRESS = PREFIX + ".IN_PROGRESS";
	}

	// TODO: better naming
	public static final class DependencyMatching {

		private DependencyMatching() {
			// Prevent instantiation of static utility class
		}

		public static final String PREFIX = "dependencyMatching";
		// Used by an OpEnvironment to hint that dependency matching is underway
		public static final String IN_PROGRESS = PREFIX + ".IN_PROGRESS";
		// Used by an Op to hint that it does not wish to be a dependency of another
		// Op.
		public static final String FORBIDDEN = PREFIX + ".FORBIDDEN";

	}

	public static final class Progress {

		private Progress() {
			// Prevent instantiation of static utility class
		}

		public static final String PREFIX = "progress";
		public static final String TRACK = PREFIX + ".TRACK";

	}

	public static final class History {

		private History() {
			// Prevent instantiation of static utility class
		}

		public static final String PREFIX = "history";
		public static final String IGNORE = PREFIX + ".IGNORE";

	}

	public static final class Cache {

		private Cache() {
			// Prevent instantiation of static utility class
		}

		public static final String PREFIX = "cache";
		public static final String IGNORE = PREFIX + ".IGNORE";

	}

}
