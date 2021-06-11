
package org.scijava.ops.provenance;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.scijava.ops.OpInfo;

/**
 * Log describing each execution of an Op.
 *
 * @author Gabe Selzer
 */
public class OpHistory {

	private static final Deque<OpExecutionSummary> history =
		new ConcurrentLinkedDeque<>();

	/**
	 * Logs a {@link OpExecutionSummary}
	 * 
	 * @param e the {@link OpExecutionSummary}
	 * @return true iff {@code e} was successfully logged
	 */
	public static boolean addExecution(OpExecutionSummary e) {
		history.addLast(e);
		return true;
	}

	/**
	 * Parses all executions of {@link OpInfo} {@code info} from the history
	 * 
	 * @param info the {@link OpInfo} of interest
	 * @return a {@link List} of all executions of {@code info}
	 */
	public static List<OpExecutionSummary> executionsOf(OpInfo info) {
		return history.stream() //
			.filter(e -> e.info().equals(info)) //
			.collect(Collectors.toList());
	}

	/**
	 * Parses all executions of {@link Object} {@code op} from the history
	 * 
	 * @param op the {@link Object} of interest
	 * @return a {@link List} of all executions of {@code op}
	 */
	public static List<OpExecutionSummary> executionsOf(Object op) {
		return history.stream() //
			.filter(e -> e.executor().equals(op)) //
			.collect(Collectors.toList());
	}

	/**
	 * Parses all executions that operated on {@link Object} {@code o} from the
	 * history
	 * 
	 * @param o the {@link Object} of interest
	 * @return a {@link List} of all executions upon {@code o}
	 */
	public static List<OpExecutionSummary> executionsUpon(Object o) {
		if (o.getClass().isPrimitive()) throw new IllegalArgumentException(
			"Cannot determine the executions upon a primitive as they are passed by reference!");
		return history.stream() //
			.filter(e -> e.isOutput(o)) //
			.collect(Collectors.toList());
	}

	public static void resetHistory() {
		history.clear();
	}

}
