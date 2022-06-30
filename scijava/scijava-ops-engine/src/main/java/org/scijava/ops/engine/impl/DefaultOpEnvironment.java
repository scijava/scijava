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

package org.scijava.ops.engine.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.discovery.Discoverer;
import org.scijava.log2.Logger;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.InfoChainGenerator;
import org.scijava.ops.api.OpCandidate;
import org.scijava.ops.api.OpCandidate.StatusCode;
import org.scijava.ops.api.OpDependencyMember;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.OpMetadata;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.api.OpWrapper;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.api.features.BaseOpHints.Adaptation;
import org.scijava.ops.api.features.BaseOpHints.DependencyMatching;
import org.scijava.ops.api.features.BaseOpHints.History;
import org.scijava.ops.api.features.BaseOpHints.Simplification;
import org.scijava.ops.api.features.DependencyMatchingException;
import org.scijava.ops.api.features.MatchingConditions;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.api.features.OpMatcher;
import org.scijava.ops.api.features.OpMatchingException;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.DefaultOpMatcher;
import org.scijava.ops.engine.matcher.impl.DefaultOpRef;
import org.scijava.ops.engine.matcher.impl.InfoMatchingOpRef;
import org.scijava.ops.engine.matcher.impl.OpAdaptationInfo;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.engine.simplify.SimplifiedOpInfo;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;
import org.scijava.types.TypeReifier;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;
import org.scijava.util.VersionUtils;

/**
 * Default implementation of {@link OpEnvironment}, whose ops and related state
 * are discovered from a SciJava application context.
 * 
 * @author Curtis Rueden
 */
public class DefaultOpEnvironment implements OpEnvironment {

	private final List<Discoverer> discoverers;

	private OpMatcher matcher;

	private Logger log;

	private TypeReifier typeService;

	private OpHistory history;

	/**
	 * The {@link OpInfoGenerator}s providing {@link OpInfo}s to this environment
	 */
	private List<OpInfoGenerator> infoGenerators;

	/**
	 * Data structure storing all known Ops, grouped by name. This reduces the
	 * search size for any Op request to the number of known Ops with the name
	 * given in the request.
	 */
	private Map<String, Set<OpInfo>> opDirectory;

	/**
	 * Data structure storing all known Ops, discoverable using their id.
	 */
	private Map<String, OpInfo> idDirectory;

	/**
	 * Data structure containing all known InfoChainGenerators
	 */
	private Set<InfoChainGenerator> infoChainGenerators;
	/**
	 * Map containing pairs of {@link MatchingConditions} (i.e. the {@link OpRef}
	 * and {@Hints} used to find an Op) and the {@link OpInstance} (wrapping an Op
	 * with its backing {@link OpInfo}) that matched those requests. Used to
	 * quickly return Ops when the matching conditions are identical to those of a
	 * previous call.
	 *
	 * @see MatchingConditions#equals(Object)
	 */
	private Map<MatchingConditions, OpInstance<?>> opCache;

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

	public DefaultOpEnvironment(final TypeReifier typeService,
		final Logger log, final OpHistory history,
		final List<OpInfoGenerator> infoGenerators, final List<Discoverer> d)
	{
		this.discoverers = d;
		this.typeService = typeService;
		this.log = log;
		this.history = history;
		this.infoGenerators = infoGenerators;
		matcher = new DefaultOpMatcher(getMatchingRoutines(this.discoverers));
	}

	public DefaultOpEnvironment(final TypeReifier typeService,
		final Logger log, final OpHistory history,
		final List<OpInfoGenerator> infoGenerators, final Discoverer... d)
	{
		this.discoverers = Arrays.asList(d);
		this.typeService = typeService;
		this.log = log;
		this.history = history;
		this.infoGenerators = infoGenerators;
		matcher = new DefaultOpMatcher(getMatchingRoutines(this.discoverers));
	}

	public static List<MatchingRoutine> getMatchingRoutines(
		final List<Discoverer> discoverers)
	{
		List<MatchingRoutine> matchers = new ArrayList<>();
		for (Discoverer d : discoverers) {
			List<Class<MatchingRoutine>> implementingClasses = d.implsOfType(
				MatchingRoutine.class);
			List<MatchingRoutine> routines = implementingClasses.parallelStream().map(
				c -> {
					try {
						return c.getConstructor().newInstance();
					}
					catch (Exception e) {
						return null;
					}
				}).filter(r -> r != null).collect(Collectors.toList());
			matchers.addAll(routines);
		}
		return matchers;
	}

