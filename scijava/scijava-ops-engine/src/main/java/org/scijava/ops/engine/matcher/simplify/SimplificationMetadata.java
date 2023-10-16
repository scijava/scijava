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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.function.Computers;
import org.scijava.function.Computers.Arity1;
import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpInfo;
import org.scijava.struct.Member;
import org.scijava.types.Types;

/**
 * Compiles a list of all needed Mutators (either Simplifiers or Focuser
 * {@link Function}s) for a given simplified Op.
 * 
 * @author Gabriel Selzer
 */
public class SimplificationMetadata {

	private final OpInfo info;
	private final Class<?> opType;
	private final MutatorChain[] argChains;
	private final MutatorChain outChain;

	private final List<InfoTree> reqSimplifiers;
	private final List<Function<?, ?>> inputSimplifiers;
	private final List<InfoTree> infoFocusers;
	private final List<Function<?, ?>> inputFocusers;
	private final InfoTree infoSimplifier;
	private final Function<?, ?> outputSimplifier;
	private final InfoTree reqFocuser;
	private final Function<?, ?> outputFocuser;

	private final Optional<InfoTree> copyOpChain;

	private final int numInputs;

	public SimplificationMetadata(SimplifiedOpRequest req, OpInfo info, TypePair[] argPairs, TypePair outPair, Map<TypePair, MutatorChain> mutators)
	{
		this.info = info;
		this.opType = Types.raw(info.opType());
		this.argChains = Arrays.stream(argPairs).map(mutators::get).toArray(MutatorChain[]::new);
		this.outChain = mutators.get(outPair);

		this.reqSimplifiers = Arrays.stream(argChains).map(MutatorChain::simplifier).collect(Collectors.toList());
		this.inputSimplifiers = inputSimplifiers(this.reqSimplifiers);

		this.infoFocusers = Arrays.stream(argChains).map(MutatorChain::focuser).collect(Collectors.toList());
		this.inputFocusers = inputFocusers(this.infoFocusers);

		this.infoSimplifier = outChain.simplifier();
		this.outputSimplifier = outputSimplifier(this.infoSimplifier);

		this.reqFocuser = outChain.focuser();
		this.outputFocuser = outputFocuser(this.reqFocuser);

		this.copyOpChain = req.copyOpChain();

		if (reqSimplifiers.size() != infoFocusers.size())
			throw new IllegalArgumentException(
				"Invalid SimplificationMetadata for Op - incompatible number of input simplifiers and focusers");
		numInputs = reqSimplifiers.size();
	}

	public SimplificationMetadata(OpInfo info, List<InfoTree> reqSimplifiers, List<InfoTree> infoFocusers, InfoTree infoSimplifier, InfoTree reqFocuser, Optional<InfoTree> outputCopier) {
		this.info = info;
		this.opType = Types.raw(info.opType());
		
		this.reqSimplifiers = reqSimplifiers;
		this.inputSimplifiers = reqSimplifiers.stream().map(chain -> (Function<?, ?>) chain.newInstance().op()).collect(Collectors.toList());

		this.infoFocusers = infoFocusers;
		this.inputFocusers = infoFocusers.stream().map(chain -> (Function<?, ?>) chain.newInstance().op()).collect(Collectors.toList());

		this.infoSimplifier = infoSimplifier;
		this.outputSimplifier = (Function<?, ?>) infoSimplifier.newInstance().op();

		this.reqFocuser = reqFocuser;
		this.outputFocuser = (Function<?, ?>) reqFocuser.newInstance().op();

		this.copyOpChain = outputCopier;

		if (reqSimplifiers.size() != infoFocusers.size())
			throw new IllegalArgumentException(
				"Invalid SimplificationMetadata for Op - incompatible number of input simplifiers and focusers");
		numInputs = reqSimplifiers.size();

		List<MutatorChain> inputChains = new ArrayList<>();
		for (int i = 0; i < numInputs; i++) {
			InfoTree simplifier = this.reqSimplifiers.get(i);
			InfoTree focuser = this.infoFocusers.get(i);
			Type inType = simplifier.info().inputs().get(0).getType();
			Type outType = focuser.info().output().getType();
			inputChains.add(new CompleteMutatorChain(simplifier, focuser, new TypePair(inType, outType)));
		}
		this.argChains = inputChains.toArray(MutatorChain[]::new);

		Type inType = this.infoSimplifier.info().inputs().get(0).getType();
		Type outType = this.reqFocuser.info().output().getType();
		this.outChain = new CompleteMutatorChain(this.infoSimplifier, this.reqFocuser, new TypePair(inType, outType));
	}

	private static List<Function<?, ?>> inputSimplifiers(List<InfoTree> reqSimplifiers)
	{
		return reqSimplifiers.stream().map(chain -> (Function<?, ?>) chain.newInstance().op()).collect(Collectors.toList());
	}

	private static List<Function<?, ?>> inputFocusers(List<InfoTree> infoFocusers)
	{
		return infoFocusers.stream().map(chain -> (Function<?, ?>) chain.newInstance().op()).collect(Collectors.toList());
	}

	private static Function<?, ?> outputSimplifier(InfoTree infoSimplifier)
	{
		return (Function<?, ?>) infoSimplifier.newInstance().op();
	}

