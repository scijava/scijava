/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.scijava.ops;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpClassInfo;
import org.scijava.ops.matcher.OpFieldInfo;
import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.matcher.OpTypeMatchingService;
import org.scijava.ops.transform.OpRunner;
import org.scijava.ops.transform.OpTransformationCandidate;
import org.scijava.ops.transform.OpTransformationException;
import org.scijava.ops.transform.OpTransformerService;
import org.scijava.ops.types.Nil;
import org.scijava.ops.types.TypeService;
import org.scijava.param.FunctionalMethodType;
import org.scijava.param.ParameterStructs;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;
import org.scijava.struct.ItemIO;
import org.scijava.util.ClassUtils;
import org.scijava.util.Types;

/**
 * Service to provide a list of available ops structured in a prefix tree and to
 * search for ops matching specified types.
 *
 * @author David Kolb
 */
@Plugin(type = Service.class)
public class OpService extends AbstractService implements SciJavaService, OpEnvironment {

	@Parameter
	private PluginService pluginService;

	@Parameter
	private OpTypeMatchingService matcher;

	@Parameter
	private LogService log;

	@Parameter
	private OpTransformerService transformer;

	@Parameter
	private TypeService typeService;

	/**
	 * Prefix tree to cache and quickly find {@link OpInfo}s.
	 */
	private PrefixTree<OpInfo> opCache;

	/**
	 * Map to collect all aliases for a specific op. All aliases will map to one
	 * canonical name of the op which is defined as the first one.
	 */
	private Map<String, String> opAliases = new HashMap<>();

	public void initOpCache() {
		opCache = new PrefixTree<>();

		// Add regular Ops
		for (final PluginInfo<Op> pluginInfo : pluginService.getPluginsOfType(Op.class)) {
			try {
				final Class<? extends Op> opClass = pluginInfo.loadClass();
				OpInfo opInfo = new OpClassInfo(opClass);
				addToCache(opInfo, pluginInfo.getName());

			} catch (InstantiableException exc) {
				log.error("Can't load class from plugin info: " + pluginInfo.toString(), exc);
			}
		}
		// Add Ops contained in an OpCollection
		for (final PluginInfo<OpCollection> pluginInfo : pluginService.getPluginsOfType(OpCollection.class)) {
			try {
				final List<Field> fields = ClassUtils.getAnnotatedFields(pluginInfo.loadClass(), OpField.class);
				for (Field field : fields) {
					OpInfo opInfo = new OpFieldInfo(field);
					addToCache(opInfo, field.getAnnotation(OpField.class).names());
				}
			} catch (InstantiableException exc) {
				log.error("Can't load class from plugin info: " + pluginInfo.toString(), exc);
			}
		}
	}

	private void addToCache(OpInfo opInfo, String opNames) {
		String[] parsedOpNames = OpUtils.parseOpNames(opNames);
		if (parsedOpNames == null || parsedOpNames.length == 0) {
			log.error("Skipping Op " + opInfo.implementationName() + ":\n" + "Op implementation must provide name.");
			return;
		}
		if (!opInfo.isValid()) {
			log.error("Skipping invalid Op " + opInfo.implementationName() + ":\n"
					+ opInfo.getValidityException().getMessage());
			return;
		}
		addAliases(parsedOpNames, opInfo.implementationName());
		opCache.add(new PrefixQuery(parsedOpNames[0]), opInfo);
	}

	@Override
	public Iterable<OpInfo> infos() {
		if (opCache == null) {
			initOpCache();
		}
		return opCache.getAndBelow(PrefixQuery.all());
	}

	@Override
	public Iterable<OpInfo> infos(String name) {
		if (opCache == null) {
			initOpCache();
		}
		if (name == null || name.isEmpty()) {
			return infos();
		}
		String opName = opAliases.get(OpUtils.getCanonicalOpName(name));
		if (opName == null) {
			throw new IllegalArgumentException("No op infos with name: " + name + " available.");
		}
		return opCache.getAndBelow(new PrefixQuery(opName));
	}

