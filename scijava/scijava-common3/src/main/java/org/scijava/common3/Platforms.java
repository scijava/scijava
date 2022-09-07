package org.scijava.common3;

/**
 * Useful methods for platform-specific interrogation.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public final class Platforms {

	private Platforms() {
		// prevent instantiation of utility class
	}

	/** Whether the operating system is Windows-based. */
	public static boolean isWindows() {
		return osName().startsWith("Win");
	}

	/** Whether the operating system is Mac-based. */
	public static boolean isMac() {
		return osName().startsWith("Mac");
	}

	/** Whether the operating system is Linux-based. */
	public static boolean isLinux() {
		return osName().startsWith("Linux");
	}

	/** Whether the operating system is POSIX compliant. */
	public static boolean isPOSIX() {
		return isMac() || isLinux();
	}

	/** Gets the name of the operating system. */
	public static String osName() {
		final String osName = System.getProperty("os.name");
		return osName == null ? "Unknown" : osName;
	}

}
