
package org.scijava.ops.engine.impl;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.scijava.ops.api.OpExecutionSummary;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfo;
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
	 * {@link Map} responsible for recording the execution of an Op returned by a
	 * <b>matching call</b>
	 */
	private final Map<UUID, ConcurrentLinkedDeque<OpExecutionSummary>> history =
		new ConcurrentHashMap<>();

	/**
	 * {@link Map} responsible for recording the {@link Graph} of {@link OpInfo}s
	 * involved to produce the result of a particular matching call
	 */
	private final Map<UUID, MutableGraph<OpInfo>> dependencyChain =
		new ConcurrentHashMap<>();

	/**
	 * {@link Map} responsible for recording the "top-level" {@link Object}
	 * produced by a matching call
	 */
	private final Map<UUID, Object> opRecord = new ConcurrentHashMap<>();

	/**
	 * {@link Map} responsible for recording the <b>wrapper</b> of the "top-level"
	 * {@link Object} produced by a matching call
	 */
	private final Map<UUID, Object> wrapperRecord =
		new ConcurrentHashMap<>();

	// -- USER API -- //

	/**
	 * Returns the list of executions on {@link Object} {@code o} recorded in the
	 * history
	 * 
	 * @param o the {@link Object} of interest
	 * @return a {@link List} of all executions upon {@code o}
	 */
	@Override
	public List<OpExecutionSummary> executionsUpon(Object o) {
		if (o.getClass().isPrimitive()) throw new IllegalArgumentException(
			"Cannot determine the executions upon a primitive as they are passed by reference!");
		return history.values().stream() //
			.filter(deque -> deque.parallelStream().anyMatch(e -> e.isOutput(o))) //
			.flatMap(Deque::stream) //
			.collect(Collectors.toList());
	}

	/**
	 * Returns the {@link Graph} of {@link OpInfo}s describing the dependency
	 * chain of the {@link Object} {@code op}.
	 * 
	 * @param op the {@Obect} returned by a matching call. NB {@code op}
	 *          <b>must</b> be the {@link Object} returned by the outermost
	 *          matching call, as the dependency {@link Object}s are not recorded.
	 * @return the {@link Graph} describing the dependency chain
	 */
	@Override
	public Graph<OpInfo> opExecutionChain(Object op) {
		// return dependency chain if op was an Op or wrapped an Op
		UUID opID = findUUIDInOpRecord(op);
		if (opID != null) return dependencyChain.get(opID);
		UUID wrapperID = findUUIDInWrapperRecord(op);
		if (wrapperID != null) return dependencyChain.get(wrapperID);

		// op not recorded - throw an exception
		throw new IllegalArgumentException(
			"No record of op being returned in a matching Execution!");
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
	 * Logs a {@link OpExecutionSummary} in the history
	 * 
	 * @param e the {@link OpExecutionSummary}
	 * @return true iff {@code e} was successfully logged
	 */
	@Override
	public boolean addExecution(OpExecutionSummary e) {
		UUID id = e.op().metadata().executionID();
		if (!history.containsKey(id)) generateDeque(id);
		history.get(id).addLast(e);
		return true;
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

	/**
	 * Logs the "top-level" Op for a particular matching call. {@code op} is the
	 * {@link Object} returned to the user (save for Op wrapping)
	 * 
	 * @param executionChainID the {@link UUID} identifying a particular matching
	 *          call
	 * @param op the {@link Object} returned from the matching call identifiable
	 *          by {@code executionChainID}.
	 */
	@Override
	public void logTopLevelOp(UUID executionChainID, Object op) {
		Object former = opRecord.putIfAbsent(executionChainID, op);
		if (former != null) throw new IllegalArgumentException("Execution ID " +
			executionChainID + " has already logged a Top-Level Op!");
	}

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
	@Override
	public void logTopLevelWrapper(UUID executionChainID, Object wrapper) {
		Object former = wrapperRecord.putIfAbsent(executionChainID, wrapper);
		if (former != null) throw new IllegalArgumentException("Execution ID " +
			executionChainID + " has already logged a Top-Level Op!");
	}

	// -- HELPER METHODS -- //

	private MutableGraph<OpInfo> buildGraph() {
		return GraphBuilder.directed().allowsSelfLoops(false).build();
	}

	private UUID findUUIDInOpRecord(Object op) {
		return keyForV(opRecord, op);
	}

	private UUID findUUIDInWrapperRecord(Object op) {
		return keyForV(wrapperRecord, op);
	}

	private synchronized void generateDeque(UUID executionTreeHash) {
		history.putIfAbsent(executionTreeHash,
			new ConcurrentLinkedDeque<OpExecutionSummary>());
	}

	private <T, V> T keyForV(Map<T, V> map, V value) {
		List<T> keys = keysForV(map, value);
		if (keys.size() > 1) throw new IllegalArgumentException("Map " + map +
			" has multiple keys for value " + value);
		if (keys.size() == 0) return null;
		return keys.get(0);
	}

	private <T, V> List<T> keysForV(Map<T, V> map, V value) {
		return map.entrySet().parallelStream() //
			.filter(e -> e.getValue() == value) //
			.map(e -> e.getKey()) //
			.collect(Collectors.toList());
	}

	public void resetHistory() {
		history.clear();
		dependencyChain.clear();
	}

}
