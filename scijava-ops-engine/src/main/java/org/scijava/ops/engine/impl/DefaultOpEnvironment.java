/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

import com.google.common.collect.TreeMultimap;
import org.scijava.common3.Any;
import org.scijava.common3.Types;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.ManualDiscoverer;
import org.scijava.meta.Versions;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.*;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.matcher.MatchingRoutine;
import org.scijava.ops.engine.matcher.OpCandidate;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.ops.engine.matcher.impl.DefaultOpMatcher;
import org.scijava.ops.engine.matcher.impl.DefaultOpRequest;
import org.scijava.ops.engine.matcher.impl.InfoMatchingOpRequest;
import org.scijava.ops.engine.matcher.impl.DefaultOpClassInfo;
import org.scijava.ops.engine.struct.FunctionalMethodType;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.priority.Priority;
import org.scijava.struct.ItemIO;
import org.scijava.types.*;
import org.scijava.types.extract.DefaultTypeReifier;
import org.scijava.types.extract.TypeReifier;
import org.scijava.types.infer.GenericAssignability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link OpEnvironment}, whose ops and related state
 * are discovered from a SciJava application context.
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public class DefaultOpEnvironment implements OpEnvironment {

	// A Discoverer used to discover backend plugins, like OpInfoGenerators
	private final Discoverer metaDiscoverer = Discoverer.union(Discoverer.all(
		ServiceLoader::load));

	private final List<Discoverer> discoverers = new ArrayList<>();

	private final ManualDiscoverer manDiscoverer = new ManualDiscoverer();

	private final OpMatcher matcher;

	private final TypeReifier typeReifier;

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
	 * Map containing pairs of {@link MatchingConditions} (i.e. the
	 * {@link OpRequest} and {@link Hints} used to find an Op) and the
	 * {@link OpInstance} (wrapping an Op with its backing {@link OpInfo}) that
	 * matched those requests. Used to quickly return Ops when the matching
	 * conditions are identical to those of a previous call.
	 *
	 * @see MatchingConditions#equals(Object)
	 */
	private final Map<MatchingConditions, OpInstance<?>> opCache =
		new HashMap<>();

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

	public DefaultOpEnvironment() {
		this(new Discoverer[0]);
	}

	public DefaultOpEnvironment(final Discoverer... discoverers) {
		typeReifier = new DefaultTypeReifier(metaDiscoverer);
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
		// prevents aliased Ops from appearing multiple times.
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
		for (Discoverer d : arr) {
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
		boolean adapting = hints.contains(BaseOpHints.Adaptation.IN_PROGRESS);
		boolean converting = hints.contains(BaseOpHints.Conversion.IN_PROGRESS);
		boolean depMatching = hints.contains(
			BaseOpHints.DependencyMatching.IN_PROGRESS);
		// if we aren't in any special matching situations, return all Ops
		if (!(adapting || converting || depMatching)) return infos;
		return infos.stream() //
			// filter out unadaptable ops
			.filter(info -> !adapting || !info.declaredHints().contains(
				BaseOpHints.Adaptation.FORBIDDEN)) //
			// filter out nonconvertible ops
			.filter(info -> !converting || !info.declaredHints().contains(
				BaseOpHints.Conversion.FORBIDDEN)) //
			// filter out ops that should not be dependencies
			.filter(info -> !depMatching || !info.declaredHints().contains(
				BaseOpHints.DependencyMatching.FORBIDDEN)) //
			.collect(Collectors.toCollection(TreeSet::new));
	}

	@Override
	public <T> T op( //
		final String opName, //
		final Nil<T> specialType, //
		final Nil<?>[] inTypes, //
		final Nil<?> outType, //
		Hints hints //
	) {
		return findOp(opName, specialType, inTypes, outType, hints).asOpType();
	}

	@Override
	public <T> T opFromInfoTree(final InfoTree tree, final Nil<T> specialType,
		Hints hints)
	{
		if (!(specialType.type() instanceof ParameterizedType))
			throw new IllegalArgumentException("TODO");
		@SuppressWarnings("unchecked")
		OpInstance<T> instance = (OpInstance<T>) tree.newInstance(specialType
			.type());
		var conditions = MatchingConditions.from( //
			new InfoMatchingOpRequest(tree.info(), Nil.of(tree.info().opType())), //
			hints //
		);
		RichOp<T> wrappedOp = wrapOp(instance, conditions);
		return wrappedOp.asOpType();
	}

	@Override
	public InfoTree treeFromSignature(String signature) {
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
		return typeReifier.reify(obj);
	}

	@Override
	public OpInfo opify(final Class<?> opClass, final double priority,
		String... names)
	{
		return new DefaultOpClassInfo( //
			opClass, //
			Versions.of(opClass), //
			"", //
			new Hints(), //
			priority, //
			names //
		);
	}

	@Override
	public <T> T typeLambda(Nil<T> opType, T lambda) {
		return LambdaTypeBaker.bakeLambdaType(lambda, opType.type());
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
	 *
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
		if (o instanceof OpInfo) infos = Collections.singletonList((OpInfo) o);
		else infos = generateAllInfos(o);
		infos.forEach(addToOpIndex);

		// Step 2: Discover "secondary" OpInfos e.g. ReducedOpInfos
		for (OpInfo info : infos) {
			generateAllInfos(info).forEach(addToOpIndex);
		}
	}

	@Override
	public String help(final OpRequest request) {
		Optional<OpDescriptionGenerator> opt = metaDiscoverer.discoverMax(
			OpDescriptionGenerator.class);
		if (opt.isEmpty()) {
			return "";
		}
		return opt.get().simpleDescriptions(this, request);
	}

	@Override
	public String helpVerbose(final OpRequest request) {
		Optional<OpDescriptionGenerator> opt = metaDiscoverer.discoverMax(
			OpDescriptionGenerator.class);
		if (opt.isEmpty()) {
			return "";
		}
		return opt.get().verboseDescriptions(this, request);
	}

	@SuppressWarnings("unchecked")
	private <T> RichOp<T> findOp(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType, Hints hints)
	{
		final OpRequest request = DefaultOpRequest.fromTypes(opName, specialType
			.type(), outType != null ? outType.type() : null, toTypes(inTypes));

		var conditions = MatchingConditions.from(request, hints);
		try {
			OpInstance<T> instance = (OpInstance<T>) generateCacheHit(conditions);
			return wrapOp(instance, conditions);
		}
		catch (OpMatchingException e) {
			// Report the full exception trace for debugging purposes
			log.debug("Op matching failed.", e);

			var debugText = "See debugging output for full failure report.";

			if (e instanceof DependencyMatchingException) {
				// Preserve the specificity of the match but point to the original
				// request instead of the dependency, and note the debug logging
				throw new DependencyMatchingException(
					"Error matching dependencies for request:\n\n" + request + "\n\n" +
						debugText);
			}

			var failedRequest = "\n\n" + request + "\n\n";

			// The directly suppressed exceptions will be from the individual matchers
			// Check here for special cases of match failure
			for (Throwable t : e.getSuppressed()) {
				// Duplicate ops detected
				if (t.getMessage().startsWith("Multiple") && t.getMessage().contains(
					"ops of priority"))
				{
					throw new OpMatchingException(
						"Multiple ops of equal priority detected for request:" +
							failedRequest + "\n" + debugText);
				}
			}

			var msg = helpVerbose(request);
			// If we have some alternatives we can suggest them here
			if (!msg.equals(OpDescriptionGenerator.NO_OP_MATCHES)) {
				msg = OpDescriptionGenerator.NO_OP_MATCHES + failedRequest +
					"Perhaps you meant one of these:\n" + msg + "\n\n";
			}
			else {
				// Otherwise, not much to report
				msg += failedRequest;
			}

			msg += debugText;

			throw new OpMatchingException(msg);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> OpInstance<T> findOp(final OpInfo info, final Nil<T> specialType,
		Hints hints) throws OpMatchingException
	{
		OpRequest request = new InfoMatchingOpRequest(info, specialType);

		// create new OpCandidate from request and info
		OpCandidate candidate = new OpCandidate(this, request, info);

		MatchingConditions conditions = MatchingConditions.from(request, hints);
		var instance = instantiateOp(candidate, conditions.hints());

		// cache instance
		setInstance(conditions, instance);
		return (OpInstance<T>) instance;
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils) //
			.filter(Objects::nonNull) //
			.map(Nil::type) //
			.toArray(Type[]::new);
	}

	/**
	 * Finds an Op instance matching the request described by {@link OpRequest}
	 * {@code request} and stores this Op in {@link #opCache}. NB the return must
	 * be an {@link Object} here (instead of some type variable T where T is the
	 * Op type) since there is no way to ensure that the {@link OpRequest} can
	 * provide that T (since the {@link OpRequest} could require that the Op
	 * returned is of multiple types).
	 *
	 * @param conditions the {@link MatchingConditions} defining a proper match
	 * @return the {@link OpInstance} generated to satisfy {@code conditions}
	 */
	private OpInstance<?> generateCacheHit(MatchingConditions conditions) {
		// see if the request has been matched already
		OpInstance<?> cachedOp = getInstance(conditions);
		if (cachedOp != null) return cachedOp;

		// obtain suitable OpCandidate
		var candidate = findOpCandidate(conditions.request(), conditions.hints());
		var instance = instantiateOp(candidate, conditions.hints());

		// cache instance
		setInstance(conditions, instance);
		return instance;
	}

	private OpInstance<?> getInstance(MatchingConditions conditions) {
		if (conditions.hints().contains(BaseOpHints.Cache.IGNORE)) {
			return null;
		}
		return opCache.get(conditions);
	}

	private void setInstance(MatchingConditions conditions,
		OpInstance<?> instance)
	{
		if (conditions.hints().contains(BaseOpHints.Cache.IGNORE)) {
			return;
		}
		opCache.put(conditions, instance);
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
		InfoTree adaptorTree = new DependencyRichOpInfoTree(candidate.opInfo(),
			conditions);
		return adaptorTree.newInstance(candidate.getType());
	}

	/**
	 * Wraps the matched op into an Op that knows its generic typing.
	 *
	 * @param instance - the {@link OpInstance} to wrap.
	 * @param conditions - the {@link Hints} used to create the {@link OpInstance}
	 * @return an Op wrapping of op.
	 */
	@SuppressWarnings("unchecked")
	private <T> RichOp<T> wrapOp(OpInstance<T> instance,
		MatchingConditions conditions) throws IllegalArgumentException
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
				log.debug("OpInfo " + info.implementationName() +
					" could not be wrapped as a " + rawType +
					", so it is instead wrapped as a " + wrapper +
					". If you want it to be wrapped as a " + rawType +
					", then you must define a new OpWrapper for that class!");
			}
			// obtain the generic type of the Op w.r.t. the Wrapper class
			Type reifiedSuperType = Types.superTypeOf(instance.type(), wrapper);
			// wrap the Op
			final OpWrapper<T> opWrapper = (OpWrapper<T>) wrappers.get(Types.raw(
				reifiedSuperType));
			return opWrapper.wrap(instance, this, conditions);
		}
		catch (IllegalArgumentException | SecurityException exc) {
			throw new IllegalArgumentException(exc.getMessage() != null ? exc
				.getMessage() : "Cannot wrap " + instance.op().getClass());
		}
		catch (NullPointerException e) {
			throw new IllegalArgumentException("No wrapper exists for " + Types.raw(
				instance.type()).toString() + ".");
		}
	}

	private Class<?> getWrapperClass(Class<?> opType) {
		if (opType == null) return null;
		// Check opType itself
		if (wrappers.containsKey(opType)) return opType;
		// Check superclass of opType
		Class<?> wrapperSuperClass = getWrapperClass(opType.getSuperclass());
		if (wrapperSuperClass != null) return wrapperSuperClass;
		// Check interfaces of opType
		for (Class<?> iFace : opType.getInterfaces()) {
			Class<?> wrapperIFace = getWrapperClass(iFace);
			if (wrapperIFace != null) return wrapperIFace;
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

		// All type variables mapped to Any must be removed before matching
		// dependencies. Consider the case of matching a request for a
		// Function<Any, Double>, which has matched an Op that is a
		// Function<List<N extends Number>, Double> with a Function<N, Double>
		// dependency. In this case, N maps to Any because List<N> maps to Any. We
		// cannot simply search for a Function<Any, Double> dependency, however,
		// because that might return a Function<Byte, Double> which would not work
		// for an arbitrary input of type <N extends Number>. Therefore, the correct
		// bound on the dependency is Function<N extends Number, Double> i.e. we
		// must ignore the Any.
		Map<TypeVariable<?>, Type> dependencyTypeVarAssigns = new HashMap<>();
		for (var entry : typeVarAssigns.entrySet()) {
			var value = entry.getValue();
			if (!Any.is(value)) {
				dependencyTypeVarAssigns.put(entry.getKey(), entry.getValue());
			}
		}
		// Let dependency matching hints contain all user hints, and additional
		// dependency-matching hints
		Hints baseDepHints = hints //
			.plus( //
				BaseOpHints.DependencyMatching.IN_PROGRESS, //
				BaseOpHints.Conversion.FORBIDDEN, //
				BaseOpHints.History.IGNORE //
			).minus(BaseOpHints.Progress.TRACK);
		// Then, match dependencies
		final List<RichOp<?>> dependencies = new ArrayList<>();
		for (final OpDependencyMember<?> dependency : Infos.dependencies(info)) {
			final OpRequest request = inferOpRequest(dependency,
				dependencyTypeVarAssigns);
			try {
				// match Op
				var depHints = baseDepHints.plus(dependency.hints());
				var conditions = MatchingConditions.from(request, depHints);
				OpInstance<?> instance = generateCacheHit(conditions);
				// add Op to dependencies
				dependencies.add(wrapOp(instance, conditions));
				// refine current type variable knowledge
				GenericAssignability //
					.inferTypeVariables(request.type(), instance.type()) //
					.forEach(dependencyTypeVarAssigns::putIfAbsent);

			}
			catch (final OpMatchingException e) {
				String message = DependencyMatchingException.message(info
					.implementationName(), dependency.key(), request);
				if (e instanceof DependencyMatchingException) {
					throw new DependencyMatchingException(message,
						(DependencyMatchingException) e);
				}
				throw new DependencyMatchingException(message);
			}
		}
		return dependencies;
	}

	private OpRequest inferOpRequest(OpDependencyMember<?> dependency,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		final Type mappedDependencyType = Types.unroll(
			new Type[] { dependency.type() }, typeVarAssigns)[0];
		final String dependencyName = dependency.getDependencyName();
		return inferOpRequest(mappedDependencyType, dependencyName, typeVarAssigns);
	}

	/**
	 * Tries to infer a {@link OpRequest} from a functional Op type. E.g. the
	 * type:
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
	 * @param typeVarAssigns the mappings of {@link TypeVariable}s to
	 *          {@link Type}s
	 * @throws OpMatchingException if {@code type} defines more than one output.
	 */
	private OpRequest inferOpRequest(Type type, String name,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		List<FunctionalMethodType> fmts = FunctionalParameters
			.findFunctionalMethodTypes(type);

		EnumSet<ItemIO> inIos = EnumSet.of(ItemIO.INPUT, ItemIO.CONTAINER,
			ItemIO.MUTABLE);
		EnumSet<ItemIO> outIos = EnumSet.of(ItemIO.OUTPUT, ItemIO.CONTAINER,
			ItemIO.MUTABLE);

		Type[] inputs = fmts.stream() //
			.filter(fmt -> inIos.contains(fmt.itemIO())) //
			.map(FunctionalMethodType::type) //
			.toArray(Type[]::new);

		Type[] outputs = fmts.stream() //
			.filter(fmt -> outIos.contains(fmt.itemIO())) //
			.map(FunctionalMethodType::type) //
			.toArray(Type[]::new);

		Type[] mappedInputs = Types.unroll(inputs, typeVarAssigns);
		Type[] mappedOutputs = Types.unroll(outputs, typeVarAssigns);

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
	 * convenience. If the user wishes to use {@link Hints} in a thread-safe
	 * manner, they should use
	 * {@link DefaultOpEnvironment#op(String, Nil, Nil[], Nil, Hints)} if using
	 * different {@link Hints} for different calls. Alternatively, this method can
	 * be called before all Ops called in parallel without issues.
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
	public double priority() {
		return Priority.VERY_LOW;
	}
}
