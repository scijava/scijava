/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.function.Computers.Arity1;
import org.scijava.log.LogService;
import org.scijava.ops.OpDependency;
import org.scijava.ops.OpDependencyMember;
import org.scijava.ops.OpEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.OpInfo;
import org.scijava.ops.OpMethod;
import org.scijava.ops.OpUtils;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.hints.BaseOpHints.Adaptation;
import org.scijava.ops.hints.BaseOpHints.Simplification;
import org.scijava.ops.hints.Hints;
import org.scijava.ops.hints.impl.AdaptationHints;
import org.scijava.ops.hints.impl.DefaultHints;
import org.scijava.ops.hints.impl.SimplificationHints;
import org.scijava.ops.matcher.DefaultOpMatcher;
import org.scijava.ops.matcher.DependencyMatchingException;
import org.scijava.ops.matcher.MatchingUtils;
import org.scijava.ops.matcher.OpAdaptationInfo;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpCandidate.StatusCode;
import org.scijava.ops.matcher.OpClassInfo;
import org.scijava.ops.matcher.OpFieldInfo;
import org.scijava.ops.matcher.OpMatcher;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.OpMethodInfo;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.provenance.OpHistory;
import org.scijava.ops.simplify.SimplifiedOpInfo;
import org.scijava.ops.util.OpWrapper;
import org.scijava.param.FunctionalMethodType;
import org.scijava.param.ParameterStructs;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;
import org.scijava.types.TypeService;
import org.scijava.types.Types;
import org.scijava.util.ClassUtils;

/**
 * Default implementation of {@link OpEnvironment}, whose ops and related state
 * are discovered from a SciJava application context.
 * 
 * @author Curtis Rueden
 */
public class DefaultOpEnvironment extends AbstractContextual implements OpEnvironment {

	@Parameter
	private PluginService pluginService;

	private OpMatcher matcher;

	@Parameter
	private LogService log;

	@Parameter
	private TypeService typeService;

	/**
	 * Data structure storing all known Ops, grouped by name. This reduces the
	 * search size for any Op request to the number of known Ops with the name
	 * given in the request.
	 */
	private Map<String, Set<OpInfo>> opDirectory;

	/**
	 * Map containing pairs of {@link MatchingConditions} (i.e. the {@link OpRef}
	 * and {@Hints} used to find an Op) and the {@code Op}s that matched those
	 * requests. Used to quickly return Ops when the matching conditions are
	 * identical to those of a previous call.
	 *
	 * @see OpRef#equals(Object)
	 */
	private Map<MatchingConditions, Object> opCache;

	/**
	 * Data structure storing all known {@link OpWrapper}s. Each {@link OpWrapper}
	 * is retrieved by providing the {@link Class} that it is able to wrap.
	 */
	private Map<Class<?>, OpWrapper<?>> wrappers;

	/**
	 * Data structure storing this Environment's {@link Hints}. NB whenever this
	 * Object is used, <b>a copy should be made</b> to prevent concurrency issues.
	 */
	private Hints environmentHints = null;

	public DefaultOpEnvironment(final Context context) {
		context.inject(this);
		matcher = new DefaultOpMatcher(log);
		// HACK
		OpHistory.resetHistory();
	}

	@Override
	public Set<OpInfo> infos() {
		if (opDirectory == null) initOpDirectory();
		return opDirectory.values().stream().flatMap(list -> list.stream()).collect(Collectors.toSet());
	}

	@Override
	public Set<OpInfo> infos(String name) {
		if (opDirectory == null) initOpDirectory();
		if (name == null || name.isEmpty()) return infos();
		return opsOfName(name);
	}

	@Override
	public Set<OpInfo> infos(Hints hints) {
		return filterInfos(infos(), hints);
	}

	@Override
	public Set<OpInfo> infos(String name, Hints hints) {
		return filterInfos(infos(name), hints);
	}

