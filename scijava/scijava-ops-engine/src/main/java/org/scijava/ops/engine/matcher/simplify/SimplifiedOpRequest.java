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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.scijava.function.Computers;
import org.scijava.function.Computers.Arity1;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.BaseOpHints.Adaptation;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.types.Nil;
import org.scijava.types.Types;

public class SimplifiedOpRequest implements OpRequest {

	/** Name of the op, or null for any name. */
	private final String name;

	/** Raw type of the request */
	private final Class<?> rawType;

	private final OpRequest srcReq;
	private final List<List<OpInfo>> simplifierSets;
	private final List<OpInfo> outputFocusers;
	private final Optional<InfoTree> copyOpChain;

	private SimplifiedOpRequest(OpRequest req, OpEnvironment env) {
		// TODO: this is probably incorrect
		this.name = req.getName();
		this.rawType = Types.raw(req.getType());
		this.srcReq = req;
		this.simplifierSets = SimplificationUtils.simplifyArgs(env, req.getArgs());
		this.outputFocusers = SimplificationUtils.getFocusers(env, req
			.getOutType());
		this.copyOpChain = Optional.empty();
	}

	private SimplifiedOpRequest(OpRequest req, OpEnvironment env,
		InfoTree copyOpChain)
	{
		this.name = req.getName();
		this.rawType = Types.raw(req.getType());
		this.srcReq = req;
		this.simplifierSets = SimplificationUtils.simplifyArgs(env, req.getArgs());
		this.outputFocusers = SimplificationUtils.getFocusers(env, req
			.getOutType());
		this.copyOpChain = Optional.of(copyOpChain);
	}

	public OpRequest srcReq() {
		return srcReq;
	}

	public Class<?> rawType() {
		return rawType;
	}

	public List<List<OpInfo>> simplifierSets() {
		return simplifierSets;
	}

	public List<OpInfo> outputFocusers() {
		return outputFocusers;
	}

	public Optional<InfoTree> copyOpChain() {
		return copyOpChain;
	}

	public static SimplifiedOpRequest simplificationOf(OpEnvironment env, OpRequest req,
		Hints hints)
	{
		Class<?> opType = Types.raw(req.getType());
		int mutableIndex = SimplificationUtils.findMutableArgIndex(opType);
		if (mutableIndex == -1) return new SimplifiedOpRequest(req, env);

		// if the Op's output is mutable, we will also need a copy Op for it.
		InfoTree copyOp = simplifierCopyOp(env, req
			.getArgs()[mutableIndex], hints);
		return new SimplifiedOpRequest(req, env, copyOp);
	}

	/**
	 * Finds a {@code copy} Op designed to copy an Op's output (of {@link Type}
	 * {@code copyType}) back into the preallocated output during simplification.
	 * <p>
	 * NB Simplification is forbidden here because we are asking for a
	 * {@code Computers.Arity1<T, T>} copy Op (for some {@link Type}
	 * {@code type}). Suppose that no direct match existed, and we tried to find a
	 * simplified version. This simplified version, because it is a
	 * Computers.Arity1, would need a {@lnk Computers.Arity<T, T>} copy Op to copy
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
	 * @param hints
	 * @return an {@code Op} able to copy data between {@link Object}s of
	 *         {@link Type} {@code copyType}
	 * @throws OpMatchingException
	 */
	private static InfoTree simplifierCopyOp(OpEnvironment env, Type copyType, Hints hints) throws
			OpMatchingException
	{
		// prevent further simplification/adaptation
		Hints hintsCopy = hints.copy() //
			.plus(Adaptation.FORBIDDEN, Simplification.FORBIDDEN);

		Nil<?> copyNil = Nil.of(copyType);
		Type copierType = Types.parameterize(Computers.Arity1.class, new Type[] {
			copyType, copyType });
		return env.infoTree("copy", Nil.of(copierType), new Nil<?>[] {
			copyNil, copyNil }, copyNil, hintsCopy);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		throw new UnsupportedOperationException(
			"The type of a SimplifiedOpRequest is indeterminate; it must be matched with a OpInfo to form a concrete Type");
	}

	@Override
	public Type getOutType() {
		throw new UnsupportedOperationException(
			"The output type of a SimplifiedOpRequest is indeterminate; it must be matched with a OpInfo to form a concrete Type");
	}

	@Override
	public Type[] getArgs() {
		throw new UnsupportedOperationException(
			"The output type of a SimplifiedOpRequest is indeterminate; it must be matched with a OpInfo to form a concrete Type");
	}

	@Override
	public String getLabel() {
		return "Simplification of " + srcReq.getLabel();
	}

	@Override
	public boolean typesMatch(Type opType) {
		return typesMatch(opType, new HashMap<>());
	}

	@Override
	public boolean typesMatch(Type opType,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		throw new UnsupportedOperationException(
			"The type of a SimplifiedOpRequest is indeterminate; it must be matched with an OpInfo to form a concrete Type!");
	}

}
