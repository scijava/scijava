
package org.scijava.ops.api;

import com.google.common.graph.Graph;

import java.util.List;
import java.util.UUID;

/**
 * Log describing each execution of an Op. This class is designed to answer two
 * questions:
 * <ol>
 * <li>Given an {@link Object} output (e.g. a {@code List<String>}), what Op(s)
 * mutated that output?
 * <li>Given an {@link Object} op, what {@link OpInfo}s were utilized to
 * implement that Op's functionality?
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
	 * Returns the list of executions on {@link Object} {@code o} recorded in the
	 * history
	 * 
	 * @param o the {@link Object} of interest
	 * @return a {@link List} of all executions upon {@code o}
	 */
	public List<OpExecutionSummary> executionsUpon(Object o);

	/**
	 * Returns the {@link Graph} of {@link OpInfo}s describing the dependency
	 * chain of the {@link Object} {@code op}.
	 * 
	 * @param op the {@Obect} returned by a matching call. NB {@code op}
	 *          <b>must</b> be the {@link Object} returned by the outermost
	 *          matching call, as the dependency {@link Object}s are not recorded.
	 * @return the {@link Graph} describing the dependency chain
	 */
	public Graph<OpInfo> opExecutionChain(Object op);

	/**
	 * Returns the {@link Graph} of {@link OpInfo}s describing the dependency
	 * chain of the Op call fufilled with {@link UUID} {@code id}
	 * 
	 * @param id the {@link UUID} associated with a particular matching call
	 * @return the {@link Graph} describing the dependency chain
	 */
	public Graph<OpInfo> opExecutionChain(UUID id);

	// -- HISTORY MAINTENANCE API -- //

	/**
	 * Logs a {@link OpExecutionSummary} in the history
	 * 
	 * @param e the {@link OpExecutionSummary}
	 * @return true iff {@code e} was successfully logged
	 */
	public boolean addExecution(OpExecutionSummary e);

	/**
	 * Logs the {@link List} of {@link OpInfo} dependencies under the
	 * {@link OpInfo} {@code info}
	 * 
	 * @param executionChainID the {@link UUID} identifying a particular matching
	 *          call.
	 * @param info the {@link OpInfo} depending on {@code dependencies}
	 * @param dependencies the {@link OpInfo}s used to fulfill the
	 *          {@link OpDependency} requests of the Op specified by {@code info}
	 */
	public void logDependencies(UUID executionChainID, OpInfo info,
		List<OpInfo> dependencies);

	/**
	 * Logs the "top-level" Op for a particular matching call. {@code op} is the
	 * {@link Object} returned to the user (save for Op wrapping)
	 * 
	 * @param executionChainID the {@link UUID} identifying a particular matching
	 *          call
	 * @param op the {@link Object} returned from the matching call identifiable
	 *          by {@code executionChainID}.
	 */
	public void logTopLevelOp(UUID executionChainID, Object op);

	/**
	 * Logs the <b>wrapper</b> of the "top-level" Op for a particular matching
	 * call. {@code wrapper} is the {@link Object} returned to the user when Op
	 * wrapping is perfomed
	 * 
	 * @param executionChainID the {@link UUID} identifying a particular matching
	 *          call
	 * @param wrapper the {@link Object} returned from the matching call
	 *          identifiable by {@code executionChainID}.
	 */
	public void logTopLevelWrapper(UUID executionChainID, Object wrapper);

}
