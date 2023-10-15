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
import java.util.function.Function;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.types.Nil;
import org.scijava.types.Types;

public class MutatorChain implements Comparable<MutatorChain>{

	private final OpInfo simplifier;
	private InfoTree simpleChain;
	private final OpInfo focuser;
	private InfoTree focusChain;

	private final Type input;
	private final Type simple;
	private final Type unfocused;
	private final Type output;

	private final OpEnvironment env;

	public MutatorChain(OpInfo simplifier,
		OpInfo focuser, TypePair ioTypes, OpEnvironment env)
	{
		this.simplifier = simplifier;
		this.simpleChain = null;
		this.focuser = focuser;
		this.focusChain = null;
		this.input = ioTypes.getA();
		this.output = ioTypes.getB();
		this.env = env;
		
		// determine simple and unfocused types.
		Type simplifierInput = simplifier.inputs().stream().filter(m -> !m
			.isOutput()).findFirst().get().getType();
		Type simplifierOutput = simplifier.output().getType();
		simple = SimplificationUtils.resolveMutatorTypeArgs(
			input, simplifierInput, simplifierOutput);
		Type focuserOutput = focuser.output().getType();
		Type focuserInput = focuser.inputs().stream().filter(m -> !m
			.isOutput()).findFirst().get().getType();
		unfocused = SimplificationUtils.resolveMutatorTypeArgs(
			output, focuserOutput, focuserInput);
	}

	public boolean isValid() {
		return Types.isAssignable(simple, unfocused);
	}

	public int numIdentitySimplifications() {
		boolean sIdentity = input.equals(simple);
		boolean fIdentity = unfocused.equals(output);
		return (sIdentity ? 1 : 0) + (fIdentity ? 1 : 0);
	}

	public InfoTree simplifier() {
		if (simpleChain == null) generateSimpleChain();
		return simpleChain;
	}

	private synchronized void generateSimpleChain() {
		if (simpleChain != null) return;
		Type[] typeArgs = { input, simple };
		Type specialType = Types.parameterize(Function.class, typeArgs);
		simpleChain = env.treeFromInfo(simplifier, Nil.of(specialType));
	}

	public InfoTree focuser() {
		if (focusChain == null) generateFocusChain();
		return focusChain;
	}

	private synchronized void generateFocusChain() {
		if (focusChain != null) return;
		Type[] typeArgs = { unfocused, output };
		Type specialType = Types.parameterize(Function.class, typeArgs);
		focusChain = env.treeFromInfo(focuser, Nil.of(specialType));
	}

	public Type inputType() {
		return input;
	}

	public Type simpleType() {
		return simple;
	}

	public Type unfocusedType() {
		return unfocused;
	}

	public Type outputType() {
		return output;
	}

	/**
	 * It is desirable for Identity functions to be used over non-Identity
	 * functions. Thus we should prioritize the {@link MutatorChain} with the
	 * largest number of identity simplifications. Iff two {@code MutatorChain}s
	 * have the same number of identity functions, do not care which is
	 * prioritized, and use {@link MutatorChain#hashCode()} as a fallback.
	 */
	@Override
	public int compareTo(MutatorChain that) {
		int identityDiff = this.numIdentitySimplifications() - that
			.numIdentitySimplifications();
		if (identityDiff != 0) return identityDiff;
		return this.hashCode() - that.hashCode();
	}

}