	@Override
	public Set<OpInfo> infos() {
		if (opDirectory == null) initOpDirectory();
		return opDirectory.values().stream().flatMap(list -> list.stream()).collect(
			Collectors.toSet());
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
		boolean adapting = hints.contains(Adaptation.IN_PROGRESS);
		boolean simplifying = hints.contains(Simplification.IN_PROGRESS);
		// if we aren't doing any
		if (!(adapting || simplifying)) return infos;
		return infos.parallelStream() //
			// filter out unadaptable ops
			.filter(info -> !adapting || !info.declaredHints().contains(
				Adaptation.FORBIDDEN)) //
			// filter out unadaptable ops
			.filter(info -> !simplifying || !info.declaredHints().contains(
				Simplification.FORBIDDEN)) //
			.collect(Collectors.toSet());
	}

	@Override
	public <T> T op(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType)
	{
		return op(opName, specialType, inTypes, outType, getDefaultHints());
	}

	@Override
	public <T> T op(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType, Hints hints)
	{
		return findOp(opName, specialType, inTypes, outType, hints).asOpType();
	}

	@Override
	public InfoChain infoChain(String opName, Nil<?> specialType,
		Nil<?>[] inTypes, Nil<?> outType)
	{
		return infoChain(opName, specialType, inTypes, outType, getDefaultHints());
	}

	@Override
	public InfoChain infoChain(String opName, Nil<?> specialType,
		Nil<?>[] inTypes, Nil<?> outType, Hints hints)
	{
		try {
			return findOp(opName, specialType, inTypes, outType, hints).infoChain();
		}
		catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public InfoChain chainFromInfo(OpInfo info, Nil<?> specialType) {
		return findOp(info, specialType, getDefaultHints()).infoChain();
	}

	@Override
	public InfoChain chainFromInfo(OpInfo info, Nil<?> specialType, Hints hints) {
		return findOp(info, specialType, hints).infoChain();
	}

	@Override
	public <T> T opFromSignature(final String signature,
		final Nil<T> specialType)
	{
		InfoChain info = chainFromID(signature);
		return opFromInfoChain(info, specialType);
	}

	@Override
	public <T> T opFromInfoChain(final InfoChain chain,
		final Nil<T> specialType)
	{
		if (!(specialType.getType() instanceof ParameterizedType))
			throw new IllegalArgumentException("TODO");
		@SuppressWarnings("unchecked")
		OpInstance<T> instance = (OpInstance<T>) chain.op(specialType.getType());
		Hints hints = getDefaultHints();
		RichOp<T> wrappedOp = wrapOp(instance, hints);
		return wrappedOp.asOpType();

	}

	@Override
	public InfoChain chainFromID(String signature) {
		if (idDirectory == null) initIdDirectory();
		if (infoChainGenerators == null) initInfoChainGenerators();

		InfoChainGenerator genOpt = InfoChainGenerator.findSuitableGenerator(
			signature, infoChainGenerators);
		return genOpt.generate(signature, idDirectory, infoChainGenerators);
	}

	@Override
	public Type genericType(Object obj) {
		return typeService.reify(obj);
	}

	@Override
	public OpInfo opify(final Class<?> opClass) {
		return opify(opClass, Priority.NORMAL);
	}

	@Override
	public OpInfo opify(final Class<?> opClass, final double priority,
		final String... names)
	{
		return new OpClassInfo(opClass, VersionUtils.getVersion(opClass), new DefaultHints(), priority,
			names);
	}

	@Override
	public <T> T bakeLambdaType(T op, Type type) {
		return LambdaTypeBaker.bakeLambdaType(op, type);
	}

	@Override
	public void register(final OpInfo info) {
		if (opDirectory == null) initOpDirectory();
		addToOpIndex.accept(info);
	}

	@SuppressWarnings("unchecked")
	private <T> RichOp<T> findOp(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType, Hints hints)
	{
		final OpRef ref = DefaultOpRef.fromTypes(opName, specialType.getType(),
			outType != null ? outType.getType() : null, toTypes(inTypes));
		MatchingConditions conditions = generateCacheHit(ref, hints);
		return (RichOp<T>) wrapViaCache(conditions);
	}

	@SuppressWarnings("unchecked")
	private <T> OpInstance<T> findOp(final OpInfo info, final Nil<T> specialType,
		Hints hints) throws OpMatchingException
	{
		OpRef ref = new InfoMatchingOpRef(info, specialType);
		MatchingConditions conditions = insertCacheHit(ref, hints, info);
		return (OpInstance<T>) getInstance(conditions);
	}

	@SuppressWarnings("unchecked")
	private <T> RichOp<T> findRichOp(final OpInfo info, final Nil<T> specialType,
		Hints hints) throws OpMatchingException
	{
		OpInstance<T> instance = findOp(info, specialType, hints);
		return (RichOp<T>) wrap(instance, hints);
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType())
			.toArray(Type[]::new);
	}

