package org.scijava.ops.engine.matcher.simplify;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.scijava.common3.Comparisons;
import org.scijava.common3.validity.ValidityException;
import org.scijava.common3.validity.ValidityProblem;
import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpDescription;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRetrievalException;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.engine.struct.OpRetypingMemberParser;
import org.scijava.ops.engine.struct.RetypingRequest;
import org.scijava.priority.Priority;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.types.Nil;
import org.scijava.types.Types;

public class SimplifiedOpInfo implements OpInfo {

	/** Identifiers for declaring a simplification in an Op signature **/
	protected static final String IMPL_DECLARATION = "|Simplification:";
	protected static final String INPUT_SIMPLIFIER_DELIMITER = "|InputSimplifier:";
	protected static final String INPUT_FOCUSER_DELIMITER = "|InputFocuser:";
	protected static final String OUTPUT_SIMPLIFIER_DELIMITER = "|OutputSimplifier:";
	protected static final String OUTPUT_FOCUSER_DELIMITER = "|OutputFocuser:";
	protected static final String OUTPUT_COPIER_DELIMITER = "|OutputCopier:";
	protected static final String ORIGINAL_INFO = "|OriginalInfo:";

	private final OpInfo srcInfo;
	private final SimplificationMetadata metadata;
	private final Type opType;
	private final double priority;
	private final Hints hints;

	private Struct struct;

	public SimplifiedOpInfo(OpInfo info, OpEnvironment env, SimplificationMetadata metadata) {
		this(info, metadata, calculatePriority(info, metadata, env));
	}

	public SimplifiedOpInfo(OpInfo info, SimplificationMetadata metadata, double priority) {
		List<ValidityProblem> problems = new ArrayList<>();
		this.srcInfo = info;
		this.metadata = metadata;
		// generate new input fmts
		Type[] inputTypes = metadata.originalInputs();
		Type outputType = metadata.focusedOutput();
		List<Member<?>> ioMembers = info.struct().members();
		ioMembers.removeIf(m -> m.getIOType() == ItemIO.NONE);
		int index = 0;
		List<FunctionalMethodType> fmts = new ArrayList<>();
		for (Member<?> m : ioMembers) {
			Type newType = m.isInput() ? inputTypes[index++] : m.isOutput()
				? outputType : null;
			fmts.add(new FunctionalMethodType(newType, m.getIOType()));
		}
		// generate new output fmt

		this.opType = SimplificationUtils.retypeOpType(info.opType(), inputTypes,
			outputType);
		RetypingRequest r = new RetypingRequest(info.struct(), fmts);
		this.struct = Structs.from(r, opType, problems, new OpRetypingMemberParser());

		this.priority = priority;
		this.hints = srcInfo.declaredHints().plus(Simplification.FORBIDDEN);

		if(!problems.isEmpty()) {
			throw new ValidityException(problems);
		}
	}

	public OpInfo srcInfo() {
		return srcInfo;
	}

	@Override
	public List<String> names() {
		return srcInfo().names();
	}

	@Override
	public Type opType() {
		return opType;
	}