	private Set<OpInfo> filterInfos(Set<OpInfo> infos, Hints hints) {
		boolean adapting = hints.containsHint(Adaptation.IN_PROGRESS);
		boolean simplifying = hints.containsHint(Simplification.IN_PROGRESS);
		// if we aren't doing any 
		if (!(adapting || simplifying)) return infos;
		return infos.parallelStream() //
				// filter out unadaptable ops
				.filter(info -> !adapting || !info.declaredHints().containsHint(Adaptation.FORBIDDEN)) //
				// filter out unadaptable ops
				.filter(info -> !simplifying || !info.declaredHints().containsHint(Simplification.FORBIDDEN)) //
				.collect(Collectors.toSet());
	}

	@Override
	public <T> T op(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType) {
		try {
			return findOpInstance(opName, specialType, inTypes, outType, getHints());
		} catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T op(final OpInfo info, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType) {
		try {
			Type[] types = Arrays.stream(inTypes).map(nil -> nil.getType()).toArray(Type[]::new);
			OpRef ref = OpRef.fromTypes(specialType.getType(), outType.getType(), types);
			return (T) findOpInstance(ref, info, getHints());
		} catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <T> T op(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType, Hints hints) {
		try {
			return findOpInstance(opName, specialType, inTypes, outType, hints.getCopy(true));
		} catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T op(final OpInfo info, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType, Hints hints) {
		try {
			Type[] types = Arrays.stream(inTypes).map(nil -> nil.getType()).toArray(Type[]::new);
			OpRef ref = OpRef.fromTypes(specialType.getType(), outType.getType(), types);
			return (T) findOpInstance(ref, info, hints.getCopy(true));
		} catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public Type genericType(Object obj) {
		return typeService.reify(obj);
	}

	@Override
	public <T> T bakeLambdaType(final T op, Type reifiedType) {
		if (wrappers == null) initWrappers();
		return LambdaTypeBaker.bakeLambdaType(op, reifiedType);
	}

	@Override
	public OpInfo opify(final Class<?> opClass) {
		return opify(opClass, Priority.NORMAL);
	}

	@Override
	public OpInfo opify(final Class<?> opClass, final double priority) {
		return new OpClassInfo(opClass, priority);
	}

	@Override
	public void register(final OpInfo info, final String name) {
		if (opDirectory == null) initOpDirectory();
		addToOpIndex(info, name);
	}

	@SuppressWarnings("unchecked")
	private <T> T findOpInstance(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?> outType, Hints hints) throws OpMatchingException {
		final OpRef ref = OpRef.fromTypes(opName, specialType.getType(), outType != null ? outType.getType() : null,
				toTypes(inTypes));
		return (T) findOpInstance(ref, hints);
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType()).toArray(Type[]::new);
	}
	
	/**
	 * Creates an Op instance from an {@link OpInfo} with the provided {@link OpRef} as a template.
	 * 
	 * @param ref
	 * @param info
	 * @return an Op created from {@code info}
	 * @throws OpMatchingException
	 */
	private Object findOpInstance(final OpRef ref, final OpInfo info, Hints hints) throws OpMatchingException {
		// create new OpCandidate from ref and info
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		if (!ref.typesMatch(info.opType(), typeVarAssigns))
			throw new OpMatchingException(
				"The given OpRef and OpInfo are not compatible!");
		OpCandidate candidate = new OpCandidate(this, this.log, ref, info,
			typeVarAssigns);
		// TODO: can this be replaced by simply setting the status code to match?
		if (!matcher.typesMatch(candidate)) throw new OpMatchingException(
			"The given OpRef and OpInfo are not compatible!");
		// obtain Op instance (with dependencies)
		Object op = instantiateOp(candidate, hints);

		// wrap Op
		Object wrappedOp = wrapOp(op, candidate.opInfo(), hints, candidate
			.typeVarAssigns());

		MatchingConditions conditions = new MatchingConditions(ref, hints);
		cacheOp(conditions, wrappedOp);

		return wrappedOp;
	}
	
	/**
	 * Finds an Op instance matching the request described by {@link OpRef}
	 * {@code ref}. NB the return must be an {@link Object} here (instead of some
	 * type variable T where T is the Op type} since there is no way to ensure
	 * that the {@code OpRef} can provide that T (since the OpRef could require
	 * that the Op returned is of multiple types).
	 * 
	 * @param ref
	 * @param hints - the {@link Hints} to use for matching
	 * @return an Op satisfying the request described by {@code ref}.
	 * @throws OpMatchingException
	 */
	private Object findOpInstance(final OpRef ref,
		Hints hints) throws OpMatchingException
	{
		// see if the ref has been matched already
		MatchingConditions conditions = new MatchingConditions(ref, hints);
		Object cachedOp = checkCacheForRef(conditions);
		if (cachedOp != null) return cachedOp;

		// obtain suitable OpCandidate
		OpCandidate candidate = findOpCandidate(ref, hints);

		// obtain Op instance (with dependencies)
		Object op = instantiateOp(candidate, hints);
		
		// wrap Op
		Object wrappedOp = wrapOp(op, candidate.opInfo(), hints, candidate.typeVarAssigns());

		// cache instance
		cacheOp(conditions, wrappedOp);

		return wrappedOp;
	}

	private void cacheOp(MatchingConditions conditions, Object op) {
		opCache.putIfAbsent(conditions, op);
	}

	private Object checkCacheForRef(MatchingConditions conditions) {
		if (opCache == null) {
			opCache = new HashMap<>();
		}
		if (opCache.containsKey(conditions))
			return opCache.get(conditions);
		return null;
	}
	
	private OpCandidate findOpCandidate(OpRef ref, Hints hints) throws OpMatchingException{
		try {
			// attempt to find a direct match
			return matcher.findSingleMatch(this, ref, hints);
		}
		catch (OpMatchingException e1) {
			// no direct match; find an adapted match
			try {
				if (hints.containsHint(Adaptation.ALLOWED)) return adaptOp(ref, hints);
				throw new OpMatchingException("No matching Op for request: " + ref +
					"\n(adaptation is disabled)", e1);
			}
			catch (OpMatchingException e2) {
				try {
					if (hints.containsHint(Simplification.ALLOWED)) return findSimplifiedOp(
						ref, hints);
					throw new OpMatchingException("No matching Op for request: " + ref +
						"\n(simplification is disabled)", e1);
				}
				catch (OpMatchingException e3) {
					// NB: It is important that the adaptation and simplification errors be
					// suppressed here. If a failure occurs in Op matching, it
					// is not the fault of our adaptation/simplification process but is
					// instead due to the incongruity between the request and the set of
					// available Ops. Thus the error stemming from the direct match
					// attempt will provide the user with more information on how to fix
					// their Op request.
					e1.addSuppressed(e2);
					e1.addSuppressed(e3);
					throw e1;
				}
			}
		}
	}

	private OpCandidate findSimplifiedOp(OpRef ref, Hints hints) throws OpMatchingException {
		Hints simplificationHints = SimplificationHints.generateHints(hints, false);
		return matcher.findSingleMatch(this, ref, simplificationHints);
	}

	/**
	 * Creates an instance of the Op from the {@link OpCandidate} <b>with its
	 * required {@link OpDependency} fields</b>.
	 * 
	 * @param candidate
	 * @return an Op with all needed dependencies
	 * @throws OpMatchingException
	 */
	private Object instantiateOp(final OpCandidate candidate, Hints hints)
		throws OpMatchingException
	{
		final List<Object> dependencies = resolveOpDependencies(candidate, hints);
		return candidate.createOp(dependencies);
	}

	/**
	 * Wraps the matched op into an {@link Op} that knows its generic typing.
	 * 
	 * @param op - the op to wrap.
	 * @param opInfo - from which we determine the {@link Type} of the {@code Op}
	 *            
	 * @return an {@link Op} wrapping of op.
	 */
	private <T> T wrapOp(T op, OpInfo opInfo, Hints hints, Map<TypeVariable<?>, Type> typeVarAssigns) {
		if (wrappers == null)
			initWrappers();

		Type opType = opInfo.opType();
		try {
			// find the opWrapper that wraps this type of Op
			Class<?> wrapper = getWrapperClass(op, opInfo);
			// obtain the generic type of the Op w.r.t. the Wrapper class 
			Type exactSuperType = Types.getExactSuperType(opType, wrapper);
			Type reifiedSuperType = Types.substituteTypeVariables(exactSuperType, typeVarAssigns);
			// wrap the Op
			@SuppressWarnings("unchecked")
			final OpWrapper<T> opWrapper = (OpWrapper<T>) wrappers.get(Types.raw(reifiedSuperType));
			return opWrapper.wrap(op, opInfo, hints.executionChainID(), reifiedSuperType);
		} catch (IllegalArgumentException | SecurityException exc) {
			log.error(exc.getMessage() != null ? exc.getMessage() : "Cannot wrap " + op.getClass());
			return op;
		} catch (NullPointerException e) {
			log.error("No wrapper exists for " + Types.raw(opType).toString() + ".");
			return op;
		}
	}

	private Class<?> getWrapperClass(Object op, OpInfo info) {
		List<Class<?>> suitableWrappers = wrappers.keySet().parallelStream().filter(
			wrapper -> wrapper.isInstance(op)).collect(Collectors.toList());
		List<Class<?>> filteredWrappers = filterWrapperSuperclasses(
			suitableWrappers);
		if (filteredWrappers.size() == 0) throw new IllegalArgumentException(info
			.implementationName() + ": matched op Type " + info.opType().getClass() +
			" does not match a wrappable Op type.");
		if (filteredWrappers.size() > 1) throw new IllegalArgumentException(
			"Matched op Type " + info.opType().getClass() +
				" matches multiple Op types: " + filteredWrappers.toString());
		if (!Types.isAssignable(Types.raw(info.opType()), filteredWrappers.get(0)))
			throw new IllegalArgumentException(Types.raw(info.opType()) +
				"cannot be wrapped as a " + filteredWrappers.get(0).getClass());
		return filteredWrappers.get(0);
	}

	private List<Class<?>> filterWrapperSuperclasses(
		List<Class<?>> suitableWrappers)
	{
		if (suitableWrappers.size() < 2) return suitableWrappers;
		List<Class<?>> list = new ArrayList<>();
		for (Class<?> c : suitableWrappers) {
			boolean isSuperclass = false;
			for (Class<?> other : suitableWrappers) {
				if (c.equals(other)) continue;
				if (c.isAssignableFrom(other)) isSuperclass = true;
			}
			if (!isSuperclass) list.add(c);
		}
		return list;
	}

	private List<Object> resolveOpDependencies(OpCandidate candidate, Hints hints) throws OpMatchingException {
		return resolveOpDependencies(candidate.opInfo(), candidate.typeVarAssigns(), hints);
	}

	private void initWrappers() {
		wrappers = new HashMap<>();
		for (OpWrapper<?> wrapper : pluginService.createInstancesOfType(OpWrapper.class)) {
			wrappers.put(wrapper.type(), wrapper);
		}
	}

	/**
	 * Attempts to inject {@link OpDependency} annotated fields of the specified
	 * object by looking for Ops matching the field type and the name specified in
	 * the annotation. The field type is assumed to be functional.
	 *
	 * @param info - the {@link OpInfo} whose {@link OpDependency}s will be
	 *          injected
	 * @param typeVarAssigns - the mapping of {@link TypeVariable}s in the
	 *          {@code OpInfo} to {@link Type}s given in the request.
	 * @throws OpMatchingException if the type of the specified object is not
	 *           functional, if the Op matching the functional type and the name
	 *           could not be found, if an exception occurs during injection
	 */
	private List<Object> resolveOpDependencies(OpInfo info, Map<TypeVariable<?>, Type> typeVarAssigns, Hints hints) throws OpMatchingException {

		final List<OpDependencyMember<?>> dependencies = info.dependencies();
		final List<Object> resolvedDependencies = new ArrayList<>(dependencies.size());

		for (final OpDependencyMember<?> dependency : dependencies) {
			final OpRef dependencyRef = inferOpRef(dependency, typeVarAssigns);
			try {
				Hints hintCopy = hints.getCopy(false);
				hintCopy.setHint(Simplification.FORBIDDEN);
				if(!dependency.isAdaptable()) {
					hintCopy.setHint(Adaptation.FORBIDDEN);
				}
				resolvedDependencies.add(findOpInstance(dependencyRef, hintCopy));
			}
			catch (final OpMatchingException e) {
				String message = DependencyMatchingException.message(info
					.implementationName(), dependency.getKey(), dependencyRef);
				if (e instanceof DependencyMatchingException) {
					throw new DependencyMatchingException(message, (DependencyMatchingException) e);
				}
				throw new DependencyMatchingException(message);
			}
		}
		return resolvedDependencies;
	}

	/**
	 * Adapts an Op with the name of ref into a type that can be SAFELY cast to
	 * ref.
	 * <p>
	 * NB This method <b>cannot</b> use the {@link OpMatcher} to find a suitable
	 * {@code adapt} Op. The premise of adaptation depends on the ability to
	 * examine the applicability of all {@code adapt} Ops with the correct output
	 * type. We need to check all of them because we do not know whether:
	 * <ul>
	 * <li>The dependencies will exist for a particular {@code adapt} Op
	 * <li>The Op we want exists with the correct type for the input of the
	 * {@code adapt} Op.
	 * </ul>
	 * 
	 * @param ref - the type of Op that we are looking to adapt to.
	 * @return {@link OpCandidate} - an Op that has been adapted to conform
	 *         the the ref type (if one exists).
	 * @throws OpMatchingException
	 */
	private OpCandidate adaptOp(OpRef ref, Hints hints) throws OpMatchingException {

		List<DependencyMatchingException> depExceptions = new ArrayList<>();
		for (final OpInfo adaptor : infos("adapt")) {
			Type adaptTo = adaptor.output().getType();
			Map<TypeVariable<?>, Type> map = new HashMap<>();
			// make sure that the adaptor outputs the correct type
			if (!adaptOpOutputSatisfiesRefTypes(adaptTo, map, ref)) continue;
			// make sure that the adaptor is a Function (so we can cast it later)
			if (Types.isInstance(adaptor.opType(), Function.class)) {
				log.debug(adaptor + " is an illegal adaptor Op: must be a Function");
				continue;
			}

			if(adaptor instanceof SimplifiedOpInfo) {
				log.debug(adaptor + " has been simplified. This is likely a typo.");
			}

			try {
				// resolve adaptor dependencies
				final List<Object> dependencies = resolveOpDependencies(adaptor, map, hints);

				@SuppressWarnings("unchecked")
				Function<Object, Object> adaptorOp = //
					(Function<Object, Object>) adaptor.createOpInstance(dependencies) //
						.object(); //

				// grab the first type parameter from the OpInfo and search for
				// an Op that will then be adapted (this will be the only input of the
				// adaptor since we know it is a Function)
				Type srcOpType = Types.substituteTypeVariables(adaptor.inputs().get(0)
					.getType(), map);
				final OpRef srcOpRef = inferOpRef(srcOpType, ref.getName(), map);
				final OpCandidate srcCandidate = findAdaptationCandidate(srcOpRef, hints);
				map.putAll(srcCandidate.typeVarAssigns());
				Type adapterOpType = Types.substituteTypeVariables(adaptor.output()
					.getType(), map);
				OpAdaptationInfo adaptedInfo = new OpAdaptationInfo(srcCandidate
					.opInfo(), adapterOpType, adaptorOp);
				OpCandidate adaptedCandidate = new OpCandidate(this, log, ref, adaptedInfo, map);
				adaptedCandidate.setStatus(StatusCode.MATCH);
				return adaptedCandidate;
			}
			catch (DependencyMatchingException d) {
				depExceptions.add(d);
			}
			catch (OpMatchingException e1) {
				log.trace(e1);
			}
		}

		//no adaptors available.
		if (depExceptions.size() == 0) {
			throw new OpMatchingException(
				"Op adaptation failed: no adaptable Ops of type " + ref.getName());
		}
		StringBuilder sb = new StringBuilder();
		for (DependencyMatchingException d : depExceptions) {
			sb.append("\n\n" + d.getMessage());
		}
		throw new DependencyMatchingException(sb.toString());
	}

	private OpCandidate findAdaptationCandidate(final OpRef srcOpRef, final Hints hints)
		throws OpMatchingException
	{
		Hints adaptationHints = AdaptationHints.generateHints(hints, false);
		final OpCandidate srcCandidate = findOpCandidate(srcOpRef, adaptationHints);
		return srcCandidate;
	}

	private boolean adaptOpOutputSatisfiesRefTypes(Type adaptTo, Map<TypeVariable<?>, Type> map, OpRef ref) {
		Type opType = ref.getType();
		// TODO: clean this logic -- can this just be ref.typesMatch() ?
		if (opType instanceof ParameterizedType) {
			if (!MatchingUtils.checkGenericAssignability(adaptTo,
				(ParameterizedType) opType, map, true))
			{
				return false;
			}
		}
		else if (!Types.isAssignable(opType, adaptTo, map)) {
			return false;
		}
		return true;
	}

	private OpRef inferOpRef(OpDependencyMember<?> dependency,
		Map<TypeVariable<?>, Type> typeVarAssigns) throws OpMatchingException
	{
		final Type mappedDependencyType = Types.mapVarToTypes(new Type[] {
			dependency.getType() }, typeVarAssigns)[0];
		final String dependencyName = dependency.getDependencyName();
		final OpRef inferredRef = inferOpRef(mappedDependencyType, dependencyName,
			typeVarAssigns);
		if (inferredRef != null) return inferredRef;
		throw new OpMatchingException("Could not infer functional " +
			"method inputs and outputs of Op dependency field: " + dependency
				.getKey());
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

		EnumSet<ItemIO> inIos = EnumSet.of(ItemIO.INPUT, ItemIO.CONTAINER, ItemIO.MUTABLE);
		EnumSet<ItemIO> outIos = EnumSet.of(ItemIO.OUTPUT, ItemIO.CONTAINER, ItemIO.MUTABLE);

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
		return new OpRef(name, type, mappedOutputs[0], mappedInputs);
	}

	private void initOpDirectory() {
		opDirectory = new HashMap<>();

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
				final Class<? extends OpCollection> c = pluginInfo.loadClass();
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
				final List<Method> methods = ClassUtils.getAnnotatedMethods(c, OpMethod.class);
				for (final Method method: methods) {
					OpInfo opInfo = new OpMethodInfo(method);
					addToOpIndex(opInfo, method.getAnnotation(OpMethod.class).names());
				}
			} catch (InstantiableException | InstantiationException | IllegalAccessException exc) {
				log.error("Can't load class from plugin info: " + pluginInfo.toString(), exc);
			}
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
			if (!opDirectory.containsKey(opName))
				opDirectory.put(opName, new TreeSet<>());
			boolean success = opDirectory.get(opName).add(opInfo);
			if(!success) System.out.println("Did not add OpInfo "+ opInfo);
		}
	}

	private Set<OpInfo> opsOfName(final String name) {
		final Set<OpInfo> ops = opDirectory.getOrDefault(name, Collections.emptySet());
		return Collections.unmodifiableSet(ops);
	}

	/**
	 * Sets the default {@Hints} used for finding Ops.
	 * <p>
	 * Note that this method is <b>not</b> thread safe and is provided for
	 * convenience. If the user wishes to use {@Hints} in a thread-safe manner,
	 * they should use
	 * {@link DefaultOpEnvironment#op(String, Nil, Nil[], Nil, Hints)} if using
	 * different {@Hint}s for different calls. Alternatively, this method can be
	 * called before all Ops called in parallel without issues.
	 */
	@Override
	public void setHints(Hints hints) {
		this.environmentHints = hints.getCopy(false);
	}

	/**
	 * Obtains some {@link Hints} to be used for finding Ops. This {@code Hints} can be either:
	 * <ul>
	 * <li> some {@link Hints} given to this environment via {@link #setHints(Hints)}
	 * <li> a {@link DefaultHints}
	 * </li>
	 * TODO: Consider concurrency issues
	 * @return a {@link Hints}
	 */
	private Hints getHints() {
		if(this.environmentHints != null) return this.environmentHints.getCopy(true);
		return new DefaultHints();
	}

}
