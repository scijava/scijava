
package org.scijava.ops.provenance;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.scijava.ops.OpInfo;

/**
 * Log describing each execution of an Op.
 *
 * @author Gabe Selzer
 */
public class OpHistory {

	private static final Queue<OpExecutionSummary> history =
		new ConcurrentLinkedQueue<>();

	/**
	 * Logs a {@link OpExecutionSummary}
	 * 
	 * @param e the {@link OpExecutionSummary}
	 * @return true iff {@code e} was successfully logged
	 */
	public static boolean addExecution(OpExecutionSummary e) {
		return history.add(e);
	}

	/**
	 * Parses all executions of {@link OpInfo} {@code info} from the history
	 * 
	 * @param info the {@link OpInfo} of interest
	 * @return a {@link List} of all executions of {@code info}
	 */
	public static List<OpExecutionSummary> executionsOf(OpInfo info) {
		return history.stream() //
			.filter(e -> e.executor().getInfo().equals(info)) //
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
			.filter(e -> e.executor().getOp().equals(op)) //
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
		return history.stream() //
			.filter(e -> e.isInput(o) || e.isOutput(o)) //
			.collect(Collectors.toList());
	}

}