	private static Function<?, ?> outputFocuser(InfoTree reqFocuser)
	{
		return (Function<?, ?>) reqFocuser.newInstance().op();
	}

	public List<Function<?, ?>> inputSimpilfiers() {
		return inputSimplifiers;
	}

	public List<Function<?, ?>> inputFocusers() {
		return inputFocusers;
	}

	public Function<?, ?> outputSimplifier() {
		return outputSimplifier;
	}

	public Function<?, ?> outputFocuser() {
		return outputFocuser;
	}

	public Type[] originalInputs() {
		return Arrays.stream(argChains).map(chain -> chain.inputType()).toArray(Type[]::new);
	}

	public Type[] simpleInputs() {
		return Arrays.stream(argChains).map(chain -> chain.simpleType()).toArray(Type[]::new);
	}

	public Type[] unfocusedInputs() {
		return Arrays.stream(argChains).map(chain -> chain.simpleType()).toArray(Type[]::new);
	}

	public Type[] focusedInputs() {
		return Arrays.stream(argChains).map(chain -> chain.outputType()).toArray(Type[]::new);
	}

	public Type originalOutput() {
		return outChain.inputType();
	}

	public Type simpleOutput() {
		return outChain.simpleType();
	}

	public Type unfocusedOutput() {
		return outChain.unfocusedType();
	}

	public Type focusedOutput() {
		return outChain.outputType();
	}

	public int numInputs() {
		return numInputs;
	}

	public Class<?> opType() {
		return opType;
	}

	public boolean hasCopyOp() {
		return copyOpChain.isPresent();
	}

	public InfoTree copyOpChain() {
		return copyOpChain.get();
	}

	public Computers.Arity1<?, ?> copyOp() {
		// TODO: Should we prevent multiple instantiations?
		return (Arity1<?, ?>) copyOpChain.get().newInstance().op();
	}

	/**
	 * The constructor to the simplified Op takes:
	 * <ol>
	 * <li>All input simplifiers</li>
	 * <li>All input focusers</li>
	 * <li>The output simplifier</li>
	 * <li>The output focuser</li>
	 * <li>The original Op</li>
	 * </ol>
	 * This Op returns the {@link Class}es of those arguments <b>in that
	 * order</b>. Note that the {@link Class} of all arugments other than the
	 * {@code Op} is {@link Function}.
	 * 
	 * @return an array of classes describing the constructor arguments of the
	 *         simplified {@code Op}
	 */
	public Class<?>[] constructorClasses() {
		// there are 2*numInputs input mutators, 2 output mutators
		int numMutators = numInputs() * 2 + 2;
		// orignal Op plus a output copier if applicable
		int numOps = hasCopyOp() ? 2 : 1;
		Class<?>[] args = new Class<?>[numMutators + numOps];
		for (int i = 0; i < numMutators; i++)
			args[i] = Function.class;
		args[args.length - numOps] = opType();
		if(hasCopyOp())
			args[args.length - 1] = Computers.Arity1.class;
		return args;
	}

	/**
	 * The constructor to the simplified Op takes:
	 * <ol>
	 * <li>All input simplifiers</li>
	 * <li>All input focusers</li>
	 * <li>The output simplifier</li>
	 * <li>The output focuser</li>
	 * <li>The original Op</li>
	 * </ol>
	 * This Op returns those arguments <b>in that order</b>.
	 * 
	 * @return an array of the constructor arguments of the simplified {@code Op}
	 */
	public Object[] constructorArgs(Object op) {
		List<Object> args = new ArrayList<>(inputSimplifiers);
		args.addAll(inputFocusers);
		args.add(outputSimplifier);
		args.add(outputFocuser);
		args.add(op);
		if(hasCopyOp())
			args.add(copyOp());

		return args.toArray();
	}

	/**
	 * Returns the index of the argument that is both the input and the output. <b>If there is no such argument (i.e. the Op produces a pure output), -1 is returned</b>
	 * 
	 * @return the index of the mutable argument.
	 */
	public int ioArgIndex() {
		List<Member<?>> inputs = info.inputs();
		Optional<Member<?>> ioArg = inputs.stream().filter(m -> m.isInput() && m.isOutput()).findFirst();
		if(ioArg.isEmpty()) return -1;
		Member<?> ioMember = ioArg.get();
		return inputs.indexOf(ioMember);
	}

	public boolean pureOutput() {
		return ioArgIndex() == -1;
	}

	public OpInfo info() {
		return info;
	}

	protected List<OpInfo> inputSimplifierInfos() {
		return reqSimplifiers.stream().map(chain -> chain.info()).collect(Collectors.toList());
	}

	protected List<InfoTree> inputSimplifierChains() {
		return reqSimplifiers;
	}

	protected List<OpInfo> inputFocuserInfos() {
		return infoFocusers.stream().map(chain -> chain.info()).collect(Collectors.toList());
	}

	protected List<InfoTree> inputFocuserChains() {
		return infoFocusers;
	}

	protected OpInfo outputSimplifierInfo() {
		return infoSimplifier.info();
	}

	protected InfoTree outputSimplifierChain() {
		return infoSimplifier;
	}

	protected OpInfo outputFocuserInfo() {
		return reqFocuser.info();
	}

	protected InfoTree outputFocuserChain() {
		return reqFocuser;
	}

}
