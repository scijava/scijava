
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
import org.scijava.function.Computers.Arity1;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpUtils;
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

	private final List<InfoChain> refSimplifiers;
	private final List<Function<?, ?>> inputSimplifiers;
	private final List<InfoChain> infoFocusers;
	private final List<Function<?, ?>> inputFocusers;
	private final InfoChain infoSimplifier;
	private final Function<?, ?> outputSimplifier;
	private final InfoChain refFocuser;
	private final Function<?, ?> outputFocuser;

	private final Optional<InfoChain> copyOpChain;

	private final int numInputs;

	public SimplificationMetadata(SimplifiedOpRef ref, OpInfo info, TypePair[] argPairs, TypePair outPair, Map<TypePair, MutatorChain> mutators)
	{
		this.info = info;
		this.opType = Types.raw(info.opType());
		this.argChains = Arrays.stream(argPairs).map(pair -> mutators.get(pair)).toArray(MutatorChain[]::new);
		this.outChain = mutators.get(outPair);

		this.refSimplifiers = Arrays.stream(argChains).map(chain -> chain.simplifier()).collect(Collectors.toList());
		this.inputSimplifiers = inputSimplifiers(this.refSimplifiers);

		this.infoFocusers = Arrays.stream(argChains).map(chain -> chain.focuser()).collect(Collectors.toList());
		this.inputFocusers = inputFocusers(this.infoFocusers);

		this.infoSimplifier = outChain.simplifier();
		this.outputSimplifier = outputSimplifier(this.infoSimplifier);

		this.refFocuser = outChain.focuser();
		this.outputFocuser = outputFocuser(this.refFocuser);

		this.copyOpChain = ref.copyOpChain();

		if (refSimplifiers.size() != infoFocusers.size())
			throw new IllegalArgumentException(
				"Invalid SimplificationMetadata for Op - incompatible number of input simplifiers and focusers");
		numInputs = refSimplifiers.size();
	}

	public SimplificationMetadata(OpInfo info, List<InfoChain> refSimplifiers, List<InfoChain> infoFocusers, InfoChain infoSimplifier, InfoChain refFocuser, Optional<InfoChain> outputCopier) {
		this.info = info;
		this.opType = Types.raw(info.opType());
		
		this.refSimplifiers = refSimplifiers;
		this.inputSimplifiers = refSimplifiers.stream().map(chain -> (Function<?, ?>) chain.newInstance().op()).collect(Collectors.toList());

		this.infoFocusers = infoFocusers;
		this.inputFocusers = infoFocusers.stream().map(chain -> (Function<?, ?>) chain.newInstance().op()).collect(Collectors.toList());

		this.infoSimplifier = infoSimplifier;
		this.outputSimplifier = (Function<?, ?>) infoSimplifier.newInstance().op();

		this.refFocuser = refFocuser;
		this.outputFocuser = (Function<?, ?>) refFocuser.newInstance().op();

		this.copyOpChain = outputCopier;

		if (refSimplifiers.size() != infoFocusers.size())
			throw new IllegalArgumentException(
				"Invalid SimplificationMetadata for Op - incompatible number of input simplifiers and focusers");
		numInputs = refSimplifiers.size();

		List<MutatorChain> inputChains = new ArrayList<>();
		for (int i = 0; i < numInputs; i++) {
			InfoChain simplifier = this.refSimplifiers.get(i);
			InfoChain focuser = this.infoFocusers.get(i);
			Type inType = simplifier.info().inputs().get(0).getType();
			Type outType = focuser.info().output().getType();
			inputChains.add(new CompleteMutatorChain(simplifier, focuser, new TypePair(inType, outType)));
		}
		this.argChains = inputChains.toArray(MutatorChain[]::new);

		Type inType = this.infoSimplifier.info().inputs().get(0).getType();
		Type outType = this.refFocuser.info().output().getType();
		this.outChain = new CompleteMutatorChain(this.infoSimplifier, this.refFocuser, new TypePair(inType, outType));
	}

	private static List<Function<?, ?>> inputSimplifiers(List<InfoChain> refSimplifiers)
	{
		return refSimplifiers.stream().map(chain -> (Function<?, ?>) chain.newInstance().op()).collect(Collectors.toList());
	}

	private static List<Function<?, ?>> inputFocusers(List<InfoChain> infoFocusers)
	{
		return infoFocusers.stream().map(chain -> (Function<?, ?>) chain.newInstance().op()).collect(Collectors.toList());
	}

	private static Function<?, ?> outputSimplifier(InfoChain infoSimplifier)
	{
		return (Function<?, ?>) infoSimplifier.newInstance().op();
	}

	private static Function<?, ?> outputFocuser(InfoChain refFocuser)
	{
		return (Function<?, ?>) refFocuser.newInstance().op();
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

	public InfoChain copyOpChain() {
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
		return refSimplifiers.stream().map(chain -> chain.info()).collect(Collectors.toList());
	}

	protected List<InfoChain> inputSimplifierChains() {
		return refSimplifiers;
	}

	protected List<OpInfo> inputFocuserInfos() {
		return infoFocusers.stream().map(chain -> chain.info()).collect(Collectors.toList());
	}

	protected List<InfoChain> inputFocuserChains() {
		return infoFocusers;
	}

	protected OpInfo outputSimplifierInfo() {
		return infoSimplifier.info();
	}

	protected InfoChain outputSimplifierChain() {
		return infoSimplifier;
	}

	protected OpInfo outputFocuserInfo() {
		return refFocuser.info();
	}

	protected InfoChain outputFocuserChain() {
		return refFocuser;
	}

}
