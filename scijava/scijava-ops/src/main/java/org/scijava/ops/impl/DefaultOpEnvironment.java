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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.ops.BaseOpHints.Adaptation;
import org.scijava.ops.BaseOpHints.DependencyMatching;
import org.scijava.ops.BaseOpHints.Simplification;
import org.scijava.ops.Hints;
import org.scijava.ops.OpCandidate;
import org.scijava.ops.OpCandidate.StatusCode;
import org.scijava.ops.OpDependency;
import org.scijava.ops.OpDependencyMember;
import org.scijava.ops.OpEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.OpHistoryService;
import org.scijava.ops.OpInfo;
import org.scijava.ops.OpInstance;
import org.scijava.ops.OpMethod;
import org.scijava.ops.OpRef;
import org.scijava.ops.OpUtils;
import org.scijava.ops.OpWrapper;
import org.scijava.ops.api.Op;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.hint.AdaptationHints;
import org.scijava.ops.hint.DefaultHints;
import org.scijava.ops.hint.SimplificationHints;
import org.scijava.ops.matcher.DependencyMatchingException;
import org.scijava.ops.matcher.OpMatcher;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.impl.DefaultOpMatcher;
import org.scijava.ops.matcher.impl.DefaultOpRef;
import org.scijava.ops.matcher.impl.OpAdaptationInfo;
import org.scijava.ops.matcher.impl.OpClassInfo;
import org.scijava.ops.matcher.impl.OpFieldInfo;
import org.scijava.ops.matcher.impl.OpMethodInfo;
import org.scijava.ops.simplify.SimplifiedOpInfo;
import org.scijava.ops.struct.FunctionalParameters;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;
import org.scijava.types.TypeService;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;
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

	@Parameter
	private OpHistoryService history;

	/**
	 * Data structure storing all known Ops, grouped by name. This reduces the
	 * search size for any Op request to the number of known Ops with the name
	 * given in the request.
	 */
	private Map<String, Set<OpInfo>> opDirectory;

	/**
	 * Map containing pairs of {@link MatchingConditions} (i.e. the {@link OpRef}
	 * and {@Hints} used to find an Op) and the {@link OpInstance} (wrapping an Op
	 * with its backing {@link OpInfo}) that matched those requests. Used to
	 * quickly return Ops when the matching conditions are identical to those of a
	 * previous call.
	 *
	 * @see MatchingConditions#equals(Object)
	 */
	private Map<MatchingConditions, OpInstance> opCache;

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
	public <T> T op(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType)
	{
		return op(opName, specialType, inTypes, outType, getHints());
	}
	
	@Override
	public <T> T op(final OpInfo info, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType) {
		try {
			return findOp(info, specialType, inTypes, outType, getHints());
		} catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <T> T op(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType, Hints hints) {
		try {
			return findOp(opName, specialType, inTypes, outType, hints);
		} catch (OpMatchingException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public <T> T op(final OpInfo info, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType, Hints hints) {
		try {
			return findOp(info, specialType, inTypes, outType, hints);
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
	private <T> T findOp(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?> outType, Hints hints) {
		final OpRef ref = DefaultOpRef.fromTypes(opName, specialType.getType(), outType != null ? outType.getType() : null,
				toTypes(inTypes));
		MatchingConditions conditions = MatchingConditions.from(ref, hints, true);
		generateOpInstance(conditions);
		return (T) wrapViaCache(conditions, conditions.hints().executionChainID());
	}

	@SuppressWarnings("unchecked")
	private <T> T findOp(final OpInfo info, final Nil<T> specialType, final Nil<?>[] inTypes,
		final Nil<?> outType, Hints hints) throws OpMatchingException
	{
		OpRef ref = DefaultOpRef.fromTypes(specialType.getType(), outType.getType(),
			toTypes(inTypes));
		MatchingConditions conditions = MatchingConditions.from(ref, hints, true);
		generateOpInstance(conditions, info);
		return (T) wrapViaCache(conditions, conditions.hints().executionChainID());
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType()).toArray(Type[]::new);
	}

	/**
	 * Creates an Op instance from an {@link OpInfo} with the provided
	 * {@link MatchingConditions} as the guidelines for {@link OpInfo} selection.
	 * This Op instance is put into the {@code opCache}, and is retrievable via
	 * {@link DefaultOpEnvironment#wrapViaCache(MatchingConditions, UUID)}
	 * 
	 * @param conditions the {@link MatchingConditions} containing the
	 *          requirements for the returned Op
	 * @param info
	 * @throws OpMatchingException
	 */
	private void generateOpInstance(final MatchingConditions conditions, final OpInfo info) {
		// create new OpCandidate from ref and info
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		if (!conditions.ref().typesMatch(info.opType(), typeVarAssigns))
			throw new OpMatchingException(
				"The given OpRef and OpInfo are not compatible!");
		OpCandidate candidate = new OpCandidate(this, this.log, conditions.ref(), info,
			typeVarAssigns);
		// TODO: can this be replaced by simply setting the status code to match?
		if (!matcher.typesMatch(candidate)) throw new OpMatchingException(
			"The given OpRef and OpInfo are not compatible!");
		// obtain Op instance (with dependencies)
		Object op = instantiateOp(candidate, conditions.hints());

		// cache raw Op
		OpInstance instance = OpInstance.of(op, info, typeVarAssigns);
		cacheOp(conditions, instance);
	}
	
	private Object wrapViaCache(MatchingConditions conditions,
		UUID executionChainID)
	{
		OpInstance instance = getInstance(conditions);
		Object wrappedOp = wrapOp(instance.op(), instance.info(), conditions
			.hints(), executionChainID, instance.typeVarAssigns());
		if (!conditions.hints().containsHint(DependencyMatching.IN_PROGRESS))
			history.getHistory().logTopLevelWrapper(executionChainID, wrappedOp);
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
	 * @param conditions - the {@link MatchingConditions} outlining the
	 *          requirements that must be fulfilled by the Op returned
	 */
	private void generateOpInstance(final MatchingConditions conditions) 
	{
		// see if the ref has been matched already
		OpInstance cachedOp = getInstance(conditions);
		if (cachedOp != null) return;

		// obtain suitable OpCandidate
		OpCandidate candidate = findOpCandidate(conditions.ref(), conditions.hints());

		// obtain Op instance (with dependencies)
		Object op = instantiateOp(candidate, conditions.hints());
		
		// cache instance
		OpInstance instance = OpInstance.of(op, candidate.opInfo(), candidate.typeVarAssigns());
		cacheOp(conditions, instance);
	}

	private void cacheOp(MatchingConditions conditions, OpInstance op) {
		opCache.putIfAbsent(conditions, op);
	if (!conditions.hints().containsHint(DependencyMatching.IN_PROGRESS))
			history.getHistory().logTopLevelOp(conditions.hints().executionChainID(), op
				.op());
	}

	private OpInstance getInstance(MatchingConditions conditions) {
		if (opCache == null) {
			opCache = new HashMap<>();
		}
		return opCache.get(conditions);
	}
	
	private OpCandidate findOpCandidate(OpRef ref, Hints hints) {
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

	private OpCandidate findSimplifiedOp(OpRef ref, Hints hints) {
		Hints simplificationHints = SimplificationHints.generateHints(hints, false);
		return matcher.findSingleMatch(this, ref, simplificationHints);
	}

	/**
	 * Creates an instance of the Op from the {@link OpCandidate} <b>with its
	 * required {@link OpDependency} fields</b>.
	 * 
	 * @param candidate
	 * @return an Op with all needed dependencies
	 */
	private Object instantiateOp(final OpCandidate candidate, Hints hints)
	{
		final List<MatchingConditions> instances = resolveOpDependencies(candidate, hints);
		Object op = candidate.createOp(wrappedDeps(instances, hints.executionChainID()));
		history.getHistory().logDependencies(hints.executionChainID(), candidate.opInfo(), infos(instances));
		return op;
	}

	private List<Object> wrappedDeps(List<MatchingConditions> instances, UUID id) {
		return instances.stream().map(i -> wrapViaCache(i, id)).collect(Collectors.toList());
	}

	private List<OpInfo> infos(List<MatchingConditions> instances) {
		return instances.stream().map(i -> getInstance(i).info()).collect(Collectors.toList());
	}

	/**
	 * Wraps the matched op into an {@link Op} that knows its generic typing.
	 * 
	 * @param op - the op to wrap.
	 * @param opInfo - from which we determine the {@link Type} of the {@code Op}
	 *            
	 * @return an {@link Op} wrapping of op.
	 */
	@SuppressWarnings("unchecked")
	private <T> T wrapOp(T op, OpInfo opInfo, Hints hints, UUID executionID, Map<TypeVariable<?>, Type> typeVarAssigns) {
		// TODO: synchronize this
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
			final OpWrapper<T> opWrapper = (OpWrapper<T>) wrappers.get(Types.raw(reifiedSuperType));
			return opWrapper.wrap(op, opInfo, hints, history.getHistory(), executionID, reifiedSuperType);
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

	private List<MatchingConditions> resolveOpDependencies(OpCandidate candidate, Hints hints) {
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
	 */
	private List<MatchingConditions> resolveOpDependencies(OpInfo info, Map<TypeVariable<?>, Type> typeVarAssigns, Hints hints) {

		final List<OpDependencyMember<?>> dependencies = info.dependencies();
		final List<MatchingConditions> resolvedDependencies = new ArrayList<>(dependencies.size());

		for (final OpDependencyMember<?> dependency : dependencies) {
			final OpRef dependencyRef = inferOpRef(dependency, typeVarAssigns);
			try {
				// TODO: Consider a new Hint implementation
				Hints hintCopy = hints.getCopy(false);
				hintCopy.setHint(DependencyMatching.IN_PROGRESS);
				hintCopy.setHint(Simplification.FORBIDDEN);
				if(!dependency.isAdaptable()) {
					hintCopy.setHint(Adaptation.FORBIDDEN);
				}

				MatchingConditions conditions = MatchingConditions.from(dependencyRef, hintCopy, false);
				generateOpInstance(conditions);
				resolvedDependencies.add(conditions);
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

			if(adaptor instanceof SimplifiedOpInfo) {
				log.debug(adaptor + " has been simplified. This is likely a typo.");
			}

			try {
				// resolve adaptor dependencies
				final List<MatchingConditions> depConditions = resolveOpDependencies(adaptor,
					map, hints);
				final List<Object> dependencies = depConditions.stream() //
					.map(c -> wrapViaCache(c, hints.executionChainID())) //
					.collect(Collectors.toList());

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
	{
		Hints adaptationHints = AdaptationHints.generateHints(hints, false);
		final OpCandidate srcCandidate = findOpCandidate(srcOpRef, adaptationHints);
		return srcCandidate;
	}

	private boolean adaptOpOutputSatisfiesRefTypes(Type adaptTo, Map<TypeVariable<?>, Type> map, OpRef ref) {
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
	 * {@link ParameterStructs#findFunctionalMethodTypes(Type)}.
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
		if(this.environmentHints != null) return this.environmentHints.getCopy(false);
		return new DefaultHints();
	}

}
