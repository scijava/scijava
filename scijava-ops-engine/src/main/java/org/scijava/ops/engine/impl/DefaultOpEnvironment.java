/*
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;
import org.scijava.discovery.ManualDiscoverer;
import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.BaseOpHints.Adaptation;
import org.scijava.ops.engine.BaseOpHints.DependencyMatching;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.engine.DependencyMatchingException;
import org.scijava.ops.engine.InfoTreeGenerator;
import org.scijava.ops.engine.MatchingConditions;
import org.scijava.ops.engine.OpCandidate;
import org.scijava.ops.engine.OpDependencyMember;
import org.scijava.ops.engine.OpDescriptionGenerator;
import org.scijava.ops.engine.OpInfoGenerator;
import org.scijava.ops.engine.OpWrapper;
import org.scijava.ops.engine.matcher.MatchingRoutine;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.ops.engine.matcher.impl.DefaultOpMatcher;
import org.scijava.ops.engine.matcher.impl.DefaultOpRequest;
import org.scijava.ops.engine.matcher.impl.InfoMatchingOpRequest;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.priority.Priority;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.Nil;
import org.scijava.types.TypeReifier;
import org.scijava.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.TreeMultimap;

/**
 * Default implementation of {@link OpEnvironment}, whose ops and related state
 * are discovered from a SciJava application context.
 * 
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public class DefaultOpEnvironment implements OpEnvironment {

	// A Discoverer used to discover backend plugins, like OpInfoGenerators
	private final Discoverer metaDiscoverer = Discoverer.union(
			Discoverer.all(ServiceLoader::load)
	);

	private final List<Discoverer> discoverers = new ArrayList<>();

	private final ManualDiscoverer manDiscoverer = new ManualDiscoverer();

	private final OpMatcher matcher;

	private final TypeReifier typeService;

	private final OpHistory history;

	/**
	 * Data structure storing all known Ops, grouped by name. This reduces the
	 * search size for any Op request to the number of known Ops with the name
	 * given in the request.
	 */
	private final TreeMultimap<String, OpInfo> opDirectory = TreeMultimap.create();

	/**
	 * Data structure storing all known Ops, discoverable using their id.
	 */
	private Map<String, OpInfo> idDirectory;

	/**
	 * Map containing pairs of {@link MatchingConditions} (i.e. the {@link OpRequest}
	 * and {@link Hints} used to find an Op) and the {@link OpInstance} (wrapping an Op
	 * with its backing {@link OpInfo}) that matched those requests. Used to
	 * quickly return Ops when the matching conditions are identical to those of a
	 * previous call.
	 *
	 * @see MatchingConditions#equals(Object)
	 */
	private final Map<MatchingConditions, OpInstance<?>> opCache = new HashMap<>();

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

	private final Logger log = LoggerFactory.getLogger(getClass());

	public DefaultOpEnvironment(){
		this(new Discoverer[0]);
	}

	public DefaultOpEnvironment(final Discoverer... discoverers)
	{
		typeService = new DefaultTypeReifier(metaDiscoverer);
		history = OpHistory.getOpHistory();
		matcher = new DefaultOpMatcher( //
				metaDiscoverer.discover(MatchingRoutine.class) //
		);
		discoverUsing(discoverers);
		discoverUsing(manDiscoverer);
	}

	@Override
	public OpHistory history() {
		return history;
	}

	@Override
	public SortedSet<OpInfo> infos() {
		// NB distinct() prevents aliased Ops from appearing multiple times.
		return new TreeSet<>(opDirectory.values());
	}

	@Override
	public SortedSet<OpInfo> infos(String name) {
		if (name == null || name.isEmpty()) return infos();
		return opsOfName(name);
	}

	@Override
	public SortedSet<OpInfo> infos(Hints hints) {
		return filterInfos(infos(), hints);
	}

	@Override
	public SortedSet<OpInfo> infos(String name, Hints hints) {
		return filterInfos(infos(name), hints);
	}

	@Override
	public void discoverUsing(Discoverer... arr) {
		for (Discoverer d: arr) {
			discoverers.add(d);

			d.discover(OpInfo.class).forEach(this::registerInfosFrom);
			d.discover(Op.class).forEach(this::registerInfosFrom);
			d.discover(OpCollection.class).forEach(this::registerInfosFrom);
		}
	}

	@Override
	public void discoverEverything() {
		discoverUsing(metaDiscoverer);
	}

	private SortedSet<OpInfo> filterInfos(SortedSet<OpInfo> infos, Hints hints) {
		boolean adapting = hints.contains(Adaptation.IN_PROGRESS);
		boolean simplifying = hints.contains(Simplification.IN_PROGRESS);
		// if we aren't doing any
		if (!(adapting || simplifying)) return infos;
		return infos.stream() //
			// filter out unadaptable ops
			.filter(info -> !adapting || !info.declaredHints().contains(
				Adaptation.FORBIDDEN)) //
			// filter out unadaptable ops
			.filter(info -> !simplifying || !info.declaredHints().contains(
				Simplification.FORBIDDEN)) //
			.collect(Collectors.toCollection(TreeSet::new));
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
	public InfoTree infoTree(String opName, Nil<?> specialType,
		Nil<?>[] inTypes, Nil<?> outType)
	{
		return infoTree(opName, specialType, inTypes, outType, getDefaultHints());
	}

	@Override
	public InfoTree infoTree(String opName, Nil<?> specialType,
		Nil<?>[] inTypes, Nil<?> outType, Hints hints)
	{
		return findOp(opName, specialType, inTypes, outType, hints).infoTree();
	}

	@Override
	public InfoTree treeFromInfo(OpInfo info, Nil<?> specialType, Hints hints) {
		return findOp(info, specialType, hints).infoTree();
	}

	@Override
	public <T> T opFromSignature(final String signature,
		final Nil<T> specialType)
	{
		InfoTree info = treeFromID(signature);
		return opFromInfoChain(info, specialType);
	}

	@Override
	public <T> T opFromInfoChain(final InfoTree tree,
		final Nil<T> specialType)
	{
		if (!(specialType.getType() instanceof ParameterizedType))
			throw new IllegalArgumentException("TODO");
		@SuppressWarnings("unchecked")
		OpInstance<T> instance = (OpInstance<T>) tree.newInstance(specialType.getType());
		Hints hints = getDefaultHints();
		RichOp<T> wrappedOp = wrapOp(instance, hints);
		return wrappedOp.asOpType();

	}

	@Override
	public InfoTree treeFromID(String signature) {
		if (idDirectory == null) initIdDirectory();
		List<InfoTreeGenerator> infoTreeGenerators = discoverers.stream() //
				.flatMap(d -> d.discover(InfoTreeGenerator.class).stream()) //
				.collect(Collectors.toList());

		InfoTreeGenerator genOpt = InfoTreeGenerator.findSuitableGenerator(
			signature, infoTreeGenerators);
		return genOpt.generate(this, signature, idDirectory, infoTreeGenerators);
	}

	@Override
	public Type genericType(Object obj) {
		return typeService.reify(obj);
	}

	@Override
	public OpInfo opify(final Class<?> opClass, String... names) {
		return opify(opClass, Priority.NORMAL, names);
	}

	@Override
	public OpInfo opify(final Class<?> opClass, final double priority,
		String... names)
	{
		return new OpClassInfo(opClass, Versions.getVersion(opClass), new Hints(), priority,
			names);
	}

	@Override
	public <T> T bakeLambdaType(T op, Type type) {
		return LambdaTypeBaker.bakeLambdaType(op, type);
	}

	@Override
	public void register(Object... objects) {
		for (Object o : objects) {
			// Step 1: Register the Op with a discoverer
			if (o.getClass().isArray()) {
				register(o);
			}
			else if (o instanceof Iterable<?>) {
				((Iterable<?>) o).forEach(this::register);
			}
			else {
				manDiscoverer.register(o);
			}

			// Step 2: Register any Op infos within the Op Index
			registerInfosFrom(o);
		}
	}

	/**
	 * Discovers all {@link OpInfo}s from {@link Object} {@code o}. Note that
	 * {@code o} is usually a {@link Op}, an {@link OpCollection}, or an
	 * {@link OpInfo} (from which "secondary" {@link OpInfo}s will be generated).
	 * <p>
	 * NB We want to make sure that we can discover {@link OpInfoGenerator}s
	 * first, so we use {@link ServiceLoader#load(Class)} to load all of them
	 * </p>
	 * @param o the {@link Object} to derive {@link OpInfo}s from.
	 * @return all {@link OpInfo}s derived from {@code o}
	 */
	private List<OpInfo> generateAllInfos(Object o) {
		// Find all OpInfoGenerators
		return metaDiscoverer.discover(OpInfoGenerator.class) //
				.stream() //
				// Filter to the ones that can operate on o
				.filter(g -> g.canGenerateFrom(o)) //
				// Map to the set of OpInfos
				.flatMap(g -> g.generateInfosFrom(o).stream()) //
				.collect(Collectors.toList());
	}

	private void registerInfosFrom(Object o) {
		// Step 1: Discover "primary" OpInfos
		List<OpInfo> infos;
		if (o instanceof OpInfo)
			infos = Collections.singletonList((OpInfo) o);
		else
			infos = generateAllInfos(o);
		infos.forEach(addToOpIndex);

		// Step 2: Discover "secondary" OpInfos e.g. ReducedOpInfos, SimplifiedOpInfos
		for(OpInfo info: infos) {
			generateAllInfos(info).forEach(addToOpIndex);
		}
	}

	/**
	 * Helper method to get the descriptions for each {@link OpInfo} in
	 * {@code infos}
	 * <p>
	 * NB we return a {@link List} here to preserve multiple instances of the same
	 * {@link OpInfo}. This is consistent with {@link DefaultOpEnvironment#infos}
	 * returning multiple instances of the same {@link OpInfo}. The duplicate
	 * {@link OpInfo}s are created when Ops have multiple names.
	 * </p>
	 *
	 * @param request an {@link OpRequest}
	 * @return a {@link Set} of {@link String}s, one describing each
	 *         {@link OpInfo} in {@code infos}.
	 */
	@Override
	public String help(final OpRequest request) {
		Optional<OpDescriptionGenerator>
				opt = metaDiscoverer.discoverMin(OpDescriptionGenerator.class);
		if (opt.isEmpty()) {
			return "";
		}
		return opt.get().simpleDescriptions(this, request);
	}

	/**
	 * Helper method to get the descriptions for each {@link OpInfo} in
	 * {@code infos}
	 * <p>
	 * NB we return a {@link List} here to preserve multiple instances of the same
	 * {@link OpInfo}. This is consistent with {@link DefaultOpEnvironment#infos}
	 * returning multiple instances of the same {@link OpInfo}. The duplicate
	 * {@link OpInfo}s are created when Ops have multiple names.
	 *
	 * @param request an {@link OpRequest}
	 * @return a {@link Set} of {@link String}s, one describing each
	 *         {@link OpInfo} in {@code infos}.
	 */
	@Override
	public String helpVerbose(final OpRequest request) {
		Optional<OpDescriptionGenerator>
				opt = metaDiscoverer.discoverMin(OpDescriptionGenerator.class);
		if (opt.isEmpty()) {
			return "";
		}
		return opt.get().verboseDescriptions(this, request);
	}

	@SuppressWarnings("unchecked")
	private <T> RichOp<T> findOp(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType, Hints hints)
	{
		final OpRequest request = DefaultOpRequest.fromTypes(opName, specialType.getType(),
			outType != null ? outType.getType() : null, toTypes(inTypes));
		MatchingConditions conditions = generateCacheHit(request, hints);
		return (RichOp<T>) wrapViaCache(conditions);
	}

	@SuppressWarnings("unchecked")
	private <T> OpInstance<T> findOp(final OpInfo info, final Nil<T> specialType,
		Hints hints) throws OpMatchingException
	{
		OpRequest request = new InfoMatchingOpRequest(info, specialType);
		MatchingConditions conditions = insertCacheHit(request, hints, info);
		return (OpInstance<T>) getInstance(conditions);
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils) //
				.filter(Objects::nonNull) //
				.map(Nil::getType) //
				.toArray(Type[]::new);
	}

	/**
	 * Creates an Op instance from an {@link OpInfo} with the provided
	 * {@link MatchingConditions} as the guidelines for {@link OpInfo} selection.
	 * This Op instance is put into the {@link #opCache}, and is retrievable via
	 * {@link DefaultOpEnvironment#wrapViaCache(MatchingConditions)}
	 * 
	 * @param request the {@link OpRequest} request
	 * @param hints the {@link Hints} containing matching preferences
	 * @param info the {@link OpInfo} describing the Op that should match these
	 *          conditions1
	 * @return the {@link MatchingConditions} that will return the Op described by
	 *         {@code info} from the op cache
	 */
	private MatchingConditions insertCacheHit(final OpRequest request, //
		final Hints hints, //
		final OpInfo info //
	)	{
		MatchingConditions conditions = MatchingConditions.from(request, hints);

		// create new OpCandidate from request and info
		OpCandidate candidate = new OpCandidate(this, request, info);

		instantiateAndCache(conditions, candidate);

		return conditions;
	}

	/**
	 * Finds an Op instance matching the request described by {@link OpRequest}
	 * {@code request} and stores this Op in {@link #opCache}. NB the return must be an
	 * {@link Object} here (instead of some type variable T where T is the Op
	 * type) since there is no way to ensure that the {@link OpRequest} can provide
	 * that T (since the {@link OpRequest} could require that the Op returned is of multiple
	 * types).
	 * 
	 * @param request the {@link OpRequest} request
	 * @param hints the {@link Hints} containing matching preferences
	 * @return the {@link MatchingConditions} that will return the Op found from
	 *         the op cache
	 */
	private MatchingConditions generateCacheHit(OpRequest request, Hints hints) {
		MatchingConditions conditions = MatchingConditions.from(request, hints);
		// see if the request has been matched already
		OpInstance<?> cachedOp = getInstance(conditions);
		if (cachedOp != null) return conditions;

		// obtain suitable OpCandidate
		OpCandidate candidate = findOpCandidate(conditions.request(), conditions
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
		opCache.putIfAbsent(conditions, op);
	}

	private OpInstance<?> getInstance(MatchingConditions conditions) {
		return opCache.get(conditions);
	}

	private OpCandidate findOpCandidate(OpRequest request, Hints hints) {
		return matcher.match(MatchingConditions.from(request, hints), this);
	}

	/**
	 * Creates an instance of the Op from the {@link OpCandidate} <b>with its
	 * required {@link OpDependency} fields</b>.
	 * 
	 * @param candidate the {@link OpCandidate} to be instantiated
	 * @param hints the {@link Hints} to use in instantiation
	 * @return an Op with all needed dependencies
	 */
	private OpInstance<?> instantiateOp(final OpCandidate candidate,
		Hints hints)
	{
		final List<RichOp<?>> conditions = resolveOpDependencies(candidate, hints);
		InfoTree adaptorChain = new DependencyRichOpInfoTree(candidate
			.opInfo(), conditions);
		return adaptorChain.newInstance(candidate.getType());
	}

	/**
	 * Wraps the matched op into an Op that knows its generic typing.
	 * 
	 * @param instance - the {@link OpInstance} to wrap.
	 * @param hints - the {@link Hints} used to create the {@link OpInstance}
	 * @return an Op wrapping of op.
	 */
	@SuppressWarnings("unchecked")
	private <T> RichOp<T> wrapOp(OpInstance<T> instance, Hints hints)
		throws IllegalArgumentException
	{
		if (wrappers == null) initWrappers();

		try {
			// find the opWrapper that wraps this type of Op
			OpInfo info = instance.infoTree().info();
			Class<?> rawType = Types.raw(info.opType());
			Class<?> wrapper = getWrapperClass(rawType);
			if (wrapper == null) {
				throw new IllegalArgumentException(info.implementationName() +
					": matched op Type " + info.opType().getClass() +
					" does not match a wrappable Op type.");
			}
			if (!wrapper.equals(rawType)) {
				log.warn("OpInfo" + info.implementationName() +
					" could not be wrapped as a " + rawType +
					", so it is instead wrapped as a " + wrapper +
					". If you want it to be wrapped as a " + rawType +
					", then you must define a new OpWrapper for that class!");
			}
			// obtain the generic type of the Op w.r.t. the Wrapper class
			Type reifiedSuperType = Types.getExactSuperType(instance.getType(),
				wrapper);
			// wrap the Op
			final OpWrapper<T> opWrapper = (OpWrapper<T>) wrappers.get(Types.raw(
				reifiedSuperType));
			return opWrapper.wrap(instance, this, hints);
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

	private Class<?> getWrapperClass(Class<?> opType) {
		if (opType == null)
			return null;
		// Check opType itself
		if (wrappers.containsKey(opType))
			return opType;
		// Check superclass of opType
		Class<?> wrapperSuperClass = getWrapperClass(opType.getSuperclass());
		if (wrapperSuperClass != null)
			return wrapperSuperClass;
		// Check interfaces of opType
		for (Class<?> iFace: opType.getInterfaces()) {
			Class<?> wrapperIFace = getWrapperClass(iFace);
			if (wrapperIFace != null)
				return wrapperIFace;
		}
		// There is no wrapper
		return null;
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
		HashMap<Class<?>, OpWrapper<?>> tmp = new HashMap<>();
		for (Discoverer d : discoverers)
			for (OpWrapper wrapper : d.discover(OpWrapper.class))
					tmp.put(wrapper.type(), wrapper);
		wrappers = tmp;
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

		final List<RichOp<?>> dependencyChains = new ArrayList<>();

		for (final OpDependencyMember<?> dependency : Infos.dependencies(info)) {
			final OpRequest dep = inferOpRequest(dependency, typeVarAssigns);
			try {
				// TODO: Consider a new Hint implementation
				Hints hintsCopy = hints.plus(DependencyMatching.IN_PROGRESS,
					Simplification.FORBIDDEN);
				if (!dependency.isAdaptable()) {
					hintsCopy = hintsCopy.plus(Adaptation.FORBIDDEN);
				}

				MatchingConditions conditions = generateCacheHit(dep,
					hintsCopy);
				dependencyChains.add(wrapViaCache(conditions));
			}
			catch (final OpMatchingException e) {
				String message = DependencyMatchingException.message(info
					.implementationName(), dependency.getKey(), dep);
				if (e instanceof DependencyMatchingException) {
					throw new DependencyMatchingException(message,
						(DependencyMatchingException) e);
				}
				throw new DependencyMatchingException(message);
			}
		}
		return dependencyChains;
	}

	private OpRequest inferOpRequest(OpDependencyMember<?> dependency,
		Map<TypeVariable<?>, Type> typeVarAssigns) 
	{
		final Type mappedDependencyType = Types.mapVarToTypes(new Type[] {
			dependency.getType() }, typeVarAssigns)[0];
		final String dependencyName = dependency.getDependencyName();
		return inferOpRequest(mappedDependencyType, dependencyName,
			typeVarAssigns);
	}

	private RichOp<?> wrapViaCache(MatchingConditions conditions) {
		OpInstance<?> instance = getInstance(conditions);
		return wrap(instance, conditions.hints());
	}

	private RichOp<?> wrap(OpInstance<?> instance, Hints hints) {
		return wrapOp(instance, hints);
	}


	/**
	 * Tries to infer a {@link OpRequest} from a functional Op type. E.g. the type:
	 * 
	 * <pre>
	 * Computer&lt;Double[], Double[]&gt
	 * </pre>
	 * 
	 * Will result in the following {@link OpRequest}:
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
	 * @param type the functional {@link Type} of the {@code op} we're looking for
	 * @param name the name of the {@code op} we're looking for
	 * @param typeVarAssigns the mappings of {@link TypeVariable}s to {@link Type}s
	 * @throws OpMatchingException if {@code type} defines more than one output.
	 */
	private OpRequest inferOpRequest(Type type, String name, Map<TypeVariable<?>, Type> typeVarAssigns) {
		List<FunctionalMethodType> fmts = FunctionalParameters.findFunctionalMethodTypes(type);

		EnumSet<ItemIO> inIos = EnumSet.of(ItemIO.INPUT, ItemIO.CONTAINER, ItemIO.MUTABLE);
		EnumSet<ItemIO> outIos = EnumSet.of(ItemIO.OUTPUT, ItemIO.CONTAINER, ItemIO.MUTABLE);

		Type[] inputs = fmts.stream() //
				.filter(fmt -> inIos.contains(fmt.itemIO())) //
				.map(FunctionalMethodType::type) //
				.toArray(Type[]::new);

		Type[] outputs = fmts.stream() //
				.filter(fmt -> outIos.contains(fmt.itemIO())) //
				.map(FunctionalMethodType::type) //
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
		return new DefaultOpRequest(name, type, mappedOutputs[0], mappedInputs);
	}

	private synchronized void initIdDirectory() {
		if (idDirectory != null) return;
		idDirectory = new HashMap<>();

		opDirectory.values() //
				.forEach(info -> idDirectory.put(info.id(), info));
	}

	private final Consumer<OpInfo> addToOpIndex = (final OpInfo opInfo) -> {
		if (opInfo.names() == null || opInfo.names().isEmpty()) {
			log.error("Skipping Op " + opInfo.implementationName() + ":\n" +
				"Op implementation must provide name.");
			return;
		}
		for (String opName : opInfo.names()) {
			opDirectory.put(opName, opInfo);
		}
	};

	private SortedSet<OpInfo> opsOfName(final String name) {
		// NB distinct() prevents aliased Ops from appearing multiple times.
		return new TreeSet<>(opDirectory.get(name));
	}

	/**
	 * Sets the default {@link Hints} used for finding Ops.
	 * <p>
	 * Note that this method is <b>not</b> thread safe and is provided for
	 * convenience. If the user wishes to use {@link Hints} in a thread-safe manner,
	 * they should use
	 * {@link DefaultOpEnvironment#op(String, Nil, Nil[], Nil, Hints)} if using
	 * different {@link Hints} for different calls. Alternatively, this method can be
	 * called before all Ops called in parallel without issues.
	 */
	@Override
	public void setDefaultHints(Hints hints) {
		environmentHints = hints.copy();
	}

	@Override
	public Hints getDefaultHints() {
		if (environmentHints != null) return environmentHints.copy();
		return new Hints();
	}

	@Override
	public double getPriority() {
		return Priority.VERY_LOW;
	}
}
