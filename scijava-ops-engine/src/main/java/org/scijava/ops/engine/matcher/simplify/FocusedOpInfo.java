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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.api.Ops;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.struct.OpRetypingMemberParser;
import org.scijava.ops.engine.struct.RetypingRequest;
import org.scijava.priority.Priority;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * A {@link SimplifiedOpInfo} whose inputs and outputs have been focused to
 * "focused" types. The inputs are focused through the use of {@code simplify}
 * {@link Function} Ops. The output is focused through the user of a
 * {@code focus} {@link Function} Op, and, if the Op defines a preallocated
 * output buffer, a {@code copy} {@link Computers.Arity1} Op to place the result
 * of the Op back into the passed output buffer.
 * <p>
 * As an example, consider a {@code Function<Number, Number>}, simplified from a
 * {@code Function<Double, Double>}. If we have a {@code simplify} Op that goes
 * from {@link Long} to {@link Number} <b>and</b> a {@code focus} Op that can go
 * from {@link Number} to {@link Long}, then we can construct a
 * {@code Function<Long, Long>} Op by using the {@code simplify} Op on our input
 * {@link Long}, passing that output into our original Op, and passing the
 * output through the {@code simplify} Op to get a {@link Long} before yielding
 * the output back to the user.
 * </p>
 *
 * @author Gabriel Selzer
 * @see SimplifiedOpInfo
 */
public class FocusedOpInfo implements OpInfo {

	/** Identifiers for declaring a simplification in an Op signature **/
	protected static final String IMPL_DECLARATION = "|Focused:";
	protected static final String INPUT_SIMPLIFIER_DELIMITER =
		"|InputSimplifier:";
	protected static final String OUTPUT_FOCUSER_DELIMITER = "|OutputFocuser:";
	protected static final String OUTPUT_COPIER_DELIMITER = "|OutputCopier:";
	protected static final String ORIGINAL_INFO = "|SimpleInfo:";

	private final SimplifiedOpInfo simpleInfo;
	private final List<RichOp<Function<?, ?>>> inputSimplifiers;
	private final RichOp<Function<?, ?>> outputFocuser;
	private final RichOp<Computers.Arity1<?, ?>> copyOp;
	private final Type opType;
	private final Struct struct;
	private final Hints hints;
	private final OpEnvironment env;
	private Double priority;

	public FocusedOpInfo(SimplifiedOpInfo simpleInfo,
		SimplifiedOpRequest simpleReq, OpEnvironment env)
	{
		this( //
				simpleInfo, //
				simpleReq.srcReq().getType(), //
				simpleReq.inputSimplifiers(), //
				simpleReq.outputFocuser(), //
				null, //
				env //
		);
	}

	public FocusedOpInfo(SimplifiedOpInfo simpleInfo,
		Type opType,
		List<RichOp<Function<?, ?>>> inputSimplifiers,
		RichOp<Function<?, ?>> outputFocuser, RichOp<Computers.Arity1<?, ?>> copier, OpEnvironment env)
	{
		this.simpleInfo = simpleInfo;
		this.inputSimplifiers = inputSimplifiers;
		this.outputFocuser = outputFocuser;
		this.env = env;

		this.opType = opType;
		var fmts = FunctionalParameters.findFunctionalMethodTypes(opType);
		RetypingRequest r = new RetypingRequest(simpleInfo.struct(), fmts);
		this.struct = Structs.from(r, opType, new OpRetypingMemberParser());

		this.hints = simpleInfo.declaredHints().plus(
			BaseOpHints.Simplification.FORBIDDEN);
		this.copyOp = ensureNonNull(copier);
	}

	private RichOp<Computers.Arity1<?,?>> ensureNonNull(RichOp<Computers.Arity1<?,?>> copier) {
		if (copier != null) {
			return copier;
		}
		return simplifierCopyOp(this.env, Types.raw(this.opType), this.inputTypes());
	}

	@Override
	public List<String> names() {
		return simpleInfo.names();
	}

