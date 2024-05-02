/*-
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.ops.api.*;
import org.scijava.ops.engine.*;
import org.scijava.ops.engine.OpCandidate.StatusCode;
import org.scijava.ops.engine.matcher.MatchingRoutine;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.ops.engine.matcher.impl.DefaultOpRequest;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.util.Infos;
import org.scijava.priority.Priority;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.types.Any;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;

public class AdaptationMatchingRoutine implements MatchingRoutine {

	@Override
	public void checkSuitability(MatchingConditions conditions)
		throws OpMatchingException
	{
		if (conditions.hints().containsAny( //
			BaseOpHints.Adaptation.IN_PROGRESS, //
			BaseOpHints.Adaptation.FORBIDDEN //
		)) //
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
		Hints adaptationHints = conditions.hints().plus( //
			BaseOpHints.Adaptation.IN_PROGRESS, //
			BaseOpHints.History.IGNORE //
		);
		List<Exception> matchingExceptions = new ArrayList<>();
		List<DependencyMatchingException> depExceptions = new ArrayList<>();
		for (final OpInfo adaptor : env.infos("engine.adapt")) {
			Type adaptTo = adaptor.output().getType();
			Map<TypeVariable<?>, Type> map = new HashMap<>();
			// make sure that the adaptor outputs the correct type
			if (!adaptOpOutputSatisfiesReqTypes(adaptTo, map, conditions.request()))
				continue;
			// make sure that the adaptor is a Function (so we can cast it later)
			if (Types.isInstance(adaptor.opType(), Function.class)) {
//				log.debug(adaptor + " is an illegal adaptor Op: must be a Function");
				continue;
			}

			try {
				// grab the first type parameter from the OpInfo and search for
				// an Op that will then be adapted (this will be the only input of the
				// adaptor since we know it is a Function)
				Type adaptFrom = adaptor.inputTypes().get(0);
				Type srcOpType = Types.substituteTypeVariables(adaptFrom, map);
				final OpRequest srcOpRequest = inferOpRequest(srcOpType, conditions
					.request().getName(), map);
				final OpCandidate srcCandidate = matcher.match(MatchingConditions.from(
					srcOpRequest, adaptationHints), env);
				// Then, once we've matched an Op, use the bounds of that match
				// to refine the bounds on the adaptor (for dependency matching)
				captureTypeVarsFromCandidate(adaptFrom, srcCandidate, map);
				// Finally, resolve the adaptor's dependencies
				List<InfoTree> depTrees = Infos.dependencies(adaptor).stream() //
					.map(d -> {
						OpRequest request = inferOpRequest(d, map);
						Nil<?> type = Nil.of(request.getType());
						Nil<?>[] args = Arrays.stream(request.getArgs()).map(Nil::of)
							.toArray(Nil[]::new);
						Nil<?> outType = Nil.of(request.getOutType());
						var op = env.op(request.getName(), type, args, outType,
							adaptationHints);
						// NB the dependency is interested in the INFOTREE of the match,
						// not the Op itself. We want to instantiate the dependencies
						// separately, so they can e.g. operate silently.
						return Ops.infoTree(op);
					}).collect(Collectors.toList());
				// And return the Adaptor, wrapped up into an OpCandidate
				Type adapterOpType = Types.substituteTypeVariables(adaptor.output()
					.getType(), map);
				InfoTree adaptorChain = new InfoTree(adaptor, depTrees);
				OpAdaptationInfo adaptedInfo = new OpAdaptationInfo(srcCandidate
					.opInfo(), adapterOpType, adaptorChain);
				OpCandidate adaptedCandidate = new OpCandidate(env, conditions
					.request(), adaptedInfo, map);
				adaptedCandidate.setStatus(StatusCode.MATCH);
				return adaptedCandidate;
			}
			catch (DependencyMatchingException d) {
				depExceptions.add(d);
				matchingExceptions.add(d);
			}
			catch (OpMatchingException | IllegalArgumentException e1) {
				matchingExceptions.add(e1);
			}
		}
		OpMatchingException agglomerated = new OpMatchingException(
			"Unable to find an Op adaptation for " + conditions);

		matchingExceptions.stream().forEach(agglomerated::addSuppressed);
		throw agglomerated;
	}

	/**
	 * Helper method that captures all type variable mappings found in the search
	 * for an Op that could satisfy an adaptor input {@code srcType}.
	 *
	 * @param srcType the type of the Op input to an adaptor
	 * @param candidate the {@link OpCandidate} matched for the adaptor input
	 * @param map the mapping
	 */
	private void captureTypeVarsFromCandidate(Type srcType, OpCandidate candidate,
		Map<TypeVariable<?>, Type> map)
	{
		Consumer<Map<TypeVariable<?>, Type>> typeVarConsumer = assigns -> {
			for (var key : assigns.keySet()) {
				if (map.containsKey(key)) {
					var existing = map.get(key);
					var replacement = assigns.get(key);
					// Ignore bounds that are weaker than current bounds.
					if (Types.isAssignable(existing, replacement) && !Any.is(existing)) {
						continue;
					}
				}
				map.put(key, assigns.get(key));
			}
		};
		// First, capture assignments between the Adaptor and the matched Op
		final Map<TypeVariable<?>, Type> srcBounds = new HashMap<>();
		GenericAssignability.inferTypeVariables(new Type[] { srcType }, new Type[] {
			candidate.getType() }, srcBounds);
		typeVarConsumer.accept(srcBounds);
		// Then, capture assignments between the original OpRef and the matched Op
		typeVarConsumer.accept(candidate.typeVarAssigns());
	}

	private OpRequest inferOpRequest(OpDependencyMember<?> dependency,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		final Type mappedDependencyType = Types.mapVarToTypes(new Type[] {
			dependency.getType() }, typeVarAssigns)[0];
		final String dependencyName = dependency.getDependencyName();
		final OpRequest inferred = inferOpRequest(mappedDependencyType,
			dependencyName, typeVarAssigns);
		if (inferred != null) return inferred;
		throw new OpMatchingException("Could not infer functional " +
			"method inputs and outputs of Op dependency field: " + dependency
				.getKey());
	}

	private boolean adaptOpOutputSatisfiesReqTypes(Type adaptTo,
		Map<TypeVariable<?>, Type> map, OpRequest request)
	{
		Type opType = request.getType();
		// TODO: clean this logic -- can this just be request.typesMatch() ?
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
	 * @param type
	 * @param name
	 * @return null if the specified type has no functional method
	 */
	private OpRequest inferOpRequest(Type type, String name,
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
		return new DefaultOpRequest(name, type, mappedOutputs[0], mappedInputs);
	}

	@Override
	public double priority() {
		return Priority.LOW;
	}

}
