package org.scijava.ops.engine.simplify;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.types.Nil;
import org.scijava.util.Types;

public class MutatorChain implements Comparable<MutatorChain>{

	private final OpInfo simplifier;
	private InfoChain simpleChain;
	private final OpInfo focuser;
	private InfoChain focusChain;

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

	public InfoChain simplifier() {
		if (simpleChain == null) generateSimpleChain();
		return simpleChain;
	}

	private synchronized void generateSimpleChain() {
		if (simpleChain != null) return;
		Type specialType = Types.parameterize(Function.class, input, simple);
		simpleChain = env.chainFromInfo(simplifier, Nil.of(specialType));
	}

	public InfoChain focuser() {
		if (focusChain == null) generateFocusChain();
		return focusChain;
	}

	private synchronized void generateFocusChain() {
		if (focusChain != null) return;
		Type specialType = Types.parameterize(Function.class, unfocused, output);
		focusChain = env.chainFromInfo(focuser, Nil.of(specialType));
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