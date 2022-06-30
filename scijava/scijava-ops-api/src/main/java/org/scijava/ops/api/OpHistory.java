
package org.scijava.ops.api;

import java.util.List;

/**
 * Log describing each execution of an Op. This class is designed to answer two
 * questions:
 * <ol>
 * <li>What Op(s) produced and/or mutated this output?
 * <li>Given an {@link Object} op, what {@link OpInfo}s (including dependencies)
 * were utilized to implement that Op's functionality?
 * </ol>
 * The answers to these two questions allow users to produce an entire
 * {@code List<Graph<OpInfo>>}, containing all of the information needed to
 * reproduce any {@link Object} output.
 * <p>
 * Note that SciJava Ops is responsible for logging the returns to <b>any</b>
 * matching calls here, but with some effort the user or other applications
 * could also contribute their algorithms to the history.
 *
 * @author Gabe Selzer
 */
public interface OpHistory {

	// -- USER API -- //

	/**
	 * Describes the known executions upon {@link Object} {@code o} recorded in
	 * the history
	 * 
	 * @param o the {@link Object} of interest
	 * @return a {@link List} of all executions upon {@code o}
	 */
	List<RichOp<?>> executionsUpon(Object o);

	/**
	 * Returns the hierarchy of {@link OpInfo}s describing the dependency chain of
	 * the {@link Object} {@code op}.
	 * 
	 * @param op the {@Obect} returned by a matching call. NB {@code op}
	 *          <b>must</b> be the {@link Object} returned by the outermost
	 *          matching call, as the dependency {@link Object}s are not recorded.
	 * @return the {@link InfoChain} describing the dependency chain
	 */
	InfoChain opExecutionChain(Object op);

	default String signatureOf(Object op) {
		return opExecutionChain(op).signature();
	}

	// -- HISTORY MAINTENANCE API -- //

	/**
	 * Logs the creation of {@link RichOp}
	 * 
	 * @param op the {@link RichOp} containing relevant information
	 */
	void logOp(RichOp<?> op);

	/**
	 * Logs the {@link Object} output of the {@link RichOp} {@code op}.
	 * @param op the {@link RichOp} producing {@code output}
	 * @param output the {@link Object} output of {@code e}
	 */
	void logOutput(RichOp<?> op, Object output);

}
