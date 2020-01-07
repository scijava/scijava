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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.matcher.DefaultOpMatcher;
import org.scijava.ops.matcher.MatchingUtils;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpClassInfo;
import org.scijava.ops.matcher.OpFieldInfo;
import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpMatcher;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.AdaptedOp;
import org.scijava.ops.transform.OpRunner;
import org.scijava.ops.transform.OpTransformationMatcher;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.types.Nil;
import org.scijava.ops.types.TypeService;
import org.scijava.ops.util.OpWrapper;
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

	private OpMatcher opMatcher;

	@Parameter
	private LogService log;

	private OpTransformationMatcher transformationMatcher;

	@Parameter
	private TypeService typeService;

	/**
	 * Prefix tree to cache and quickly find {@link OpInfo}s.
	 */
	// private PrefixTree<OpInfo> opCache;

	/**
	 * Map to collect all aliases for a specific op. All aliases will map to one
	 * canonical name of the op which is defined as the first one.
	 */
	private Map<String, List<OpInfo>> opCache;

	private List<OpTransformer> transformerIndex;

	private Map<Class<?>, OpWrapper<?>> wrappers;

	private void initOpCache() {
		opCache = new HashMap<>();

		// Add regular Ops
		for (final PluginInfo<Op> pluginInfo : pluginService.getPluginsOfType(Op.class)) {
			try {
				final Class<?> opClass = pluginInfo.loadClass();
				OpInfo opInfo = new OpClassInfo(opClass);
				addToOpIndex(opInfo, pluginInfo.getName());
			} catch (InstantiableException exc) {
				log.error("Can't load class from plugin info: " + pluginInfo.toString(), exc);
			}
		}
		// Add Ops contained in an OpCollection
		for (final PluginInfo<OpCollection> pluginInfo : pluginService.getPluginsOfType(OpCollection.class)) {
			try {
				Class<? extends OpCollection> c = pluginInfo.loadClass();
				final List<Field> fields = ClassUtils.getAnnotatedFields(c, OpField.class);
				Object instance = null;
				for (Field field : fields) {
					final boolean isStatic = Modifier.isStatic(field.getModifiers());
					if (!isStatic && instance == null) {
						instance = field.getDeclaringClass().newInstance();
					}
					OpInfo opInfo = new OpFieldInfo(isStatic ? null : instance, field);
					addToOpIndex(opInfo, field.getAnnotation(OpField.class).names());
				}
			} catch (InstantiableException | InstantiationException | IllegalAccessException exc) {
				log.error("Can't load class from plugin info: " + pluginInfo.toString(), exc);
			}
		}
	}

	private void initWrappers() {
		wrappers = new HashMap<>();
		for (OpWrapper<?> wrapper : pluginService.createInstancesOfType(OpWrapper.class)) {
			wrappers.put(wrapper.type(), wrapper);
		}
	}

	private void addToOpIndex(final OpInfo opInfo, final String opNames) {
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
		for (String opName : parsedOpNames) {
			if (!opCache.containsKey(opName))
				opCache.put(opName, new ArrayList<>());
			opCache.get(opName).add(opInfo);
		}
	}

	public synchronized void initTransformerIndex() {
		transformerIndex = pluginService.createInstancesOfType(OpTransformer.class);
	}

	@Override
	public Iterable<OpInfo> infos() {
		if (opCache == null) {
			initOpCache();
		}
		return opCache.values().stream().flatMap(list -> list.stream()).collect(Collectors.toList());
	}

	@Override
	public Iterable<OpInfo> infos(String name) {
		if (opCache == null) {
			initOpCache();
		}
		if (name == null || name.isEmpty()) {
			return infos();
		}
		Iterable<OpInfo> infos = opCache.get(name);
		if (infos == null)
			throw new IllegalArgumentException("No op infos with name: " + name + " available.");
		return infos;
	}

	private OpMatcher getOpMatcher() {
		if (opMatcher == null) {
			opMatcher = new DefaultOpMatcher(log);
		}
		return opMatcher;
	}

	private synchronized List<OpTransformer> getTransformerIndex() {
		if (transformerIndex == null) {
			initTransformerIndex();
		}
		return transformerIndex;
	}

