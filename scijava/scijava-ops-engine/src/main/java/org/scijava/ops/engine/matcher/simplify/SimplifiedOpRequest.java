/*-
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

package org.scijava.ops.engine.matcher.simplify;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Computers.Arity1;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.api.Ops;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.BaseOpHints.Adaptation;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.types.Any;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;

public class SimplifiedOpRequest implements OpRequest {

	/** Name of the op, or null for any name. */
	private final String name;

	/** Type of the Op asked for by the simple request */
	private final Type type;

	/** Input types of the simple request */
	private final Type[] inTypes;

	/** Output type of the simple request */
	private final Type outType;

	private final OpRequest srcReq;
	private final List<RichOp<Function<?, ?>>> inSimplifiers;
	private final RichOp<Function<?, ?>> outFocuser;
	private final RichOp<Computers.Arity1<?, ?>> outputCopier;

	public SimplifiedOpRequest(OpRequest req, OpEnvironment env)
	{
		Hints h = new Hints(Adaptation.FORBIDDEN, Simplification.FORBIDDEN);
		this.name = req.getName();
		this.srcReq = req;
		// Find the simplifiers and focusers
		this.inSimplifiers = new ArrayList<>();
		for(Type arg: req.getArgs()) {
			var inNil = Nil.of(arg);
			var simplifier = env.unary("simplify", h).inType(inNil).outType(Object.class).function();
			inSimplifiers.add(Ops.rich(simplifier));
		}
		var inNil = Nil.of(new Any());
		var outNil = Nil.of(req.getOutType());
		var focuser = env.unary("focus", h).inType(inNil).outType(outNil).function();
		this.outFocuser = Ops.rich(focuser);

		// Determine the new type
		inTypes = simpleArgs(req.getArgs(), inSimplifiers);
		var info = Ops.info(outFocuser);
		Map<TypeVariable<?>, Type> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(new Type[] {info.outputType()}, new Type[] {req.getOutType()}, typeAssigns);
		outType = Types.mapVarToTypes(info.inputTypes().get(0), typeAssigns);

		type = SimplificationUtils.retypeOpType(req.getType(), inTypes, outType);
		// TODO: Fix
		int mutableIndex = SimplificationUtils.findMutableArgIndex(Types.raw(type));
		this.outputCopier = mutableIndex == -1 ? null : simplifierCopyOp(env, req.getArgs()[mutableIndex]);
	}

	private Type[] simpleArgs(Type[] args, List<RichOp<Function<?, ?>>> ops) {
		Type[] opIns = new Type[args.length];
		Type[] opOuts = new Type[args.length];
		for(int i = 0; i < opIns.length; i++) {
			var info = Ops.info(ops.get(i));
			opIns[i] = info.inputTypes().get(0);
			opOuts[i] = info.outputType();
		}

		Map<TypeVariable<?>, Type> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(opIns, args, typeAssigns);
		return Types.mapVarToTypes(opOuts, typeAssigns);
	}

	public OpRequest srcReq() {
		return srcReq;
	}

	public List<RichOp<Function<?, ?>>> inputSimplifiers() {
		return inSimplifiers;
	}

	public RichOp<Function<?, ?>> outputFocuser() {
		return outFocuser;
	}

	/**
	 * Finds a {@code copy} Op designed to copy an Op's output (of {@link Type}
	 * {@code copyType}) back into the preallocated output during simplification.
	 * <p>
	 * NB Simplification is forbidden here because we are asking for a
	 * {@code Computers.Arity1<T, T>} copy Op (for some {@link Type}
	 * {@code type}). Suppose that no direct match existed, and we tried to find a
	 * simplified version. This simplified version, because it is a
	 * Computers.Arity1, would need a {@link Computers.Arity1} copy Op to copy
	 * the output of the simplified Op back into the preallocated output. But this
	 * call is already identical to the Op we asked for, and we know that there is
	 * no direct match, thus we go again into simplification. This thus causes an
	 * infinite loop (and eventually a {@link StackOverflowError}. This means that
	 * we cannot find a simplified copy Op <b>unless a direct match can be
	 * found</b>, at which point we might as well just use the direct match.
	 * <p>
	 * Adaptation is similarly forbidden, as to convert most Op types to
	 * {@link Arity1} you would need an identical copy Op.
	 * <p>
	 *
	 * @param copyType - the {@link Type} that we need to be able to copy
	 * @return an {@code Op} able to copy data between {@link Object}s of
	 *         {@link Type} {@code copyType}
	 * @throws OpMatchingException
	 */
	private static RichOp<Computers.Arity1<?, ?>> simplifierCopyOp(OpEnvironment env, Type copyType) throws
			OpMatchingException
	{
		// prevent further simplification/adaptation
		Hints hints = new Hints(Adaptation.FORBIDDEN, Simplification.FORBIDDEN);
		Nil<?> copyNil = Nil.of(copyType);
		var op = env.unary("copy", hints).inType(copyNil).outType(copyNil).computer();
		return Ops.rich(op);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Type getOutType() {
		return outType;
	}

	@Override
	public Type[] getArgs() {
		return inTypes;
	}

	@Override
	public String getLabel() {
		return "Simplification of " + srcReq.getLabel();
	}

	@Override
	public boolean typesMatch(Type opType) {
		return typesMatch(opType, new HashMap<>());
	}

	/**
	 * Determines whether the specified type satisfies the op's required types
	 * using {@link Types#isApplicable(Type[], Type[])}.
	 */
	@Override
	public boolean typesMatch(final Type opType,
			final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		if (type == null) return true;
		if (type instanceof ParameterizedType) {
			if (!GenericAssignability.checkGenericAssignability(opType,
					(ParameterizedType) type, typeVarAssigns, true))
			{
				return false;
			}
		}

		return Types.isAssignable(opType, type);
	}

	@Override
	public String toString() {
		return requestString();
	}
}