	/**
	 * Creates an Op instance from an {@link OpInfo} with the provided
	 * {@link MatchingConditions} as the guidelines for {@link OpInfo} selection.
	 * This Op instance is put into the {@code opCache}, and is retrievable via
	 * {@link DefaultOpEnvironment#wrapViaCache(MatchingConditions)}
	 * 
	 * @param ref the {@link OpRef} request
	 * @param hints the {@link Hints} containing matching preferences
	 * @param info the {@link OpInfo} describing the Op that should match these
	 *          conditions1
	 * @return the {@link MatchingConditions} that will return the Op described by
	 *         {@code info} from the op cache
	 * @throws OpMatchingException
	 */
	private MatchingConditions insertCacheHit(final OpRef ref, final Hints hints,
		final OpInfo info)
	{
		MatchingConditions conditions = MatchingConditions.from(ref, hints);

		// create new OpCandidate from ref and info
		OpCandidate candidate = new ManualOpCandidate(this, ref, info,
			this.matcher);

		instantiateAndCache(conditions, candidate);

		return conditions;
	}

	private RichOp<?> wrapViaCache(MatchingConditions conditions) {
		OpInstance<?> instance = getInstance(conditions);
		return wrap(instance, conditions.hints());
	}

	private RichOp<?> wrap(OpInstance<?> instance, Hints hints) {
		RichOp<?> wrappedOp = wrapOp(instance, hints);
		return wrappedOp;
	}

	/**
	 * Finds an Op instance matching the request described by {@link OpRef}
	 * {@code ref} and stores this Op in {@code opCache}. NB the return must be an
	 * {@link Object} here (instead of some type variable T where T is the Op
	 * type} since there is no way to ensure that the {@code OpRef} can provide
	 * that T (since the OpRef could require that the Op returned is of multiple
	 * types).
	 * 
	 * @param ref the {@link OpRef} request
	 * @param hints the {@link Hints} containing matching preferences
	 * @return the {@link MatchingConditions} that will return the Op found from
	 *         the op cache
	 */
	private MatchingConditions generateCacheHit(OpRef ref, Hints hints) {
		MatchingConditions conditions = MatchingConditions.from(ref, hints);
		// see if the ref has been matched already
		OpInstance<?> cachedOp = getInstance(conditions);
		if (cachedOp != null) return conditions;

		// obtain suitable OpCandidate
		OpCandidate candidate = findOpCandidate(conditions.ref(), conditions
			.hints());

		instantiateAndCache(conditions, candidate);

		return conditions;
	}

	private void instantiateAndCache(MatchingConditions conditions,
		OpCandidate candidate)
	{
		// obtain Op instance (with dependencies)
		OpInstance<?> op = instantiateOp(candidate, conditions.hints());

		// cache instance
		cacheOp(conditions, op);
	}

	private void cacheOp(MatchingConditions conditions, OpInstance<?> op) {
		opCache.putIfAbsent(conditions, op);
	}

	private OpInstance<?> getInstance(MatchingConditions conditions) {
		if (opCache == null) {
			opCache = new HashMap<>();
		}
		return opCache.get(conditions);
	}

	private OpCandidate findOpCandidate(OpRef ref, Hints hints) {
		return matcher.match(MatchingConditions.from(ref, hints), this);
	}

	/**
	 * Creates an instance of the Op from the {@link OpCandidate} <b>with its
	 * required {@link OpDependency} fields</b>.
	 * 
	 * @param candidate
	 * @return an Op with all needed dependencies
	 */
	private OpInstance<?> instantiateOp(final OpCandidate candidate,
		Hints hints)
	{
		final List<RichOp<?>> conditions = resolveOpDependencies(candidate, hints);
		InfoChain adaptorChain = new DependencyRichOpInfoChain(candidate
			.opInfo(), conditions);
		return adaptorChain.op(candidate.getType());
	}

