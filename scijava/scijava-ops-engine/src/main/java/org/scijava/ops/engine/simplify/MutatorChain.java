package org.scijava.ops.engine.simplify;

import java.lang.reflect.Type;

import org.scijava.ops.api.OpInfo;
import org.scijava.util.Types;

public class MutatorChain implements Comparable<MutatorChain>{

	private final OpInfo simplifier;
	private final OpInfo focuser;

	private final Type input;
	private final Type simple;
	private final Type unfocused;
	private final Type output;

	public MutatorChain(OpInfo simplifier,
		OpInfo focuser, TypePair ioTypes)
	{
		this.simplifier = simplifier;
		this.focuser = focuser;
		this.input = ioTypes.getA();
		this.output = ioTypes.getB();
		
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

	public OpInfo simplifier() {
		return simplifier;
	}

	public OpInfo focuser() {
		return focuser;
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