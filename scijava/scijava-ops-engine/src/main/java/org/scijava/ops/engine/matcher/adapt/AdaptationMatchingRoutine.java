
package org.scijava.ops.engine.matcher.adapt;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.engine.OpCandidate;
import org.scijava.ops.engine.OpCandidate.StatusCode;
import org.scijava.ops.api.OpDependencyMember;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.api.features.BaseOpHints.Adaptation;
import org.scijava.ops.engine.DependencyMatchingException;
import org.scijava.ops.api.features.MatchingConditions;
import org.scijava.ops.engine.matcher.MatchingRoutine;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.ops.api.features.OpMatchingException;
import org.scijava.ops.engine.matcher.impl.DefaultOpRef;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.priority.Priority;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;

public class AdaptationMatchingRoutine implements MatchingRoutine {

	@Override
	public void checkSuitability(MatchingConditions conditions)
		throws OpMatchingException
	{
		if (conditions.hints().containsAny(Adaptation.IN_PROGRESS,
			Adaptation.FORBIDDEN)) //
			throw new OpMatchingException(
				"Adaptation is not suitable: Adaptation is disabled");
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
	 * <li>The dependencies will exist for a particular {@code adapt} Op</li>
	 * <li>The Op we want exists with the correct type for the input of the
	 * {@code adapt} Op.</li>
	 * </ul>
	 * 
	 * @param conditions the {@link MatchingConditions} the return must satisfy
	 * @param matcher the {@link OpMatcher} performing the matching
	 * @param env the {@link OpEnvironment} containing matchable Ops
	 * @return an {@link OpCandidate} describing the match
	 * @throws OpMatchingException when no match can be found
	 */
	@Override
	public OpCandidate findMatch(MatchingConditions conditions, OpMatcher matcher,
		OpEnvironment env) throws OpMatchingException
	{
		Hints adaptationHints = conditions.hints().plus(Adaptation.IN_PROGRESS);
		List<DependencyMatchingException> depExceptions = new ArrayList<>();
		for (final OpInfo adaptor : env.infos("adapt")) {
			Type adaptTo = adaptor.output().getType();
			Map<TypeVariable<?>, Type> map = new HashMap<>();
			// make sure that the adaptor outputs the correct type
			if (!adaptOpOutputSatisfiesRefTypes(adaptTo, map, conditions.ref()))
				continue;
			// make sure that the adaptor is a Function (so we can cast it later)
			if (Types.isInstance(adaptor.opType(), Function.class)) {
//				log.debug(adaptor + " is an illegal adaptor Op: must be a Function");
				continue;
			}

			try {
				// resolve adaptor dependencies
				final Map<TypeVariable<?>, Type> adaptorBounds = new HashMap<>();
				final Map<TypeVariable<?>, Type> dependencyBounds = new HashMap<>();
				List<InfoChain> depChains = adaptor.dependencies().stream().map(d -> {
					OpRef ref = inferOpRef(d, map);
					Nil<?> type = Nil.of(ref.getType());
					Nil<?>[] args = Arrays.stream(ref.getArgs()).map(Nil::of).toArray(
						Nil[]::new);
					Nil<?> outType = Nil.of(ref.getOutType());
					InfoChain chain = env.infoChain(ref.getName(), type, args, outType,
						adaptationHints);
					// Check if the bounds of the dependency can inform the type of the
					// adapted Op
					final Type matchedOpType = chain.info().opType();
					// Find adaptor type variable bounds fulfilled by matched Op
					GenericAssignability.inferTypeVariables( //
						new Type[] { d.getType() }, //
						new Type[] { matchedOpType }, //
						dependencyBounds //
					);
					for (TypeVariable<?> typeVar : map.keySet()) {
						// Ignore TypeVariables not present in this particular dependency
						if (!dependencyBounds.containsKey(typeVar)) continue;
						Type matchedType = dependencyBounds.get(typeVar);
						// Resolve any type variables from the dependency ref that we can
						GenericAssignability.inferTypeVariables( //
							new Type[] { ref.getType() }, //
							new Type[] { matchedOpType }, //
							adaptorBounds //
						);
						Type mapped = Types.mapVarToTypes(matchedType, adaptorBounds);
						// If the type variable is more specific now, update it
						if (mapped != null && Types.isAssignable(mapped, map.get(typeVar))) {
							map.put(typeVar, mapped);
						}
					}
					dependencyBounds.clear();
					return chain;
				}).collect(Collectors.toList());
				InfoChain adaptorChain = new InfoChain(adaptor, depChains);

				// grab the first type parameter from the OpInfo and search for
				// an Op that will then be adapted (this will be the only input of the
				// adaptor since we know it is a Function)
				Type srcOpType = Types.substituteTypeVariables(adaptor.inputs().get(0)
					.getType(), map);
				final OpRef srcOpRef = inferOpRef(srcOpType, conditions.ref().getName(),
					map);
				final OpCandidate srcCandidate = matcher.match(MatchingConditions.from(
					srcOpRef, adaptationHints), env);
				map.putAll(srcCandidate.typeVarAssigns());
				Type adapterOpType = Types.substituteTypeVariables(adaptor.output()
					.getType(), map);
				OpAdaptationInfo adaptedInfo = new OpAdaptationInfo(srcCandidate
					.opInfo(), adapterOpType, adaptorChain);
				OpCandidate adaptedCandidate = new OpCandidate(env, conditions.ref(),
					adaptedInfo, map);
				adaptedCandidate.setStatus(StatusCode.MATCH);
				return adaptedCandidate;
			}
			catch (DependencyMatchingException d) {
				depExceptions.add(d);
			}
			catch (OpMatchingException | IllegalArgumentException e1) {
//				log.trace(e1);
			}
		}
		throw new OpMatchingException("Unable to find an Op adaptation for " +
			conditions);
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
	private OpRef inferOpRef(Type type, String name,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		List<FunctionalMethodType> fmts = FunctionalParameters
			.findFunctionalMethodTypes(type);
		if (fmts == null) return null;

		EnumSet<ItemIO> inIos = EnumSet.of(ItemIO.INPUT, ItemIO.CONTAINER,
			ItemIO.MUTABLE);
		EnumSet<ItemIO> outIos = EnumSet.of(ItemIO.OUTPUT, ItemIO.CONTAINER,
			ItemIO.MUTABLE);

		Type[] inputs = fmts.stream().filter(fmt -> inIos.contains(fmt.itemIO()))
			.map(fmt -> fmt.type()).toArray(Type[]::new);

		Type[] outputs = fmts.stream().filter(fmt -> outIos.contains(fmt.itemIO()))
			.map(fmt -> fmt.type()).toArray(Type[]::new);

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

	@Override
	public double priority() {
		return Priority.LOW;
	}

}