	@Override
	public LogService logger() {
		return log;
	}

	/**
	 * Attempts to inject {@link OpDependency} annotated fields of the specified
	 * object by looking for Ops matching the field type and the name specified in
	 * the annotation. The field type is assumed to be functional.
	 *
	 * @param op
	 * @throws OpMatchingException
	 *             if the type of the specified object is not functional, if the Op
	 *             matching the functional type and the name could not be found, if
	 *             an exception occurs during injection
	 */
	private void resolveOpDependencies(Object op, OpCandidate parentOp) throws OpMatchingException {
		// HACK: Only works with Op instances and OpRunner, not extensible.
		// Consider extensible ways to achieve something similar e.g. extending OpInfo
		// to support OpDependencies.
		if (op instanceof OpRunner) {
			op = ((OpRunner<?>) op).getAdaptedOp();
		}
		final List<OpDependencyMember<?>> dependencies = parentOp.opInfo()
			.dependencies();
		for (final OpDependencyMember<?> dependency : dependencies) {
			final String dependencyName = dependency.getDependencyName();
			final Type mappedDependencyType = Types.mapVarToTypes(new Type[] {
				dependency.getType() }, parentOp.typeVarAssigns())[0];
			final OpRef inferredRef = inferOpRef(mappedDependencyType, dependencyName,
				parentOp.typeVarAssigns());
			if (inferredRef == null) {
				throw new OpMatchingException("Could not infer functional " +
					"method inputs and outputs of Op dependency field: " + dependency
						.getKey());
			}
			Object matchedOp = null;
			try {
				matchedOp = findOpInstance(dependencyName, inferredRef);
			}
			catch (final Exception e) {
				throw new OpMatchingException(
					"Could not find Op that matches requested Op dependency field:" +
						"\nOp class: " + op.getClass().getName() + //
						"\nDependency field: " + dependency.getKey() + //
						"\n\n Attempted request:\n" + inferredRef, e);
			}
			try {
				dependency.createInstance(op).set(matchedOp);
			}
			catch (final Exception e) {
				throw new OpMatchingException(
					"Exception trying to inject Op dependency field.\n" +
						"\tOp dependency field to resolve: " + dependency.getKey() + "\n" +
						"\tFound Op to inject: " + matchedOp.getClass().getName() + "\n" +
						"\tWith inferred OpRef: " + inferredRef, e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T findOpInstance(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?> outType, final Object... secondaryArgs) {
		final OpRef ref = OpRef.fromTypes(opName, toTypes(specialType), outType != null ? outType.getType() : null, toTypes(
			inTypes));
		return (T) findOpInstance(opName, ref, secondaryArgs);
	}

	public Object findOpInstance(final String opName, final OpRef ref, final Object... secondaryArgs) {
		Object op = null;
		OpCandidate match = null;
		OpTransformationCandidate transformation = null;
		try {
			// Find single match which matches the specified types
			match = matcher.findSingleMatch(this, ref);
			op = match.createOp(secondaryArgs);
		} catch (OpMatchingException e) {
			log.debug("No matching Op for request: " + ref + "\n");
			log.debug("Attempting Op transformation...");

			// If we can't find an op matching the original request, we try to find a
			// transformation
			transformation = transformer.findTransfromation(this, ref);
			if (transformation == null) {
				log.debug("No matching Op transformation found");
				throw new IllegalArgumentException(e);
			}

			// If we found one, try to do transformation and return transformed op
			log.debug("Matching Op transformation found:\n" + transformation + "\n");
			try {
				op = transformation.exceute(this, secondaryArgs);
			} catch (OpMatchingException | OpTransformationException e1) {
				throw new IllegalArgumentException("Execution of Op transformatioon failed:\n" + e1);
			}
		}
		try {
			// Try to resolve annotated OpDependency fields
			if (match != null)
				resolveOpDependencies(op, match);
			else if (transformation != null)
				resolveOpDependencies(op, transformation.getSourceOp());
		} catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
		return op;
	}

	public <T> T findOp(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType,
			final Object... secondaryArgs) {
		return findOpInstance(opName, specialType, inTypes, outType, secondaryArgs);
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType()).toArray(Type[]::new);
	}

	public Object run(final String opName, final Object... args) {

		Nil<?>[] inTypes = Arrays.stream(args).map(arg -> Nil.of(typeService.reify(arg))).toArray(Nil[]::new);
		Nil<?> outType = new Nil<Object>() {};

		OpRunner<Object> op = findOpInstance(opName, new Nil<OpRunner<Object>>() {
		}, inTypes, outType);

		// TODO change
		return op.run(args);
	}

	/**
	 * Tries to infer a {@link OpRef} from a functional Op type. E.g. the type:
	 * 
	 * <pre>
	 * Computer&lt;Double[], Double[]&gt
	 * </pre>
	 * 
	 * Will result in the following {@link OpRef}:
	 * 
	 * <pre>
	 * Name: 'specified name'
	 * Types:       [Computer&lt;Double, Double&gt]
	 * InputTypes:  [Double[], Double[]]
	 * OutputTypes: [Double[]]
	 * </pre>
	 * 
	 * Input and output types will be inferred by looking at the signature of the
	 * functional method of the specified type. Also see
	 * {@link ParameterStructs#getFunctionalMethodTypes(Type)}.
	 *
	 * @param type
	 * @param name
	 * @return null if the specified type has no functional method
	 */
	private OpRef inferOpRef(Type type, String name, Map<TypeVariable<?>, Type> typeVarAssigns)
		throws OpMatchingException
	{
		List<FunctionalMethodType> fmts = ParameterStructs.getFunctionalMethodTypes(type);
		if (fmts == null)
			return null;

		EnumSet<ItemIO> inIos = EnumSet.of(ItemIO.BOTH, ItemIO.INPUT);
		EnumSet<ItemIO> outIos = EnumSet.of(ItemIO.BOTH, ItemIO.OUTPUT);

		Type[] inputs = fmts.stream().filter(fmt -> inIos.contains(fmt.itemIO())).map(fmt -> fmt.type())
				.toArray(Type[]::new);

		Type[] outputs = fmts.stream().filter(fmt -> outIos.contains(fmt.itemIO())).map(fmt -> fmt.type())
				.toArray(Type[]::new);

		Type[] mappedInputs = Types.mapVarToTypes(inputs, typeVarAssigns);
		Type[] mappedOutputs = Types.mapVarToTypes(outputs, typeVarAssigns);

		final int numOutputs = mappedOutputs.length;
		if (numOutputs != 1) {
			String error = "Op '" + name + "' of type " + type + " specifies ";
			error += numOutputs == 0 //
				? "no outputs" //
				: "multiple outputs: " + Arrays.toString(outputs);
			error += ". This is not supported.";
			throw new OpMatchingException(error);
		}
		return new OpRef(name, new Type[] { type }, mappedOutputs[0], mappedInputs);
	}

	/**
	 * Updates alias map using the specified String list. The first String in the
	 * list is assumed to be the canonical name of the op. After this method
	 * returns, all String in the specified list will map to the canonical name.
	 *
	 * @param opNames
	 */
	private void addAliases(String[] opNames, String opImpl) {
		String opName = opNames[0];
		for (String alias : opNames) {
			if (alias == null || alias.isEmpty()) {
				continue;
			}
			if (opAliases.containsKey(alias)) {
				if (!opAliases.get(alias).equals(opName)) {
					log.warn("Possible naming clash for op '" + opImpl + "' detected. Attempting to add alias '" + alias
							+ "' for op name '" + opName + "'. However the alias '" + alias + "' is already "
							+ "associated with op name '" + opAliases.get(alias) + "'.");
				}
				continue;
			}
			opAliases.put(alias, opName);
		}
	}

	/**
	 * Constructs a string with the specified number of tabs '\t'.
	 *
	 * @param numOfTabs
	 * @return
	 */
	private static String getIndent(int numOfTabs) {
		String str = "";
		for (int i = 0; i < numOfTabs; i++) {
			str += "\t";
		}
		return str;
	}

	/**
	 * Class to represent a query for a {@link PrefixTree}. Prefixes must be
	 * separated by dots ('.'). E.g. 'math.add'. These queries are used in order to
	 * specify the level where elements should be inserted into or retrieved from
	 * the tree.
	 */
	private static class PrefixQuery {
		String cachedToString;

		LinkedList<String> list = new LinkedList<>();

		public static PrefixQuery all() {
			return new PrefixQuery("");
		}

		/**
		 * Construct a new query from the specified string. Prefixes must be separated
		 * by dots.
		 *
		 * @param query
		 *            the string to use as query
		 */
		public PrefixQuery(String query) {
			if (query == null || query.isEmpty()) {
				return;
			}
			for (String s : query.split("\\.")) {
				list.add(s);
			}
			cachedToString = string();
		}

		/**
		 * Remove and return the first prefix.
		 *
		 * @return
		 */
		public String pop() {
			return list.removeFirst();
		}

		/**
		 * Whether there are more prefixes.
		 *
		 * @return
		 */
		public boolean hasNext() {
			return !list.isEmpty();
		}

		private String string() {
			int i = 1;
			String toString = "root \u00AC \n";
			for (String s : list) {
				toString += getIndent(i);
				toString += s + " \u00AC \n";
				i++;
			}
			return toString.substring(0, toString.length() - 3);
		}

		@Override
		public String toString() {
			return cachedToString;
		}
	}

	/**
	 * Data structure to group elements which share common prefixes. E.g. adding the
	 * following elements:
	 *
	 * <pre>
	 *	Prefix:		Elem:
	 *	"math.add"	obj1
	 *	"math.add"	obj2
	 *	"math.sqrt"	obj3
	 *	"math"		obj4
	 *	""		obj5
	 * </pre>
	 *
	 * Will result in the following tree:
	 *
	 * <pre>
	 *               root [obj5]
	 *                     |
	 *                     |
	 *                math [obj4]
	 *               /           \
	 *              /             \
	 *      add [obj1, obj2]   sqrt [obj3]
	 * </pre>
	 *
	 * @author David Kolb
	 *
	 * @param <T>
	 */
	private static class PrefixTree<T> {
		private PrefixNode<T> root;

		public PrefixTree() {
			root = new PrefixNode<>();
		}

		/**
		 * Adds the specified element on the level represented by the specified query.
		 * This method is in O(#number of prefixes in query)
		 *
		 * @param query
		 * @param node
		 * @param data
		 */
		public void add(PrefixQuery query, T data) {
			add(query, root, data);
		}

		private void add(PrefixQuery query, PrefixNode<T> node, T data) {
			if (query.hasNext()) {
				String level = query.pop();
				PrefixNode<T> child = node.getChildOrCreate(level);
				add(query, child, data);
			} else {
				node.data.add(data);
			}
		}

		/**
		 * Collects all elements of the level specified by the query and below. E.g.
		 * using the query 'math' on the example tree from the javadoc of this class
		 * would return all elements contained in the tree except for 'obj5'. 'math.add'
		 * would only return 'obj1' and 'obj2'. This method returns an iterable over
		 * these elements in O(# number of all nodes below query). The number of nodes
		 * is the number of distinct prefixes below the specified query.
		 *
		 * @param query
		 * @return
		 */
		public Iterable<T> getAndBelow(PrefixQuery query) {
			PrefixNode<T> queryNode = findNode(query);
			LinkedLinkedLists list = new LinkedLinkedLists();
			collectAll(queryNode, list);
			return () -> list.iterator();
		}

		private PrefixNode<T> findNode(PrefixQuery query) {
			return findNode(query, root);
		}

		private PrefixNode<T> findNode(PrefixQuery query, PrefixNode<T> node) {
			if (query.hasNext()) {
				String level = query.pop();
				PrefixNode<T> child = node.getChild(level);
				if (child != null) {
					return findNode(query, child);
				}
			}
			return node;
		}

		private void collectAll(PrefixNode<T> node, LinkedLinkedLists list) {
			if (node.hasData()) {
				list.append(node.data);
			}
			for (PrefixNode<T> v : node.children.values()) {
				collectAll(v, list);
			}
		}

		/**
		 * Wrapper for {@link ArrayList}s providing O(1) concatenation of lists if only
		 * an iterator over theses lists is required. The order of lists will be
		 * retained. Added lists will be simply saved in a super LinkedList. If the
		 * iterator reaches the end of one list, it will switch to the next if
		 * available.
		 *
		 * @author David Kolb
		 */
		private class LinkedLinkedLists implements Iterable<T> {

			LinkedList<LinkedList<T>> lists = new LinkedList<>();

			long size = 0;

			private void append(LinkedList<T> list) {
				lists.add(list);
				size += list.size();
			}

			@Override
			public Iterator<T> iterator() {
				return new LinkedIterator();
			}

			private class LinkedIterator implements Iterator<T> {

				private Iterator<LinkedList<T>> listsIter = lists.iterator();
				private Iterator<T> currentIter;

				public LinkedIterator() {
					if (!lists.isEmpty()) {
						currentIter = listsIter.next().iterator();
					}
				}

				@Override
				public boolean hasNext() {
					if (currentIter == null) {
						return false;
					}
					// if there are still lists available, possibly switch
					// to the next one
					if (listsIter.hasNext()) {
						// if the current iterator still has elements we are
						// fine, if not
						// switch to the next one
						if (!currentIter.hasNext()) {
							currentIter = listsIter.next().iterator();
						}
					}
					return currentIter.hasNext();
				}

				@Override
				public T next() {
					return currentIter.next();
				}
			}

			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();
				int i = 0;
				for (LinkedList<T> l : lists) {
					sb.append(i + ".) ");
					sb.append(l.toString() + "\n");
					i++;
				}
				return sb.toString();
			}
		}

		private static class PrefixNode<T> {
			private LinkedList<T> data = new LinkedList<>();
			private Map<String, PrefixNode<T>> children = new HashMap<>();

			public PrefixNode<T> getChildOrCreate(String id) {
				if (children.containsKey(id)) {
					return children.get(id);
				} else {
					PrefixNode<T> n = new PrefixNode<>();
					children.put(id, n);
					return n;
				}
			}

			public PrefixNode<T> getChild(String id) {
				return children.get(id);
			}

			public boolean hasData() {
				return !data.isEmpty();
			}
		}

		private String nodeToString(PrefixNode<T> node, String nodeName, StringBuilder sb, int level) {
			if (node.children.isEmpty() && node.data.isEmpty()) {
				return "";
			}
			sb.append(getIndent(level) + "Node -> Name: [");
			sb.append(nodeName + "]\n");
			sb.append(getIndent(level) + "Data:\n");
			for (T t : node.data) {
				sb.append(getIndent(level) + "\t" + t.getClass().getSimpleName() + "\n");
			}
			if (!node.data.isEmpty()) {
			} else {
				sb.delete(sb.length() - 1, sb.length());
				sb.append(" <empty>\n");
			}

			sb.append(getIndent(level) + "Children:\n");

			for (Entry<String, PrefixNode<T>> e : node.children.entrySet()) {
				String sub = nodeToString(e.getValue(), e.getKey(), new StringBuilder(), level + 1);
				sb.append(sub + "\n");
			}
			if (!node.children.isEmpty()) {
			} else {
				sb.delete(sb.length() - 1, sb.length());
				sb.append(" <empty>\n");
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			return nodeToString(root, "root", new StringBuilder(), 0);
		}
	}
}
