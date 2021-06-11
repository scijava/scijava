
package org.scijava.ops.provenance;

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

import org.scijava.ops.OpInfo;

/**
 * Log describing each execution of an Op.
 *
 * @author Gabe Selzer
 */
public class OpHistory {

	private static final Map<UUID, ConcurrentLinkedDeque<OpExecutionSummary>> history =
		new ConcurrentHashMap<>();

	private static final Map<UUID, MutableGraph<OpInfo>> dependencyChain =
		new ConcurrentHashMap<>();
	private static final Map<UUID, Object> opRecord = new ConcurrentHashMap<>();
	private static final Map<UUID, Object> wrapperRecord = new ConcurrentHashMap<>();

	/**
	 * Logs a {@link OpExecutionSummary}
	 * 
	 * @param e the {@link OpExecutionSummary}
	 * @return true iff {@code e} was successfully logged
	 */
	public static boolean addExecution(OpExecutionSummary e) {
		if (!history.containsKey(e.executionTreeHash())) generateDeque(e.executionTreeHash());
		history.get(e.executionTreeHash()).addLast(e);
		return true;
	}

	private static synchronized void generateDeque(UUID executionTreeHash) {
		history.putIfAbsent(executionTreeHash, new ConcurrentLinkedDeque<OpExecutionSummary>());
	}

	/**
	 * Parses all executions of {@link OpInfo} {@code info} from the history
	 * 
	 * @param info the {@link OpInfo} of interest
	 * @return a {@link List} of all executions of {@code info}
	 */
	public static List<OpExecutionSummary> executionsOf(OpInfo info) {
		return history.values().stream() //
			.flatMap(Deque::stream) //
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
		return history.values().stream() //
			.flatMap(Deque::stream) //
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
		return history.values().stream() //
			.filter(deque -> deque.parallelStream().anyMatch(e -> e.isOutput(o))) //
			.flatMap(Deque::stream) //
			.collect(Collectors.toList());
	}

	public static Graph<OpInfo> opExecutionChain(Object op) {
		UUID opExecutionID = findUUIDInOpRecord(op);
		UUID wrapperExecutionID = findUUIDInWrapperRecord(op);
		if (opExecutionID != null && wrapperExecutionID != null) {
			throw new IllegalStateException("op is recorded in both the OpRecord and the WrapperRecord!");
		}
		if (opExecutionID == null && wrapperExecutionID == null) {
			throw new IllegalArgumentException("No record of op being returned in a matching Execution!");
		}
		UUID id = opExecutionID != null ? opExecutionID : wrapperExecutionID;
		return dependencyChain.get(id);
	}

	private static UUID findUUIDInOpRecord(Object op) {
		List<UUID> opExecutionIDs = opRecord.entrySet().parallelStream() //
				.filter(e -> e.getValue() == op) //
				.map(e -> e.getKey()) //
				.collect(Collectors.toList());
		if (opExecutionIDs.size() > 1) throw new IllegalArgumentException(
			"Multiple Op Execution chains found for Op " + op);
		if (opExecutionIDs.size() == 0) return null;
		return opExecutionIDs.get(0);
	}

	private static UUID findUUIDInWrapperRecord(Object op) {
		List<UUID> opExecutionIDs = wrapperRecord.entrySet().parallelStream() //
				.filter(e -> e.getValue() == op) //
				.map(e -> e.getKey()) //
				.collect(Collectors.toList());
		if (opExecutionIDs.size() > 1) throw new IllegalArgumentException(
			"Multiple Op Execution chains found for Op " + op);
		if (opExecutionIDs.size() == 0) return null;
		return opExecutionIDs.get(0);
	}

	public static void resetHistory() {
		history.clear();
		dependencyChain.clear();
	}

	public static void logDependencies(UUID executionChainID, OpInfo info,
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

	public static void logTopLevelOp(UUID executionChainID, Object op) {
		Object former = opRecord.putIfAbsent(executionChainID, op);
		if (former != null) throw new IllegalArgumentException("Execution ID " +
			executionChainID + " has already logged a Top-Level Op!");
	}

	public static void logTopLevelWrapper(UUID executionChainID, Object wrapper) {
		Object former = wrapperRecord.putIfAbsent(executionChainID, wrapper);
		if (former != null) throw new IllegalArgumentException("Execution ID " +
			executionChainID + " has already logged a Top-Level Op!");
	}

	private static MutableGraph<OpInfo> buildGraph() {
		return GraphBuilder.directed().allowsSelfLoops(false).build();
	}

}