//	private OpTransformationMatcher getTransformationMatcher() {
//		if (transformationMatcher == null) {
//			transformationMatcher = new DefaultOpTransformationMatcher(getOpMatcher());
//		}
//		return transformationMatcher;
//	}

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
	private List<Object> resolveOpDependencies(OpCandidate op) throws OpMatchingException {
		final List<OpDependencyMember<?>> dependencies = op.opInfo().dependencies();
		final List<Object> resolvedDependencies = new ArrayList<>(dependencies.size());
		for (final OpDependencyMember<?> dependency : dependencies) {
			final String dependencyName = dependency.getDependencyName();
			final Type mappedDependencyType = Types.mapVarToTypes(new Type[] { dependency.getType() },
					op.typeVarAssigns())[0];
			final OpRef inferredRef = inferOpRef(mappedDependencyType, dependencyName, op.typeVarAssigns());
			if (inferredRef == null) {
				throw new OpMatchingException("Could not infer functional "
						+ "method inputs and outputs of Op dependency field: " + dependency.getKey());
			}
			try {
				resolvedDependencies.add(findOpInstance(dependencyName, inferredRef, dependency.isAdaptable()));
			} catch (final Exception e) {
				throw new OpMatchingException("Could not find Op that matches requested Op dependency:" + "\nOp class: "
						+ op.opInfo().implementationName() + //
						"\nDependency identifier: " + dependency.getKey() + //
						"\n\n Attempted request:\n" + inferredRef, e);
			}
		}
		return resolvedDependencies;
	}

	@SuppressWarnings("unchecked")
	public <T> T findOpInstance(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?> outType) {
		final OpRef ref = OpRef.fromTypes(opName, toTypes(specialType), outType != null ? outType.getType() : null,
				toTypes(inTypes));
		return (T) findOpInstance(opName, ref, true);
	}

	public Object findOpInstance(final String opName, final OpRef ref, boolean adaptable) {
		Object op = null;
		OpCandidate match = null;
		AdaptedOp adaptation = null;
		try {
			// Find single match which matches the specified types
			match = getOpMatcher().findSingleMatch(this, ref);
			final List<Object> dependencies = resolveOpDependencies(match);
			op = match.createOp(dependencies);
		} catch (OpMatchingException e) {
			log.debug("No matching Op for request: " + ref + "\n");
			if (adaptable == false) {
				throw new IllegalArgumentException(opName + " cannot be adapted (adaptation is disabled)");
			}
			log.debug("Attempting Op adaptation...");
			try {
				adaptation = adaptOp(opName, ref);
				op = adaptation.op();
			} catch (OpMatchingException e1) {
				log.debug("No suitable Op adaptation found");
				throw new IllegalArgumentException(e1);
			}

		}
		try {
			// Try to resolve annotated OpDependency fields
			// N.B. Adapted Op dependency fields are already matched.
			if (match != null)
				resolveOpDependencies(match);
		} catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
		// TODO: THIS IS WRONG! We need the OpInfo of the output of the adaptation!
		// This gives the OpInfo of the original (unadapted) op!
		OpInfo adaptedInfo = adaptation == null ? null : adaptation.srcInfo();
		Object wrappedOp = wrapOp(op, match, adaptedInfo);
		return wrappedOp;
	}

	public AdaptedOp adaptOp(String opName, OpRef ref) throws OpMatchingException {

		// TODO: support all types of ref
		// TODO: multi-stage adaptations (do we need this if we are not doing OpRunners
		// anymore?)
		// TODO: prevent searching for Op types that have already been searched for
		// TODO: export code to helper method.
		Type opType = ref.getTypes()[0];
		List<OpInfo> adaptors = opCache.get("adapt");

		// create a priority queue to store suitable transformations.
		Comparator<OpCandidate> comp = (OpCandidate i1,
				OpCandidate i2) -> i1.opInfo().priority() < i2.opInfo().priority() ? -1
						: i1.opInfo().priority() == i2.opInfo().priority() ? 0 : 1;
		Queue<OpCandidate> suitableAdaptors = new PriorityQueue<>(comp);

		// create an OpCandidate list of suitable adaptors
		for (OpInfo adaptor : adaptors) {
			Type adaptTo = adaptor.output().getType();
			Map<TypeVariable<?>, Type> map = new HashMap<>();
			// make sure that the adaptor outputs the correct type
			if(opType instanceof ParameterizedType) {
				try {
					if(!MatchingUtils.checkGenericAssignability(adaptTo, (ParameterizedType) opType, map, true))
					continue;
				} catch(IllegalArgumentException e) {continue; }
			}
			else if (!Types.isAssignable(opType, adaptTo, map)) {
				continue;
			}
			// make sure that the adaptor is a Function (so we can cast it later)
			if (Types.isInstance(adaptor.opType(), Function.class)) {
				log.debug(adaptor + " is an illegal adaptor Op: must be a Function");
				continue;
			}
			// build the type of fromOp (we know there must be one input because the adaptor
			// is a Function)
			Type adaptFrom = adaptor.inputs().get(0).getType();
			Type refAdaptTo = Types.substituteTypeVariables(adaptTo, map);
			Type refAdaptFrom = Types.substituteTypeVariables(adaptFrom, map);

			// build the OpRef of the adaptor.
			Type refType = Types.parameterize(Function.class, new Type[] { refAdaptFrom, refAdaptTo });
			OpRef adaptorRef = new OpRef("adapt", new Type[] { refType }, refAdaptTo, new Type[] { refAdaptFrom });

			// make an OpCandidate
			suitableAdaptors.add(new OpCandidate(this, log, adaptorRef, adaptor, map));
		}

		while (suitableAdaptors.size() > 0) {
			OpCandidate adaptor = suitableAdaptors.remove();
			try {
				// resolve adaptor dependencies and get the adaptor (as a function) //TODO
				final List<Object> dependencies = resolveOpDependencies(adaptor);
				// adaptor.setStatus(StatusCode.MATCH);
				Object adaptorOp = adaptor.opInfo().createOpInstance(dependencies).object();

				// grab the first type parameter (from the OpCandidate?) and search for an Op
				// that will then be adapted (this will be the first (only) type in the args of
				// the adaptor)
				Type adaptFrom = adaptor.paddedArgs()[0];
				final OpRef inferredRef = inferOpRef(adaptFrom, opName, adaptor.typeVarAssigns());
				// TODO: export this to another function (also done in findOpInstance).
				// We need this here because we need to grab the OpInfo. 
				// TODO: is there a better way to do this?
				final OpCandidate srcCandidate = getOpMatcher().findSingleMatch(this, inferredRef);
				final List<Object> srcDependencies = resolveOpDependencies(srcCandidate);
				final Object fromOp = srcCandidate.opInfo().createOpInstance(srcDependencies).object();

				// get adapted Op by applying adaptor on unadapted Op, then return
				// TODO: can we make this safer?
				@SuppressWarnings("unchecked")
				Object toOp = ((Function<Object, Object>) adaptorOp).apply(fromOp);
				return new AdaptedOp(toOp, srcCandidate.opInfo(), adaptor.opInfo());
			} catch (OpMatchingException e1) {
				continue;
			}

		}
		// no adaptors available.
		throw new OpMatchingException("Op adaptation failed: no adaptable Ops of type " + opName);
	}

	/**
	 * Wraps the matched op into an {@link Op} that knows its generic typing and
	 * {@link OpInfo}.
	 * 
	 * @param op
	 *            - the matched op to wrap.
	 * @param match
	 *            - where to retrieve the {@link OpInfo} if no transformation is
	 *            needed.
	 * @param transformation
	 *            - where to retrieve the {@link OpInfo} if a transformation is
	 *            needed.
	 * @return an {@link Op} wrapping of op.
	 */
	private Object wrapOp(Object op, OpCandidate match, OpInfo adaptationSrcInfo) {
		if (wrappers == null)
			initWrappers();

		// TODO: we don't want to wrap OpRunners, do we? What is the point?
		if (OpRunner.class.isInstance(op))
			return op;

		OpInfo opInfo = match == null ? adaptationSrcInfo : match.opInfo();
		// FIXME: this type is not necessarily Computer, Function, etc. but often
		// something more specific (like the class of an Op).
		Type type = opInfo.opType();
		try {
			// determine the Op wrappers that could wrap the matched Op
			Class<?>[] suitableWrappers = wrappers.keySet().stream().filter(wrapper -> wrapper.isInstance(op))
					.toArray(Class[]::new);
			if (suitableWrappers.length == 0)
				throw new IllegalArgumentException(opInfo.implementationName() + ": matched op Type " + type.getClass()
						+ " does not match a wrappable Op type.");
			if (suitableWrappers.length > 1)
				throw new IllegalArgumentException(
						"Matched op Type " + type.getClass() + " matches multiple Op types: " + wrappers.toString());
			// get the wrapper and wrap up the Op
			return wrap(suitableWrappers[0], op, opInfo);
		} catch (IllegalArgumentException | SecurityException exc) {
			log.error(exc.getMessage() != null ? exc.getMessage() : "Cannot wrap " + op.getClass());
			return op;
		} catch (NullPointerException e) {
			log.error("No wrapper exists for " + Types.raw(type).toString() + ".");
			return op;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T wrap(Class<T> opType, Object op, OpInfo info) {
		OpWrapper<T> wrapper = (OpWrapper<T>) wrappers.get(opType);
		return wrapper.wrap((T) op, info);
	}

	public <T> T findOp(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType) {
		return findOpInstance(opName, specialType, inTypes, outType);
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType()).toArray(Type[]::new);
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
	 * {@link ParameterStructs#findFunctionalMethodTypes(Type)}.
	 *
	 * @param type
	 * @param name
	 * @return null if the specified type has no functional method
	 */
	private OpRef inferOpRef(Type type, String name, Map<TypeVariable<?>, Type> typeVarAssigns)
			throws OpMatchingException {
		List<FunctionalMethodType> fmts = ParameterStructs.findFunctionalMethodTypes(type);
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
			if (!node.data.isEmpty()) {} else {
				sb.delete(sb.length() - 1, sb.length());
				sb.append(" <empty>\n");
			}

			sb.append(getIndent(level) + "Children:\n");

			for (Entry<String, PrefixNode<T>> e : node.children.entrySet()) {
				String sub = nodeToString(e.getValue(), e.getKey(), new StringBuilder(), level + 1);
				sb.append(sub + "\n");
			}
			if (!node.children.isEmpty()) {} else {
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
