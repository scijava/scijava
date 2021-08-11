package org.scijava.ops.engine;

import java.lang.annotation.Annotation;

/**
 * A set of common hints. Ideally these would be {@link Enum}s, but
 * unfortunately {@link Annotation}s cannot handle arrays of Enums :(
 * 
 * @author Gabriel Selzer
 */
public class BaseOpHints {

	public static class Simplification {
		public static final String PREFIX = "simplification";
		public static final String FORBIDDEN = PREFIX + ".FORBIDDEN";
		public static final String IN_PROGRESS = PREFIX + ".IN_PROGRESS";
	}

	public static class Adaptation {
		public static final String PREFIX = "adaptation";
		public static final String FORBIDDEN = PREFIX + ".FORBIDDEN";
		public static final String IN_PROGRESS = PREFIX + ".IN_PROGRESS";
	}

	// TODO: better naming
	public static class DependencyMatching {
		public static final String PREFIX = "dependencyMatching";
		public static final String IN_PROGRESS = PREFIX + ".IN_PROGRESS";
		
	}

}
