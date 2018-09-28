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
import java.util.ArrayList;
import java.util.Arrays;
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
import org.scijava.ops.matcher.MatchingResult;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpClassInfo;
import org.scijava.ops.matcher.OpFieldInfo;
import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.matcher.OpTypeMatchingService;
import org.scijava.ops.util.Inject;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;
import org.scijava.struct.StructInstance;
import org.scijava.types.Nil;
import org.scijava.util.ClassUtils;

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
				final List<Field> fields = ClassUtils.getAnnotatedFields(pluginInfo.loadClass(),
						OpField.class);
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
		if (!opInfo.isValid()) {
			log.error("Skipping invalid Op " + opInfo.implementationName() + ":\n"
					+ opInfo.getValidityException().getMessage());
			return;
		}
		String[] parsedNames = OpUtils.parseOpNames(opNames);
		addAliases(parsedNames, opInfo.implementationName());
		opCache.add(new PrefixQuery(parsedNames[0]), opInfo);
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

	public <T> StructInstance<T> findOpInstance(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?>[] outTypes, final Object... secondaryArgs) {
		final OpRef ref = OpRef.fromTypes(opName, toTypes(specialType), toTypes(outTypes), toTypes(inTypes));

		// Find single match which matches the specified types
		OpCandidate match = findTypeMatch(ref);

		@SuppressWarnings("unchecked")
		final StructInstance<T> opInst = (StructInstance<T>) match.createOp();

		// Inject the secondary args if there are any
		if (Inject.Structs.isInjectable(opInst)) {
			// Get padded secondary args
			Object[] paddedArgs = OpUtils.padArgs(match, true, secondaryArgs);
			if (paddedArgs == null) {
				throw new IllegalStateException(match.opInfo().implementationName() + " | " + match.getStatus());
			}
			Inject.Structs.inputs(opInst, paddedArgs);
		// Secondary args are given, however there are no to inject
		} else if (secondaryArgs.length > 0) {
			log.warn(
					"Specified Op has no secondary args, however secondary args are given. "
					+ "The specified args will not be injected.");
		}
		return opInst;
	}

	public <T> T findOp(final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?>[] outTypes,
			final Object... secondaryArgs) {
		return findOpInstance(null, specialType, inTypes, outTypes, secondaryArgs).object();
	}

	public <T> T findOp(final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType,
			final Object... secondaryArgs) {
		return findOpInstance(null, specialType, inTypes, new Nil[] { outType }, secondaryArgs).object();
	}

	public <T> T findOp(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?>[] outTypes,
			final Object... secondaryArgs) {
		return findOpInstance(opName, specialType, inTypes, outTypes, secondaryArgs).object();
	}

	public <T> T findOp(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?> outType, final Object... secondaryArgs) {
		return findOpInstance(opName, specialType, inTypes, new Nil[] { outType }, secondaryArgs).object();
	}

	public MatchingResult findTypeMatches(final OpRef ref) {
		return matcher.findMatch(this, ref);
	}

	public OpCandidate findTypeMatch(final OpRef ref) {
		return findTypeMatches(ref).singleMatch();
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType()).toArray(Type[]::new);
	}

	/**
	 * Updates alias map using the specified String list. The first String in
	 * the list is assumed to be the canonical name of the op. After this method
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
					log.warn("Possible naming clash for op '" + opImpl + "' detected. Attempting to add alias '"
							+ alias + "' for op name '" + opName + "'. However the alias '" + alias + "' is already "
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
	 * separated by dots ('.'). E.g. 'math.add'. These queries are used in order
	 * to specify the level where elements should be inserted into or retrieved
	 * from the tree.
	 */
	private static class PrefixQuery {
		String cachedToString;

		LinkedList<String> list = new LinkedList<String>();

		public static PrefixQuery all() {
			return new PrefixQuery("");
		}

		/**
		 * Construct a new query from the specified string. Prefixes must be
		 * separated by dots.
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
	 * Data structure to group elements which share common prefixes. E.g. adding
	 * the following elements:
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
			root = new PrefixNode<T>();
		}

		/**
		 * Adds the specified element on the level represented by the specified
		 * query. This method is in O(#number of prefixes in query)
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
		 * Collects all elements of the level specified by the query and below.
		 * E.g. using the query 'math' on the example tree from the javadoc of
		 * this class would return all elements contained in the tree except for
		 * 'obj5'. 'math.add' would only return 'obj1' and 'obj2'. This method
		 * returns an iterable over these elements in O(# number of all nodes
		 * below query). The number of nodes is the number of distinct prefixes
		 * below the specified query.
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
		 * Wrapper for {@link ArrayList}s providing O(1) concatenation of lists
		 * if only an iterator over theses lists is required. The order of lists
		 * will be retained. Added lists will be simply saved in a super
		 * LinkedList. If the iterator reaches the end of one list, it will
		 * switch to the next if available.
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
