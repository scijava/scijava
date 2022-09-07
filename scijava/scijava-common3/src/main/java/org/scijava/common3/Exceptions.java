package org.scijava.common3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Utility class for working with {@link Exception}s.
 * 
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Exceptions {

	private static final String NL = System.getProperty("line.separator");

	private Exceptions() {
		// prevent instantiation of utility class
	}

	/**
	 * Creates a new {@link IllegalArgumentException} with the given message
	 * strings joined by commas.
	 */
	public static IllegalArgumentException iae(final String... notes) {
		return iae(null, notes);
	}

	/**
	 * Creates a new {@link IllegalArgumentException} with the given cause and
	 * message strings joined by commas.
	 */
	public static IllegalArgumentException iae(final Throwable cause, final String... notes) {
		final String s = String.join(", ", notes);
		final IllegalArgumentException exc = new IllegalArgumentException(s);
		if (cause != null) exc.initCause(cause);
		return exc;
	}

	/** Extracts the given exception's corresponding stack trace to a string. */
	public static String stackTrace(final Throwable t) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.printStackTrace(new PrintStream(out, false, "UTF-8"));
			return new String(out.toByteArray(), "UTF-8");
		}
		catch (final IOException exc) {
			return null;
		}
	}

	/**
	 * Provides a stack dump of the given thread.
	 * <p>
	 * The output is similar to a subset of that given when Ctrl+\ (or Ctrl+Pause
	 * on Windows) is pressed from the console.
	 * </p>
	 */
	public static String stackTrace(final Thread thread) {
		final StringBuilder sb = new StringBuilder();
		dumpThread(thread, thread.getStackTrace(), sb);
		return sb.toString();
	}

	/**
	 * Provides a complete dump of all threads.
	 * <p>
	 * The output is similar to a subset of that given when Ctrl+\ (or Ctrl+Pause
	 * on Windows) is pressed from the console.
	 */
	public static String dumpAll() {
		final StringBuilder sb = new StringBuilder();

		final Map<Thread, StackTraceElement[]> stackTraces =
			Thread.getAllStackTraces();

		// sort list of threads by name
		final ArrayList<Thread> threads =
			new ArrayList<>(stackTraces.keySet());
		Collections.sort(threads, (t1, t2) -> t1.getName().compareTo(t2.getName()));

		for (final Thread t : threads) {
			dumpThread(t, stackTraces.get(t), sb);
		}

		return sb.toString();
	}

	// -- Helper methods --

	private static void dumpThread(final Thread t,
		final StackTraceElement[] trace, final StringBuilder sb)
	{
		threadInfo(t, sb);
		for (final StackTraceElement element : trace) {
			sb.append("\tat ");
			sb.append(element);
			sb.append(NL);
		}
		sb.append(NL);
	}

	private static void threadInfo(final Thread t, final StringBuilder sb) {
		sb.append("\"");
		sb.append(t.getName());
		sb.append("\"");
		if (!t.isAlive()) sb.append(" DEAD");
		if (t.isInterrupted()) sb.append(" INTERRUPTED");
		if (t.isDaemon()) sb.append(" daemon");
		sb.append(" prio=");
		sb.append(t.getPriority());
		sb.append(" id=");
		sb.append(t.getId());
		sb.append(" group=");
		sb.append(t.getThreadGroup().getName());
		sb.append(NL);
		sb.append("   java.lang.Thread.State: ");
		sb.append(t.getState());
		sb.append(NL);
	}
}
