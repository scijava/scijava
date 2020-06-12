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
import org.scijava.log.LogService;
import org.scijava.ops.OpDependency;
import org.scijava.ops.OpDependencyMember;
import org.scijava.ops.OpEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.OpInfo;
import org.scijava.ops.OpUtils;
import org.scijava.ops.adapt.AdaptedOp;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.matcher.DefaultOpMatcher;
import org.scijava.ops.matcher.MatchingUtils;
import org.scijava.ops.matcher.OpAdaptationInfo;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpClassInfo;
import org.scijava.ops.matcher.OpFieldInfo;
import org.scijava.ops.matcher.OpMatcher;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.OpRef;
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
	 * Map to collect all aliases for a specific op. All aliases will map to one
	 * canonical name of the op which is defined as the first one.
	 */
	private Map<String, Set<OpInfo>> opCache;

	private Map<Class<?>, OpWrapper<?>> wrappers;

	public DefaultOpEnvironment(final Context context) {
		context.inject(this);
		matcher = new DefaultOpMatcher(log);
	}

	@Override
	public Iterable<OpInfo> infos() {
		if (opCache == null) initOpCache();
		return opCache.values().stream().flatMap(list -> list.stream()).collect(Collectors.toList());
	}

	@Override
	public Iterable<OpInfo> infos(String name) {
		if (opCache == null) initOpCache();
		if (name == null || name.isEmpty()) return infos();
		return opsOfName(name);
	}

	@Override
	public <T> T op(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType) {
		return findOpInstance(opName, specialType, inTypes, outType);
	}

	@Override
	public Type genericType(Object obj) {
		return typeService.reify(obj);
	}

	@Override
	public <T> T opify(final T op, Type reifiedType) {
		if (wrappers == null) initWrappers();
		@SuppressWarnings("unchecked")
		final OpWrapper<T> wrapper = (OpWrapper<T>) wrappers.get(Types.raw(reifiedType));
		return wrapper.wrap(op, reifiedType);
	}

	@SuppressWarnings("unchecked")
	private <T> T findOpInstance(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?> outType) {
		final OpRef ref = OpRef.fromTypes(opName, toTypes(specialType), outType != null ? outType.getType() : null,
				toTypes(inTypes));
		return (T) findOpInstance(opName, ref, true);
	}

	private Object findOpInstance(final String opName, final OpRef ref, boolean adaptable) {
		Object op = null;
		OpCandidate match = null;
		AdaptedOp adaptation = null;
		try {
			// Find single match which matches the specified types
			match = matcher.findSingleMatch(this, ref);
			final List<Object> dependencies = resolveOpDependencies(match);
			op = match.createOp(dependencies);
		} catch (OpMatchingException e) {
			log.debug("No matching Op for request: " + ref + "\n");
			if (!adaptable) {
				throw new IllegalArgumentException(opName + " cannot be adapted (adaptation is disabled)");
			}
			log.debug("Attempting Op adaptation...");
			try {
				adaptation = adaptOp(ref);
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
		OpAdaptationInfo adaptedInfo = adaptation == null ? null : adaptation.opInfo();
		Object wrappedOp = wrapOp(op, match, adaptedInfo);
		return wrappedOp;
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType()).toArray(Type[]::new);
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

	/**
	 * Adapts an Op with the name of ref into a type that can be SAFELY cast to ref.
	 * 
	 * @param ref
	 *            - the type of Op that we are looking to adapt to.
	 * @return {@link AdaptedOp} - an Op that has been adapted to conform the the
	 *         ref type.
	 * @throws OpMatchingException
	 */
	private AdaptedOp adaptOp(OpRef ref) throws OpMatchingException {
		// FIXME: Need to validate against all types, not just the first.
		final Type opType = ref.getTypes()[0];

		for (final OpInfo adaptor : infos("adapt")) {
			Type adaptTo = adaptor.output().getType();
			Map<TypeVariable<?>, Type> map = new HashMap<>();
			// make sure that the adaptor outputs the correct type
			if (opType instanceof ParameterizedType) {
				if (!MatchingUtils.checkGenericAssignability(adaptTo,
					(ParameterizedType) opType, map, true))
				{
					continue;
				}
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
			OpCandidate candidate = new OpCandidate(this, log, adaptorRef, adaptor, map);

			try {
				// resolve adaptor dependencies and get the adaptor (as a function)
				final List<Object> dependencies = resolveOpDependencies(candidate);
				Object adaptorOp = adaptor.createOpInstance(dependencies).object();

				// grab the first type parameter (from the OpCandidate?) and search for an Op
				// that will then be adapted (this will be the first (only) type in the args of
				// the adaptor)
				Type srcOpType = Types.substituteTypeVariables(adaptor.inputs().get(0).getType(), map);
				final OpRef srcOpRef;
					srcOpRef = inferOpRef(srcOpType, ref.getName(), map);
				// TODO: export this to another function (also done in findOpInstance).
				// We need this here because we need to grab the OpInfo. 
				// TODO: is there a better way to do this?
				final OpCandidate srcCandidate = matcher.findSingleMatch(this, srcOpRef);
				final List<Object> srcDependencies = resolveOpDependencies(srcCandidate);
				final Object fromOp = srcCandidate.opInfo().createOpInstance(srcDependencies).object();

				// get adapted Op by applying adaptor on unadapted Op, then return
				// TODO: can we make this safer?
				@SuppressWarnings("unchecked")
				Object toOp = ((Function<Object, Object>) adaptorOp).apply(fromOp);
				// construct type of adapted op
				Type adapterOpType = Types.substituteTypeVariables(adaptor.output().getType(),
						map);
				return new AdaptedOp(toOp, adapterOpType, srcCandidate.opInfo(), adaptor);
			} catch (OpMatchingException e1) {
				log.trace(e1);
			}
		}

		// no adaptors available.
		throw new OpMatchingException("Op adaptation failed: no adaptable Ops of type " + ref.getName());
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
	 * @param adaptationInfo
	 *            - where to retrieve the {@link OpInfo} if a transformation is
	 *            needed.
	 * @return an {@link Op} wrapping of op.
	 */
	private Object wrapOp(Object op, OpCandidate match, OpAdaptationInfo adaptationInfo) {
		if (wrappers == null)
			initWrappers();

		OpInfo opInfo = match == null ? adaptationInfo : match.opInfo();
		// FIXME: this type is not necessarily Computer, Function, etc. but often
		// something more specific (like the class of an Op).
		// TODO: Is this correct?
		Type type = match == null ? adaptationInfo.opType() : match.getRef().getTypes()[0];
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
			if (!Types.isAssignable(Types.raw(type), suitableWrappers[0]))
				throw new IllegalArgumentException(Types.raw(type) + "cannot be wrapped as a " + suitableWrappers[0].getClass());
			return opify(op, type);
		} catch (IllegalArgumentException | SecurityException exc) {
			log.error(exc.getMessage() != null ? exc.getMessage() : "Cannot wrap " + op.getClass());
			return op;
		} catch (NullPointerException e) {
			log.error("No wrapper exists for " + Types.raw(type).toString() + ".");
			return op;
		}
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
				opCache.put(opName, new TreeSet<>());
			opCache.get(opName).add(opInfo);
		}
	}

	private Set<OpInfo> opsOfName(final String name) {
		final Set<OpInfo> ops = opCache.getOrDefault(name, Collections.emptySet());
		return Collections.unmodifiableSet(ops);
	}

}
