
package org.scijava.ops.engine.simplify;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.function.Computers;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpUtils;
import org.scijava.struct.Member;
import org.scijava.util.Types;

/**
 * Compiles a list of all needed Mutators (either Simplifiers or Focuser
 * {@link Function}s) for a given {@link SimplifiedOpCandidate}.
 * 
 * @author G
 */
public class SimplificationMetadata {

	private final OpInfo info;
	private final Class<?> opType;
	private final MutatorChain[] argChains;
	private final MutatorChain outChain;

	private final List<OpInfo> refSimplifiers;
	private final List<Function<?, ?>> inputSimplifiers;
	private final List<OpInfo> infoFocusers;
	private final List<Function<?, ?>> inputFocusers;
	private final OpInfo infoSimplifier;
	private final Function<?, ?> outputSimplifier;
	private final OpInfo refFocuser;
	private final Function<?, ?> outputFocuser;

	private final Optional<Computers.Arity1<?, ?>> copyOp;

	private final int numInputs;

	public SimplificationMetadata(SimplifiedOpRef ref, OpInfo info, TypePair[] argPairs, TypePair outPair, Map<TypePair, MutatorChain> mutators,
		OpEnvironment env)
	{
		this.info = info;
		this.opType = Types.raw(info.opType());
		this.argChains = Arrays.stream(argPairs).map(pair -> mutators.get(pair)).toArray(MutatorChain[]::new);
		this.outChain = mutators.get(outPair);

		this.refSimplifiers = Arrays.stream(argChains).map(chain -> chain.simplifier()).collect(Collectors.toList());
		this.inputSimplifiers = inputSimplifiers(argChains, env, this.refSimplifiers);

		this.infoFocusers = Arrays.stream(argChains).map(chain -> chain.focuser()).collect(Collectors.toList());
		this.inputFocusers = inputFocusers(argChains, env, this.infoFocusers);

		this.infoSimplifier = outChain.simplifier();
		this.outputSimplifier = outputSimplifier(outChain, env, this.infoSimplifier);

		this.refFocuser = outChain.focuser();
		this.outputFocuser = outputFocuser(outChain, env, this.refFocuser);

		this.copyOp = ref.copyOp();

		if (refSimplifiers.size() != infoFocusers.size())
			throw new IllegalArgumentException(
				"Invalid SimplificationMetadata for Op - incompatible number of input simplifiers and focusers");
		numInputs = refSimplifiers.size();
	}

	public SimplificationMetadata(OpInfo info, List<InfoChain> refSimplifiers, List<InfoChain> infoFocusers, InfoChain infoSimplifier, InfoChain refFocuser, Optional<InfoChain> outputCopier) {
		this.info = info;
		this.opType = Types.raw(info.opType());
		
		this.refSimplifiers = refSimplifiers.stream().map(chain -> chain.info()).collect(Collectors.toList());
		this.inputSimplifiers = refSimplifiers.stream().map(chain -> (Function<?, ?>) chain.op().op()).collect(Collectors.toList());

		this.infoFocusers = infoFocusers.stream().map(chain -> chain.info()).collect(Collectors.toList());
		this.inputFocusers = infoFocusers.stream().map(chain -> (Function<?, ?>) chain.op().op()).collect(Collectors.toList());

		this.infoSimplifier = infoSimplifier.info();
		this.outputSimplifier = (Function<?, ?>) infoSimplifier.op().op();

		this.refFocuser = refFocuser.info();
		this.outputFocuser = (Function<?, ?>) refFocuser.op().op();

		if (outputCopier.isEmpty()) {
			this.copyOp = Optional.empty();
		}
		else {
			this.copyOp = Optional.of((Computers.Arity1<?, ?>) outputCopier.get().op()
				.op());
		}

		if (refSimplifiers.size() != infoFocusers.size())
			throw new IllegalArgumentException(
				"Invalid SimplificationMetadata for Op - incompatible number of input simplifiers and focusers");
		numInputs = refSimplifiers.size();

		List<MutatorChain> inputChains = new ArrayList<>();
		for (int i = 0; i < numInputs; i++) {
			OpInfo simplifier = this.refSimplifiers.get(i);
			OpInfo focuser = this.infoFocusers.get(i);
			Type inType = simplifier.inputs().get(0).getType();
			Type outType = focuser.output().getType();
			inputChains.add(new MutatorChain(simplifier, focuser, new TypePair(inType, outType)));
		}
		this.argChains = inputChains.toArray(MutatorChain[]::new);

		Type inType = this.infoSimplifier.inputs().get(0).getType();
		Type outType = this.refFocuser.output().getType();
		this.outChain = new MutatorChain(this.infoSimplifier, this.refFocuser, new TypePair(inType, outType));
	}

	private static List<Function<?, ?>> inputSimplifiers(
		MutatorChain[] chains, OpEnvironment env, List<OpInfo> refSimplifiers)
	{
		Type[] originalInputs = Arrays.stream(chains).map(chain -> chain.inputType()).toArray(Type[]::new);
		Type[] simpleInputs = Arrays.stream(chains).map(chain -> chain.simpleType()).toArray(Type[]::new);
		return SimplificationUtils.findArgMutators(env, refSimplifiers,
			originalInputs, simpleInputs);
	}

	private static List<Function<?, ?>> inputFocusers(MutatorChain[] chains, 
		OpEnvironment env, List<OpInfo> infoFocusers)
	{
		Type[] unfocusedInputs = Arrays.stream(chains).map(chain -> chain.unfocusedType()).toArray(Type[]::new);
		Type[] focusedInputs = Arrays.stream(chains).map(chain -> chain.outputType()).toArray(Type[]::new);
		return SimplificationUtils.findArgMutators(env, infoFocusers,
			unfocusedInputs, focusedInputs);
	}

	private static Function<?, ?> outputSimplifier(MutatorChain chain,
		OpEnvironment env, OpInfo infoSimplifier)
	{
		Type originalOutput = chain.inputType();
		Type simpleOutput = chain.simpleType();
		return SimplificationUtils.findArgMutator(env, infoSimplifier,
			originalOutput, simpleOutput);
	}

	private static Function<?, ?> outputFocuser(MutatorChain chain,
		OpEnvironment env, OpInfo refFocuser)
	{
		Type unfocusedOutput = chain.unfocusedType();
		Type focusedOutput = chain.outputType();
		return SimplificationUtils.findArgMutator(env, refFocuser, unfocusedOutput,
			focusedOutput);
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
		return copyOp.isPresent();
	}

	public Computers.Arity1<?, ?> copyOp() {
		return copyOp.get();
	}

	/**
	 * The constructor to the simplified Op takes:
	 * <ol>
	 * <li>All input simplifiers
	 * <li>All input focusers
	 * <li>The output simplifier
	 * <li>The output focuser
	 * <li>The original Op
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
	 * <li>All input simplifiers
	 * <li>All input focusers
	 * <li>The output simplifier
	 * <li>The output focuser
	 * <li>The original Op
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
			args.add(copyOp.get());

		return args.toArray();
	}

	/**
	 * Returns the index of the argument that is both the input and the output. <b>If there is no such argument (i.e. the Op produces a pure output), -1 is returned</b>
	 * 
	 * @return the index of the mutable argument.
	 */
	public int ioArgIndex() {
		List<Member<?>> inputs = OpUtils.inputs(info.struct());
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
		return refSimplifiers;
	}

	protected List<OpInfo> inputFocuserInfos() {
		return infoFocusers;
	}

	protected OpInfo outputSimplifierInfo() {
		return infoSimplifier;
	}

	protected OpInfo outputFocuserInfo() {
		return refFocuser;
	}

}