	/**
	 * Wraps the matched op into an {@link Op} that knows its generic typing.
	 * 
	 * @param instance - the {@link OpInstance} to wrap.
	 * @param hints - the {@link Hints} used to create the {@link OpInstance}
	 * @return an {@link Op} wrapping of op.
	 */
	@SuppressWarnings("unchecked")
	private <T> RichOp<T> wrapOp(OpInstance<T> instance, Hints hints)
		throws IllegalArgumentException
	{
		if (wrappers == null) initWrappers();

		try {
			// find the opWrapper that wraps this type of Op
			Class<?> wrapper = getWrapperClass(instance.op(), instance.infoChain()
				.info());
			// obtain the generic type of the Op w.r.t. the Wrapper class
			Type reifiedSuperType = Types.getExactSuperType(instance.getType(),
				wrapper);
			OpMetadata metadata = new OpMetadata(reifiedSuperType, instance
				.infoChain(), hints, history);
			// wrap the Op
			final OpWrapper<T> opWrapper = (OpWrapper<T>) wrappers.get(Types.raw(
				reifiedSuperType));
			return opWrapper.wrap(instance, metadata);
		}
		catch (IllegalArgumentException | SecurityException exc) {
			throw new IllegalArgumentException(exc.getMessage() != null ? exc
				.getMessage() : "Cannot wrap " + instance.op().getClass());
		}
		catch (NullPointerException e) {
			throw new IllegalArgumentException("No wrapper exists for " + Types.raw(
				instance.getType()).toString() + ".");
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

	private List<RichOp<?>> resolveOpDependencies(OpCandidate candidate,
		Hints hints)
	{
		return resolveOpDependencies(candidate.opInfo(), candidate.typeVarAssigns(),
			hints);
	}

	@SuppressWarnings("rawtypes")
	private synchronized void initWrappers() {
		if (wrappers != null) return;
		wrappers = new HashMap<>();
		for (Discoverer d : discoverers)
			for (Class<OpWrapper> cls : d.implsOfType(OpWrapper.class)) {
				OpWrapper<?> wrapper;
				try {
					wrapper = cls.getDeclaredConstructor().newInstance();
					wrappers.put(wrapper.type(), wrapper);
				}
				catch (Throwable t) {
					log.warn("OpWrapper " + cls + " not instantiated. Due to " + t);
				}
			}
	}

	private synchronized void initInfoChainGenerators() {
		if (infoChainGenerators != null) return;
		Set<InfoChainGenerator> generators = new HashSet<>();
		for (Discoverer d : discoverers)
			for (Class<InfoChainGenerator> cls : d.implsOfType(
				InfoChainGenerator.class))
			{
				InfoChainGenerator wrapper;
				try {
					wrapper = cls.getDeclaredConstructor().newInstance();
					generators.add(wrapper);
				}
				catch (Throwable t) {
					log.warn("OpWrapper " + cls + " not instantiated. Due to " + t);
				}
			}
		infoChainGenerators = generators;
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
	 */
	private List<RichOp<?>> resolveOpDependencies(OpInfo info,
		Map<TypeVariable<?>, Type> typeVarAssigns, Hints hints)
	{

		final List<OpDependencyMember<?>> dependencies = info.dependencies();
		final List<RichOp<?>> dependencyChains = new ArrayList<>();

		for (final OpDependencyMember<?> dependency : dependencies) {
			final OpRef dependencyRef = inferOpRef(dependency, typeVarAssigns);
			try {
				// TODO: Consider a new Hint implementation
				Hints hintsCopy = hints.plus(DependencyMatching.IN_PROGRESS,
					History.SKIP_RECORDING, Simplification.FORBIDDEN);
				if (!dependency.isAdaptable()) {
					hintsCopy = hintsCopy.plus(Adaptation.FORBIDDEN);
				}

				MatchingConditions conditions = generateCacheHit(dependencyRef,
					hintsCopy);
				dependencyChains.add(wrapViaCache(conditions));
			}
			catch (final OpMatchingException e) {
				String message = DependencyMatchingException.message(info
					.implementationName(), dependency.getKey(), dependencyRef);
				if (e instanceof DependencyMatchingException) {
					throw new DependencyMatchingException(message,
						(DependencyMatchingException) e);
				}
				throw new DependencyMatchingException(message);
			}
		}
		return dependencyChains;
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
	 * @return {@link OpCandidate} - an Op that has been adapted to conform the
	 *         the ref type (if one exists).
	 */
	private OpCandidate adaptOp(OpRef ref, Hints hints) {

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

			if (adaptor instanceof SimplifiedOpInfo) {
				log.debug(adaptor + " has been simplified. This is likely a typo.");
			}

			try {
				// resolve adaptor dependencies
				final List<RichOp<?>> dependencies = resolveOpDependencies(adaptor, map,
					hints);
				InfoChain adaptorChain = new DependencyRichOpInfoChain(adaptor,
					dependencies);

				// grab the first type parameter from the OpInfo and search for
				// an Op that will then be adapted (this will be the only input of the
				// adaptor since we know it is a Function)
				Type srcOpType = Types.substituteTypeVariables(adaptor.inputs().get(0)
					.getType(), map);
				final OpRef srcOpRef = inferOpRef(srcOpType, ref.getName(), map);
				final OpCandidate srcCandidate = findAdaptationCandidate(srcOpRef,
					hints);
				map.putAll(srcCandidate.typeVarAssigns());
				Type adapterOpType = Types.substituteTypeVariables(adaptor.output()
					.getType(), map);
				OpAdaptationInfo adaptedInfo = new OpAdaptationInfo(srcCandidate
					.opInfo(), adapterOpType, adaptorChain);
				OpCandidate adaptedCandidate = new OpCandidate(this, ref, adaptedInfo,
					map);
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

		// no adaptors available.
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

	private OpCandidate findAdaptationCandidate(final OpRef srcOpRef,
		final Hints hints)
	{
		Hints adaptationHints = hints.plus(Adaptation.IN_PROGRESS);
		final OpCandidate srcCandidate = findOpCandidate(srcOpRef, adaptationHints);
		return srcCandidate;
	}

	private boolean adaptOpOutputSatisfiesRefTypes(Type adaptTo,
		Map<TypeVariable<?>, Type> map, OpRef ref)
	{
		Type opType = ref.getType();
		// TODO: clean this logic -- can this just be ref.typesMatch() ?
		if (opType instanceof ParameterizedType) {
			if (!GenericAssignability.checkGenericAssignability(adaptTo,
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
		Map<TypeVariable<?>, Type> typeVarAssigns) 
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
	 * {@link FunctionalParameters#findFunctionalMethodTypes(Type)}.
	 *
	 * @param type
	 * @param name
	 * @return null if the specified type has no functional method
	 */
	private OpRef inferOpRef(Type type, String name, Map<TypeVariable<?>, Type> typeVarAssigns)
			{
		List<FunctionalMethodType> fmts = FunctionalParameters.findFunctionalMethodTypes(type);
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
		return new DefaultOpRef(name, type, mappedOutputs[0], mappedInputs);
	}

	private synchronized void initOpDirectory() {
		if (opDirectory != null) return;
		opDirectory = new HashMap<>();
		for (final OpInfoGenerator generator : infoGenerators) {
			List<OpInfo> infos = generator.generateInfos();
			infos.forEach(addToOpIndex);
		}
	}

	private synchronized void initIdDirectory() {
		if (idDirectory != null) return;
		idDirectory = new HashMap<>();
		if (opDirectory == null) initOpDirectory();

		opDirectory.values().stream().flatMap(c -> c.stream()).forEach(info -> {
			idDirectory.put(info.id(), info);
		});
	}

	private final Consumer<OpInfo> addToOpIndex = (final OpInfo opInfo) -> {
		if (opInfo.names() == null || opInfo.names().size() == 0) {
			log.error("Skipping Op " + opInfo.implementationName() + ":\n" +
				"Op implementation must provide name.");
			return;
		}
		if (!opInfo.isValid()) {
			log.error("Skipping invalid Op " + opInfo.implementationName() + ":\n" +
				opInfo.getValidityException().getMessage());
			return;
		}
		for (String opName : opInfo.names()) {
			if (!opDirectory.containsKey(opName)) opDirectory.put(opName,
				new TreeSet<>());
			boolean success = opDirectory.get(opName).add(opInfo);
			if (!success) System.out.println("Did not add OpInfo " + opInfo);
		}
	};

	private Set<OpInfo> opsOfName(final String name) {
		final Set<OpInfo> ops = opDirectory.getOrDefault(name, Collections
			.emptySet());
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
	public void setDefaultHints(Hints hints) {
		this.environmentHints = hints.copy();
	}

	@Override
	public Hints getDefaultHints() {
		if (this.environmentHints != null) return this.environmentHints.copy();
		return new DefaultHints();
	}

	@Override
	public Hints createHints(String... startingHints) {
		return new DefaultHints(startingHints);
	}

}
