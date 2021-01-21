package org.scijava.ops.hints;

import java.lang.annotation.Annotation;

/**
 * A set of common hints. Ideally these would be {@link Enum}s, but
 * unfortunately {@link Annotation}s cannot handle arrays of Enums :(
 * 
 * @author Gabriel Selzer
 */
public class BaseOpHints {

	public static class Simplification {
		public static final String prefix = "simplification";
		public static final String ALLOWED = prefix + ".ALLOWED";
		public static final String FORBIDDEN = prefix + ".FORBIDDEN";
		public static final String IN_PROGRESS = prefix + ".IN_PROGRESS";
	}

	public static class Adaptation {
		public static final String prefix = "adaptation";
		public static final String ALLOWED = prefix + ".ALLOWED";
		public static final String FORBIDDEN = prefix + ".FORBIDDEN";
		public static final String IN_PROGRESS = prefix + ".IN_PROGRESS";
	}

}