	@Override
	public Type opType() {
		return opType;
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public Hints declaredHints() {
		return hints;
	}

	@Override
	public double priority() {
		if (priority == null) {
			priority = calculatePriority(env);
		}
		return priority;
	}

	@Override
	public String implementationName() {
		StringBuilder sb = new StringBuilder(simpleInfo.srcInfo().implementationName());
		sb.append("|focused");
		for(Member<?> m: struct()) {
			if (m.isInput() || m.isOutput()) {
				sb.append("_");
				sb.append(SimplificationUtils.getClassName(m.getType()));
			}
		}
		return sb.toString();
	}

	/**
	 * Creates a <b>simplified</b> version of the original Op, whose parameter
	 * types are dictated by the {@code focusedType}s of this info's Simplifiers.
	 * The resulting Op will use {@code simplifier}s to simplify the inputs, and
	 * then will use this info's {@code focuser}s to focus the simplified inputs
	 * into types suitable for the original Op.
	 *
	 * @param dependencies - this Op's dependencies
	 */
	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		// Original Op
		final Object op = simpleInfo.srcInfo().createOpInstance(dependencies)
			.object();
		// Create Functions to Simplify and then Focus each input
		List<Function<?, ?>> inputProcessors = new ArrayList<>();
		for(int i = 0; i < inputSimplifiers.size(); i++) {
			inputProcessors.add(combineFunctions(inputSimplifiers.get(i), simpleInfo.inputFocusers.get(i)));
		}
		// Create Function to Simplify and then Focus the output
		Function<?, ?> outputProcessor = combineFunctions(simpleInfo.outputSimplifier, outputFocuser);
		// Grab the output copier if it exists.
		Computers.Arity1<?, ?> outputCopier = copyOp == null ? null : copyOp.asOpType();

		try {
			Object simpleOp = SimplificationUtils.javassistOp(op,
					this,
					inputProcessors,
					outputProcessor,
					outputCopier
			);
			return struct().createInstance(simpleOp);
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException("Failed to invoke focusing of Op: \n" +
				simpleInfo + "\nProvided Op dependencies were: " + dependencies, ex);
		}
	}

	/**
	 * Helper function that coerces type variables, so that we can call
	 * {@link Function#andThen(Function)} and return the composition.
	 * 
	 * @param f1 the first {@link Function}, mapping X to Y
	 * @param f2 the second {@link Function}, mapping Y to Z
	 * @return the composition, mapping X to Z
	 * @param <T> the output type of the first function, which is also the input
	 *          type of the second function.
	 * @param <U> the output type of the second function.
	 */
	@SuppressWarnings("unchecked")
	private static <T, U> Function<?, ?> combineFunctions(
		RichOp<Function<?, ?>> f1, RichOp<Function<?, ?>> f2)
	{
		Function<?, T> foo1 = (Function<?, T>) f1.asOpType();
		Function<T, U> foo2 = (Function<T, U>) f2.asOpType();
		return foo1.andThen(foo2);
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return simpleInfo.getAnnotationBearer();
	}

	@Override
	public String version() {
		return simpleInfo.version();
	}

	/**
	 * For a simplified Op, we define the implementation as the concatenation of:
	 * <ol>
	 * <li>The signature of all input simplifiers</li>
	 * <li>The signature of all input focusers</li>
	 * <li>The signature of the output simplifier</li>
	 * <li>The signature of the output focuser</li>
	 * <li>The signature of the output copier</li>
	 * <li>The id of the source Op</li>
	 * </ol>
	 */
	@Override
	public String id() {
		// original Op
		StringBuilder sb = new StringBuilder(IMPL_DECLARATION);
		// input simplifiers
		for (RichOp<Function<?, ?>> i : inputSimplifiers) {
			sb.append(INPUT_SIMPLIFIER_DELIMITER);
			sb.append(i.infoTree().signature());
		}
		// output focuser
		sb.append(OUTPUT_FOCUSER_DELIMITER);
		sb.append(outputFocuser.infoTree().signature());
		// output copier
		if (copyOp != null) {
			sb.append(OUTPUT_COPIER_DELIMITER);
			sb.append(copyOp.infoTree().signature());
		}
		// original info
		sb.append(ORIGINAL_INFO);
		sb.append(simpleInfo.id());

		return sb.toString();
	}