	@Override
	public Hints declaredHints() {
		return hints;
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public double priority() {
		return priority;
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
	 * <li>the loss undertaken by converting each of the Op's outputs from the info
	 * type to the ref type</li>
	 * </ul>
	 * </ul>
	 */
	private static double calculatePriority(OpInfo srcInfo, SimplificationMetadata metadata, OpEnvironment env) {
		// BASE PRIORITY
		double base = Priority.VERY_LOW;

		// ORIGINAL PRIORITY
		double originalPriority = srcInfo.priority();

		// PENALTY
		double penalty = 0;

		Type[] originalInputs = metadata.originalInputs();
		Type[] opInputs = metadata.focusedInputs();
		for (int i = 0; i < metadata.numInputs(); i++) {
			penalty += determineLoss(env, Nil.of(originalInputs[i]), Nil.of(opInputs[i]));
		}

		// TODO: only calculate the loss once
		Type opOutput = metadata.focusedOutput();
		Type originalOutput = metadata.originalOutput();
		penalty += determineLoss(env, Nil.of(opOutput), Nil.of(originalOutput));

		// PRIORITY = BASE + ORIGINAL - PENALTY
		return base + originalPriority - penalty;
	}

	/**
	 * Calls a {@code lossReporter} Op to determine the <b>worst-case</b>
	 * loss from a {@code T} to a {@code R}. If no {@code lossReporter} exists for
	 * such a conversion, we assume infinite loss.
	 * 
	 * @param <T> -the generic type we are converting from.
	 * @param <R> - generic type we are converting to.
	 * @param from - a {@link Nil} describing the type we are converting from
	 * @param to - a {@link Nil} describing the type we are converting to
	 * @return - a {@code double} describing the magnitude of the <worst-case>
	 *         loss in a conversion from an instance of {@code T} to an instance
	 *         of {@code R}
	 */
	private static <T, R> double determineLoss(OpEnvironment env, Nil<T> from, Nil<R> to) {
		Type specialType = Types.parameterize(LossReporter.class, new Type[] { from
			.getType(), to.getType() });
		@SuppressWarnings("unchecked")
		Nil<LossReporter<T, R>> specialTypeNil = (Nil<LossReporter<T, R>>) Nil.of(
			specialType);
		try {
			Type nilFromType = Types.parameterize(Nil.class, new Type[] {from.getType()});
			Type nilToType = Types.parameterize(Nil.class, new Type[] {to.getType()});
			LossReporter<T, R> op = env.op("lossReporter", specialTypeNil, new Nil[] {
				Nil.of(nilFromType), Nil.of(nilToType) }, Nil.of(Double.class));
			return op.apply(from, to);
		}
		catch (OpRetrievalException e) {
			return Double.POSITIVE_INFINITY;
		}
	}

	@Override
	public String implementationName() {
		return srcInfo.implementationName() + " simplified to a " + opType();
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return srcInfo.getAnnotationBearer();
	}

	/**
	 * Creates a <b>simplified</b> version of the original Op, whose parameter
	 * types are dictated by the {@code focusedType}s of this info's
	 * Simplifiers. The resulting Op will use {@code simplifier}s to
	 * simplify the inputs, and then will use this info's {@code focuser}s to
	 * focus the simplified inputs into types suitable for the original Op.
	 * 
	 * @param dependencies - this Op's dependencies
	 */
	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies)
	{
		final Object op = srcInfo.createOpInstance(dependencies).object();
		try {
			Object simpleOp = SimplificationUtils.javassistOp(op, metadata);
			return struct().createInstance(simpleOp);
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException(
				"Failed to invoke simplification of Op: \n" + srcInfo +
					"\nProvided Op dependencies were: " + Objects.toString(dependencies),
				ex);
		}
	}

	@Override
	public String toString() { return OpDescription.basic(this); }
	
	@Override
	public int compareTo(final OpInfo that) {
		// compare priorities
		if (this.priority() < that.priority()) return 1;
		if (this.priority() > that.priority()) return -1;

		// compare implementation names 
		int implNameDiff = Comparisons.compare(this.implementationName(), that.implementationName());
		if(implNameDiff != 0) return implNameDiff; 

		// compare structs if the OpInfos are "sibling" SimplifiedOpInfos
		if(that instanceof SimplifiedOpInfo) return compareToSimplifiedInfo((SimplifiedOpInfo) that);

		return 0;
	}

	private int compareToSimplifiedInfo(SimplifiedOpInfo that) {
		// Compare structs
		List<Member<?>> theseMembers = new ArrayList<>();
		this.struct().forEach(theseMembers::add);
		List<Member<?>> thoseMembers = new ArrayList<>();
		that.struct().forEach(thoseMembers::add);
		return theseMembers.hashCode() - thoseMembers.hashCode();
	}

	@Override
	public String version() {
		return Versions.getVersion(this.getClass());
	}

	/**
	 * For a simplified Op, we define the implementation as the concatenation
	 * of:
	 * <ol>
	 * <li>The signature of all input simplifiers</li>
	 * <li>The signature of all input focusers</li>
	 * <li>The signature of the output simplifier</li>
	 * <li>The signature of the output focuser</li>
	 * <li>The signature of the output copier</li>
	 * <li>The id of the source Op</li>
	 * </ol>
	 * <p>
	 */
	@Override
	public String id() {
		// original Op
		StringBuilder sb = new StringBuilder(IMPL_DECLARATION);
		// input simplifiers
		for (InfoChain i : metadata.inputSimplifierChains()) {
			sb.append(INPUT_SIMPLIFIER_DELIMITER);
			sb.append(i.signature());
		}
		// input focusers
		for (InfoChain i : metadata.inputFocuserChains()) {
			sb.append(INPUT_FOCUSER_DELIMITER);
			sb.append(i.signature());
		}
		// output simplifier
		sb.append(OUTPUT_SIMPLIFIER_DELIMITER);
		sb.append(metadata.outputSimplifierChain().signature());
		// output focuser
		sb.append(OUTPUT_FOCUSER_DELIMITER);
		sb.append(metadata.outputFocuserChain().signature());

		// output copier
		sb.append(OUTPUT_COPIER_DELIMITER);
		sb.append(metadata.copyOpChain().signature());
		// original info
		sb.append(ORIGINAL_INFO);
		sb.append(srcInfo().id());
		return sb.toString();
	}
}
