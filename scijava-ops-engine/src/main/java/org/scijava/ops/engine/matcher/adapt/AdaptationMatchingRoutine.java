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

import org.scijava.common3.Any;
import org.scijava.common3.Types;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.MatchingConditions;
import org.scijava.ops.engine.OpDependencyMember;
import org.scijava.ops.engine.matcher.MatchingRoutine;
import org.scijava.ops.engine.matcher.OpCandidate;
import org.scijava.ops.engine.matcher.OpCandidate.StatusCode;
import org.scijava.ops.engine.matcher.OpMatcher;
import org.scijava.ops.engine.matcher.impl.DefaultOpRequest;
import org.scijava.ops.engine.struct.FunctionalMethodType;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.util.Infos;
import org.scijava.priority.Priority;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;
import org.scijava.types.infer.GenericAssignability;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        var adaptationHints = conditions.hints().plus( //
			BaseOpHints.Adaptation.IN_PROGRESS, //
			BaseOpHints.History.IGNORE //
		);
		List<Exception> matchingExceptions = new ArrayList<>();
		for (final var adaptor : env.infos("engine.adapt")) {
			var adaptTo = adaptor.output().type();
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
                var adaptFrom = adaptor.inputTypes().get(0);
                var srcOpType = Types.unroll(adaptFrom, map);
				final var srcOpRequest = inferOpRequest(srcOpType, conditions
					.request().name(), map);
				final var srcCandidate = matcher.match(MatchingConditions.from(
					srcOpRequest, adaptationHints), env);
				// Then, once we've matched an Op, use the bounds of that match
				// to refine the bounds on the adaptor (for dependency matching)
				captureTypeVarsFromCandidate(adaptFrom, srcCandidate, map);
				// Finally, resolve the adaptor's dependencies
                var depTrees = Infos.dependencies(adaptor).stream() //
					.map(d -> {
                        var request = inferOpRequest(d, map);
                        var type = Nil.of(request.type());
						Nil<?>[] args = Arrays.stream(request.argTypes()).map(Nil::of)
							.toArray(Nil[]::new);
                        var outType = Nil.of(request.outType());
						var op = env.op(request.name(), type, args, outType,
							adaptationHints);
						// NB the dependency is interested in the INFOTREE of the match,
						// not the Op itself. We want to instantiate the dependencies
						// separately, so they can e.g. operate silently.
						return Ops.infoTree(op);
					}).collect(Collectors.toList());
				// And return the Adaptor, wrapped up into an OpCandidate
                var adapterOpType = Types.unroll(adaptor.output()
					.type(), map);
                var adaptorTree = new InfoTree(adaptor, depTrees);
                var adaptedInfo = new OpAdaptationInfo(srcCandidate
					.opInfo(), adapterOpType, adaptorTree);
                var adaptedCandidate = new OpCandidate(env, conditions
					.request(), adaptedInfo, map);
				adaptedCandidate.setStatus(StatusCode.MATCH);
				return adaptedCandidate;
			}
			catch (OpMatchingException | IllegalArgumentException e1) {
				matchingExceptions.add(e1);
			}
		}
        var agglomerated = new OpMatchingException(
			"Unable to find an Op adaptation for " + conditions);

		matchingExceptions.forEach(agglomerated::addSuppressed);
		throw agglomerated;
	}

	/**
	 * Helper method that captures all type variable mappings found in the search
	 * for an Op that could satisfy an adaptor input {@code adaptorType}.
	 * <p>
	 * Notably, this method performs significant checks to ensure no {@link Any} objects
	 * are added to {@code map}.
	 * </p>
	 *
	 * @param adaptorType the type of the Op input to an adaptor
	 * @param candidate the {@link OpCandidate} matched for the adaptor input
	 * @param map the mapping
	 */
	private void captureTypeVarsFromCandidate(Type adaptorType, OpCandidate candidate,
		Map<TypeVariable<?>, Type> map)
	{

		// STEP 1: Base adaptor type variables
		// For example, let's say we're operating on Computer<I, O>s.
		// The adaptor might work on Computer<T, T>s. Which we need to resolve.
		// We can match them to the candidate's type, maybe a Computer<Integer, Any>.
		// But, to purge the Any we will have to fall back to the Info's type.
		// That process is performed below.
		final Type rawType = Types.parameterize(Types.raw(adaptorType));
		final Map<TypeVariable<?>, Type> adaptorBounds = new HashMap<>();
		GenericAssignability.inferTypeVariables(
				new Type[] {rawType},
				new Type[] {adaptorType},
				adaptorBounds
		);
		final Type infoType = Types.superTypeOf(candidate.opInfo().opType(), Types.raw(adaptorType));
		final Map<TypeVariable<?>, Type> infoBounds = new HashMap<>();
		GenericAssignability.inferTypeVariables(
				new Type[] {rawType},
				new Type[] {infoType},
				infoBounds
		);
		final Type candidateType = candidate.getType();
		final Map<TypeVariable<?>, Type> candidateBounds = new HashMap<>();
		GenericAssignability.inferTypeVariables(
				new Type[] {rawType},
				new Type[] {candidateType},
				candidateBounds
		);
		for (TypeVariable<?> tv: adaptorBounds.keySet()) {
			var adaptorBound = adaptorBounds.get(tv);
			if (adaptorBound instanceof TypeVariable) {
				TypeVariable<?> adaptTV = (TypeVariable<?>) adaptorBound;
				var candidateBound = candidateBounds.get(tv);
				if (hasAny(candidateBound)) {
					var infoBound = infoBounds.get(tv);
					map.put(adaptTV, infoBound);
				}
				else {
					map.put(adaptTV, candidateBound);
				}

			}
		}

		// STEP 2: Additional candidate mappings
		// Sometimes, there are additional type variables within the candidate
		// that don't pertain to the adaptor itself, but must still be
		// preserved for e.g. dependency matching.
		for (var entry : candidate.typeVarAssigns().entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			if (map.containsKey(key)) {
				var existing = map.get(key);
				// Ignore bounds that are weaker than current bounds.
				if (Types.isAssignable(existing, value) && !hasAny(existing)) {
					continue;
				}
			}
			if (!hasAny(value)) {
				map.put(key, value);
			}
		}
	}


	/**
	 * Returns {@code true} iff {@code t} is:
	 * <ol>
	 * <li>an {@link Any}</li>
	 * <li>a {@link Type} that is bounded in some way by an {@link Any}</li>
	 * </ol>
	 *
	 * @param t some {@link Type}
	 * @return {@code true} iff {@code t} is an {@link Any}
	 */
	private boolean hasAny(Type t) {
		if (Any.is(t)) return true;
		// NB Classes handled in Any.is call
		if (t instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) t;
			for(Type arg: pt.getActualTypeArguments()) {
				if (hasAny(arg)) {
					return true;
				}
			}
		}
		return false;
	}

	private OpRequest inferOpRequest(OpDependencyMember<?> dependency,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		final var mappedDependencyType = Types.unroll(new Type[] {
			dependency.type() }, typeVarAssigns)[0];
		final var dependencyName = dependency.getDependencyName();
		final var inferred = inferOpRequest(mappedDependencyType,
			dependencyName, typeVarAssigns);
		if (inferred != null) return inferred;
		throw new OpMatchingException("Could not infer functional " +
			"method inputs and outputs of Op dependency field: " + dependency
				.key());
	}

	private boolean adaptOpOutputSatisfiesReqTypes(Type adaptTo,
		Map<TypeVariable<?>, Type> map, OpRequest request)
	{
        var opType = request.type();
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
        var fmts = FunctionalParameters
			.findFunctionalMethodTypes(type);
		if (fmts == null) return null;

        var inIos = EnumSet.of(ItemIO.INPUT, ItemIO.CONTAINER,
			ItemIO.MUTABLE);
        var outIos = EnumSet.of(ItemIO.OUTPUT, ItemIO.CONTAINER,
			ItemIO.MUTABLE);

        var inputs = fmts.stream().filter(fmt -> inIos.contains(fmt.itemIO()))
			.map(fmt -> fmt.type()).toArray(Type[]::new);

        var outputs = fmts.stream().filter(fmt -> outIos.contains(fmt.itemIO()))
			.map(fmt -> fmt.type()).toArray(Type[]::new);

        var mappedInputs = Types.unroll(inputs, typeVarAssigns);
        var mappedOutputs = Types.unroll(outputs, typeVarAssigns);

		final var numOutputs = mappedOutputs.length;
		if (numOutputs != 1) {
            var error = "Op '" + name + "' of type " + type + " specifies ";
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