	/**
	 * Finds a {@code copy} Op designed to copy an Op's output (of {@link Type}
	 * {@code copyType}) back into the preallocated output during simplification.
	 * <p>
	 * NB Simplification is forbidden here because we are asking for a
	 * {@code Computers.Arity1<T, T>} copy Op (for some {@link Type}
	 * {@code type}). Suppose that no direct match existed, and we tried to find a
	 * simplified version. This simplified version, because it is a
	 * Computers.Arity1, would need a {@link Computers.Arity1} copy Op to copy the
	 * output of the simplified Op back into the preallocated output. But this
	 * call is already identical to the Op we asked for, and we know that there is
	 * no direct match, thus we go again into simplification. This thus causes an
	 * infinite loop (and eventually a {@link StackOverflowError}. This means that
	 * we cannot find a simplified copy Op <b>unless a direct match can be
	 * found</b>, at which point we might as well just use the direct match.
	 * </p>
	 * <p>
	 * Adaptation is similarly forbidden, as to convert most Op types to
	 * {@link Computers.Arity1} you would need an identical copy Op.
	 * </p>
	 *
	 * @param env - the {@link OpEnvironment} containing the copy Ops
	 * @param opType - the {@link FunctionalInterface} implemented by the Op.
	 * @param args - the input {@link Type}s to the Op
	 * @return an {@code Op} able to copy data between {@link Object}s of
	 *         {@link Type} {@code copyType}
	 */
	private static RichOp<Computers.Arity1<?, ?>> simplifierCopyOp(
		OpEnvironment env, Class<?> opType, List<Type> args)
	{
		int mutableIndex = SimplificationUtils.findMutableArgIndex(opType);
		if (mutableIndex == -1) {
			return null;
		}
		Type copyType = args.get(mutableIndex);
		// prevent further simplification/adaptation
		Hints hints = new Hints(BaseOpHints.Adaptation.FORBIDDEN,
			BaseOpHints.Simplification.FORBIDDEN);
		Nil<?> copyNil = Nil.of(copyType);
		var op = env.unary("copy", hints).inType(copyNil).outType(copyNil)
			.computer();
		return Ops.rich(op);
	}

	/**
	 * We define the priority of any {@link SimplifiedOpInfo} as the sum of the
	 * following:
	 * <ul>
	 * <li>{@link Priority#VERY_LOW} to ensure that simplifications are not chosen
	 * over a direct match.</li>
	 * <li>The {@link OpInfo#priority} of the source info to ensure that a
	 * simplification of a higher-priority Op wins out over a simplification of a
	 * lower-priority Op, all else equal.</li>
	 * <li>a penalty defined as a lossiness heuristic of this simplification. This
	 * penalty is the sum of:</li>
	 * <ul>
	 * <li>the loss undertaken by converting each of the Op's inputs from the ref
	 * type to the info type</li>
	 * <li>the loss undertaken by converting each of the Op's outputs from the
	 * info type to the ref type</li>
	 * </ul>
	 * </ul>
	 */
	private double calculatePriority(OpEnvironment env) {
		// BASE PRIORITY
		double base = Priority.VERY_LOW;

		// ORIGINAL PRIORITY
		double originalPriority = simpleInfo.srcInfo().priority();

		// PENALTY
		double penalty = 0;

		List<Type> originalInputs = simpleInfo.srcInfo().inputTypes();
		List<Type> inputs = inputTypes();
		for (int i = 0; i < inputs.size(); i++) {
			penalty += determineLoss(env, Nil.of(inputs.get(i)), Nil.of(originalInputs
				.get(i)));
		}

		Type opOutput = simpleInfo.srcInfo().outputType();
		penalty += determineLoss(env, Nil.of(opOutput), Nil.of(outputType()));

		// PRIORITY = BASE + ORIGINAL - PENALTY
		return base + originalPriority - penalty;
	}

	/**
	 * Calls a {@code engine.lossReporter} Op to determine the <b>worst-case</b> loss
	 * from a {@code T} to a {@code R}. If no {@code engine.lossReporter} exists for such
	 * a conversion, we assume infinite loss.
	 *
	 * @param <T> -the generic type we are converting from.
	 * @param <R> - generic type we are converting to.
	 * @param from - a {@link Nil} describing the type we are converting from
	 * @param to - a {@link Nil} describing the type we are converting to
	 * @return - a {@code double} describing the magnitude of the <worst-case>
	 *         loss in a conversion from an instance of {@code T} to an instance
	 *         of {@code R}
	 */
	private static <T, R> double determineLoss(OpEnvironment env, Nil<T> from,
		Nil<R> to)
	{
		Type specialType = Types.parameterize(LossReporter.class, new Type[] { from
			.getType(), to.getType() });
		@SuppressWarnings("unchecked")
		Nil<LossReporter<T, R>> specialTypeNil = (Nil<LossReporter<T, R>>) Nil.of(
			specialType);
		try {
			Type nilFromType = Types.parameterize(Nil.class, new Type[] { from
				.getType() });
			Type nilToType = Types.parameterize(Nil.class, new Type[] { to
				.getType() });
			Hints h = new Hints(BaseOpHints.Adaptation.FORBIDDEN,
				BaseOpHints.Simplification.FORBIDDEN);
			LossReporter<T, R> op = env.op("engine.lossReporter", specialTypeNil, new Nil[] {
				Nil.of(nilFromType), Nil.of(nilToType) }, Nil.of(Double.class), h);
			return op.apply(from, to);
		}
		catch (OpMatchingException e) {
			return Double.POSITIVE_INFINITY;
		}
	}

}
