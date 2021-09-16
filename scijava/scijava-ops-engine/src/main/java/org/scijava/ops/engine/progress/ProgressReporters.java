
package org.scijava.ops.engine.progress;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.scijava.ops.api.OpExecution;
import org.scijava.ops.api.ProgressReporter;

public final class ProgressReporters {

	private ProgressReporters() {}

	/**
	 * Each thread runs through a hierarchy of Ops, and each Op is interrupted as
	 * its dependencies are run. We cannot lose the track of that outer Op when
	 * keeping track of the inner Op; this is the reason to keep a
	 * {@link Deque} for each {@link Thread}.
	 */
	private static ThreadLocal<Deque<OpExecution>> metadata =
		new ThreadLocal<>()
		{

			@Override
			protected Deque<OpExecution> initialValue() {
				return new ArrayDeque<>();
			}
		};

	public static OpExecution get() {
		return metadata.get().peek();
	}

	public static OpExecution remove() {
		return metadata.get().pop();
	}

	public static void add(OpExecution p) {
		metadata.get().push(p);
	}

	public static void setReporter(ProgressReporter p) {
		get().setReporter(p);
	}

}
