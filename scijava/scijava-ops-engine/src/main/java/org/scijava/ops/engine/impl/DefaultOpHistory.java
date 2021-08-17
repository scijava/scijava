
package org.scijava.ops.engine.impl;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.spi.OpDependency;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

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
@Plugin(type = Service.class)
public class DefaultOpHistory extends AbstractService implements OpHistory {

	// -- DATA STRCUTURES -- //

	/**
	 * {@link Map} responsible for recording the {@link Graph} of {@link OpInfo}s
	 * involved to produce the result of a particular matching call
	 */
	private final Map<UUID, MutableGraph<OpInfo>> dependencyChain =
		new ConcurrentHashMap<>();

	private final Map<Object, ConcurrentLinkedDeque<UUID>> mutationMap =
		new WeakHashMap<>();

	private final Map<RichOp<?>, UUID> mutatorMap = new WeakHashMap<>();

	// -- USER API -- //

	/**
	 * Returns the list of executions on {@link Object} {@code o} recorded in the
	 * history
	 * <p>
	 * The list of executions is described by a {@link UUID}, which points to a
	 * particular Op execution chain.
	 * 
	 * @param o the {@link Object} of interest
	 * @return an {@link Iterable} of all executions upon {@code o}
	 */
	@Override
	public ArrayList<UUID> executionsUpon(Object o) {
		if (o.getClass().isPrimitive()) throw new IllegalArgumentException(
			"Cannot determine the executions upon a primitive as they are passed by reference!");
		return new ArrayList<>(mutationMap.get(o));
	}

	@Override
	public Graph<OpInfo> opExecutionChain(Object op) {
		return dependencyChain.get(mutatorMap.get(op));
	}

	/**
	 * Returns the {@link Graph} of {@link OpInfo}s describing the dependency
	 * chain of the Op call fufilled with {@link UUID} {@code id}
	 * 
	 * @param id the {@link UUID} associated with a particular matching call
	 * @return the {@link Graph} describing the dependency chain
	 */
	@Override
	public Graph<OpInfo> opExecutionChain(UUID id) {
		if (!dependencyChain.containsKey(id)) throw new IllegalArgumentException(
			"UUID " + id + " has not been registered to an Op execution chain");
		return dependencyChain.get(id);
	}

	// -- HISTORY MAINTENANCE API -- //

	/**
	 * Logs an Op execution in the history
	 * <p>
	 * TODO: It would be nice if different Objects returned different Objects with
	 * the same hash code would hash differently. For example, if two Ops return a
	 * {@link Double} of the same value, they will appear as the same Object, and
	 * asking for the execution history on either of the {@link Object}s will
	 * suggest that both executions mutated both {@link Object}s. This would
	 * really hamper the simplicity of the implementation, however.
	 * 
	 * @param op the {@link RichOp} being executed
	 * @param output the output of the Op execution
	 * @return true iff {@code e} was successfully logged
	 */
	@Override
	public boolean addExecution(RichOp<?> op, Object output) {
		if (!mutationMap.containsKey(output)) generateDeque(output);
		mutationMap.get(output).addLast(op.metadata().executionID());
		return true;
	}

	@Override
	public void logTopLevelOp(RichOp<?> op, UUID executionChainID) {
		mutatorMap.put(op, executionChainID);
	}

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
	@Override
	public void logDependencies(UUID executionChainID, OpInfo info,
		List<OpInfo> dependencies)
	{
		dependencyChain.putIfAbsent(executionChainID, buildGraph());
		MutableGraph<OpInfo> depTree = dependencyChain.get(executionChainID);
		depTree.addNode(info);
		dependencies.forEach(i -> {
			depTree.addNode(i);
			depTree.putEdge(info, i);
		});
	}

	// -- HELPER METHODS -- //

	private MutableGraph<OpInfo> buildGraph() {
		return GraphBuilder.directed().allowsSelfLoops(false).build();
	}

	private synchronized void generateDeque(Object output) {
		mutationMap.putIfAbsent(output, new ConcurrentLinkedDeque<UUID>());
	}

	public void resetHistory() {
		mutationMap.clear();
		dependencyChain.clear();
	}

}
