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

package org.scijava.ops.engine.matcher.convert;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.common3.Comparisons;
import org.scijava.function.Computers;
import org.scijava.meta.Versions;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.engine.struct.OpRetypingMemberParser;
import org.scijava.ops.engine.struct.RetypingRequest;
import org.scijava.ops.engine.util.Infos;
import org.scijava.priority.Priority;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.inference.FunctionalInterfaces;
import org.scijava.types.inference.GenericAssignability;

/**
 * An {@link OpInfo} whose input and output types are transformed through the
 * use of {@code engine.convert} {@link Function} Ops. Op instances created by
 * this {@code OpInfo} utilize a {@code engine.convert} "preconverter" Op to
 * coerce each provided input to the type expected by the Op, and after
 * execution use a {@code engine.convert} "postconverter" Op to coerce the
 * output back into the type expected by the user. If the Op defines a
 * preallocated output buffer, a {@code engine.copy} {@link Computers.Arity1} Op
 * is used to place the computational results of the Op back into the passed
 * output buffer.
 * <p>
 * As an example, consider a {@code Function<Double, Double>}. If we have a
 * {@code engine.convert} Op that goes from {@link Integer} to {@link Double}
 * and a {@code engine.convert} Op that goes from {@link Double} to
 * {@link Integer} then we can construct a {@code Function<Integer, Integer>}
 * Op. At runtime, we will utilize the former {@code engine.convert} Op to
 * preconvert the input {@code Integer} into a {@code Double}, invoke the
 * {@code Function<Double, Double>}, and then postconvert the {@code Double}
 * into an {@code Integer} using the latter {@code engine.convert} Op, which is
 * returned to the user.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class ConvertedOpInfo implements OpInfo {

	/** Identifiers for declaring a conversion in an Op signature **/
	protected static final String IMPL_DECLARATION = "|Conversion:";
	protected static final String PRECONVERTER_DELIMITER = "|Preconverter:";
	protected static final String POSTCONVERTER_DELIMITER = "|Postconverter:";
	protected static final String OUTPUT_COPIER_DELIMITER = "|OutputCopier:";
	protected static final String ORIGINAL_INFO = "|OriginalInfo:";

	private final OpInfo info;
	private final OpEnvironment env;
	final List<RichOp<Function<?, ?>>> preconverters;
	final RichOp<Function<?, ?>> postconverter;
	final RichOp<Computers.Arity1<?, ?>> copyOp;
	private final Type opType;
	private final Struct struct;
	private Double priority = null;
	private final Hints hints;

	public ConvertedOpInfo(OpInfo info,
		List<RichOp<Function<?, ?>>> preconverters,
		RichOp<Function<?, ?>> postconverter,
		final RichOp<Computers.Arity1<?, ?>> copyOp, OpEnvironment env)
	{
		this( //
			info, //
			generateOpType(info, preconverters, postconverter), //
			preconverters, //
			postconverter, //
			copyOp, //
			env //
		);
	}

	private static Type generateOpType(OpInfo info,
		List<RichOp<Function<?, ?>>> preconverter,
		RichOp<Function<?, ?>> postconverter)
	{
		Type[] inTypes = inTypes(info.inputTypes(), preconverter);
		Type outType = outType(info.outputType(), postconverter);
		Class<?> fIface = FunctionalInterfaces.findFrom(info.opType());
		return retypeOpType(fIface, inTypes, outType);
	}

	public ConvertedOpInfo( //
		OpInfo info, //
		Type opType, //
		List<RichOp<Function<?, ?>>> preconverters, //
		RichOp<Function<?, ?>> postconverter, //
		final RichOp<Computers.Arity1<?, ?>> copyOp, //
		OpEnvironment env //
	) {
		this.info = info;
		this.opType = opType;
		this.preconverters = preconverters;
		this.postconverter = postconverter;
		this.copyOp = copyOp;
		this.env = env;
		this.struct = generateStruct( //
			info, //
			opType, //
			preconverters, //
			postconverter //
		);
		this.hints = info.declaredHints().plus( //
			BaseOpHints.Conversion.FORBIDDEN, //
			"converted" //
		);
	}

	/**
	 * Helper method to generate the new {@link Struct}
	 *
	 * @param info the original {@link OpInfo}
	 * @param opType the new Op's {@link Type}
	 * @param preconverters the {@link Function}s responsible for converting Op
	 *          parameters
	 * @param postconverter the {@link Function} responsible for converting the
	 *          Op's return
	 * @return the {@link Struct} of the converted Op
	 */
	private static Struct generateStruct(OpInfo info, Type opType,
		List<RichOp<Function<?, ?>>> preconverters,
		RichOp<Function<?, ?>> postconverter)
	{
		List<Type> originalIns = new ArrayList<>(info.inputTypes());
		List<Member<?>> ioMembers = new ArrayList<>(info.struct().members());
		// If the mutable index differs between the declared Op type and the
		// requested Op type, we must move the IO memberr
		int fromIOIdx = Conversions.mutableIndexOf(Types.raw(info.opType()));
		int toIOIdx = Conversions.mutableIndexOf(Types.raw(opType));
		if (fromIOIdx != toIOIdx) {
			originalIns.add(toIOIdx, originalIns.remove(fromIOIdx));
			ioMembers.add(toIOIdx, ioMembers.remove(fromIOIdx));
		}
		// Determine the new input and output types of the Op
		Type[] inTypes = inTypes(originalIns, preconverters);
		Type outType = outType(info.outputType(), postconverter);

		// Create the functional member types of the new OpInfo
		int index = 0;
		List<FunctionalMethodType> fmts = new ArrayList<>();
		for (Member<?> m : ioMembers) {
			if (m.getIOType() == ItemIO.NONE) continue;
			Type newType = m.isInput() ? inTypes[index++] : m.isOutput() ? outType
				: null;
			fmts.add(new FunctionalMethodType(newType, m.getIOType()));
		}
		// generate new struct
		RetypingRequest r = new RetypingRequest(info.struct(), fmts);
		return Structs.from(r, opType, new OpRetypingMemberParser());
	}

	public OpInfo srcInfo() {
		return info;
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
	public String implementationName() {
		StringBuilder sb = new StringBuilder(info.implementationName());
		sb.append("|converted");
		for (Member<?> m : struct()) {
			if (m.isInput() || m.isOutput()) {
				sb.append("_");
				sb.append(Conversions.getClassName(m.getType()));
			}
		}
		return sb.toString();
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return info.getAnnotationBearer();
	}

	private final Function<?, ?> IGNORED = t -> t;

	/**
	 * Creates a <b>converted</b> version of the original Op, whose parameter
	 * types are dictated by the input types of this info's preconverters. The
	 * resulting Op uses those preconverters to convert the inputs. After invoking
	 * the original Op, this Op will use this info's postconverters to convert the
	 * output into the type requested by the user. If the request defines a
	 * preallocated output buffer, this Op will also take care to copy the
	 * postconverted output back into the user-provided output buffer.
	 *
	 * @param dependencies - this Op's dependencies
	 */
	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		final Object op = info.createOpInstance(dependencies).object();
		try {
			Object convertedOp = Conversions.javassistOp( //
				op, //
				this, //
				this.preconverters.stream().map(rich -> {
					if (rich == null) {
						return IGNORED;
					}
					return rich.asOpType();
				}).collect(Collectors.toList()), //
				this.postconverter.asOpType(), //
				this.copyOp == null ? null : this.copyOp.asOpType() //
			);
			return struct().createInstance(convertedOp);
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException(
				"Failed to invoke parameter conversion of Op: \n" + info +
					"\nProvided Op dependencies were: " + dependencies, ex);
		}
	}

	@Override
	public String toString() {
		return Infos.describe(this);
	}

	@Override
	public int compareTo(final OpInfo that) {
		// compare priorities
		if (this.priority() < that.priority()) return 1;
		if (this.priority() > that.priority()) return -1;

		// compare implementation names
		int implNameDiff = Comparisons.compare(this.implementationName(), that
			.implementationName());
		if (implNameDiff != 0) return implNameDiff;

		// compare structs if the OpInfos are "sibling" ConvertedOpInfos
		if (that instanceof ConvertedOpInfo) return compareConvertedInfos(
			(ConvertedOpInfo) that);

		return 0;
	}

	private int compareConvertedInfos(ConvertedOpInfo that) {
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
	 * For a converted Op, we define the implementation as the concatenation of:
	 * <ol>
	 * <li>The signature of all preconverters</li>
	 * <li>The signature of the postconverter</li>
	 * <li>The signature of the output copier</li>
	 * <li>The id of the source Op</li>
	 * </ol>
	 * <p>
	 */
	@Override
	public String id() {
		// original Op
		StringBuilder sb = new StringBuilder(IMPL_DECLARATION);
		// preconverters
		for (RichOp<Function<?, ?>> i : preconverters) {
			sb.append(PRECONVERTER_DELIMITER);
			sb.append(i.infoTree().signature());
		}
		// postconverter
		sb.append(POSTCONVERTER_DELIMITER);
		sb.append(postconverter.infoTree().signature());
		// output copier
		if (copyOp != null) {
			sb.append(OUTPUT_COPIER_DELIMITER);
			sb.append(copyOp.infoTree().signature());
		}
		// original info
		sb.append(ORIGINAL_INFO);
		sb.append(srcInfo().id());
		return sb.toString();
	}

	private static Type[] inTypes(List<Type> originalInputs,
		List<RichOp<Function<?, ?>>> preconverters)
	{
		Map<TypeVariable<?>, Type> typeAssigns = new HashMap<>();
		Type[] inTypes = new Type[originalInputs.size()];
		for (int i = 0; i < originalInputs.size(); i++) {
			typeAssigns.clear();
			// Start by looking at the input type T of the preconverter
			var type = preconverters.get(i).instance().getType();
			var pType = (ParameterizedType) type;
			inTypes[i] = pType.getActualTypeArguments()[0];
			// Sometimes, the type of the Op instance can contain wildcards.
			// These do not help us in determining the new type, so we have to
			// start over with the input converter input type.
			if (inTypes[i] instanceof WildcardType) {
				inTypes[i] = Ops.info(preconverters.get(i)).inputTypes().get(0);
			}
			// Infer type variables in the preconverter input w.r.t. the
			// parameter types of the ORIGINAL OpInfo
			GenericAssignability.inferTypeVariables( //
				new Type[] { pType.getActualTypeArguments()[1] }, //
				new Type[] { originalInputs.get(i) }, //
				typeAssigns //
			);
			// Map type variables in T to Types inferred above
			inTypes[i] = Types.mapVarToTypes(inTypes[i], typeAssigns);
		}
		return inTypes;
	}

	private static Type outType( //
		Type originalOutput, //
		RichOp<Function<?, ?>> postconverter //
	) {
		// Start by looking at the output type T of the postconverter
		var type = postconverter.instance().getType();
		var pType = (ParameterizedType) type;
		Type outType = pType.getActualTypeArguments()[1];
		// Sometimes, the type of the Op instance can contain wildcards.
		// These do not help us in determining the new type, so we have to
		// start over with the postconverter's output type.
		if (outType instanceof WildcardType) {
			outType = Ops.info(postconverter).outputType();
		}
		Map<TypeVariable<?>, Type> vars = new HashMap<>();
		// Infer type variables in the postconverter input w.r.t. the
		// parameter types of the ORIGINAL OpInfo
		GenericAssignability.inferTypeVariables( //
			new Type[] { pType.getActualTypeArguments()[0] }, //
			new Type[] { originalOutput }, //
			vars //
		);
		// map type variables in T to Types inferred in Step 2a
		return Types.mapVarToTypes(outType, vars);
	}

	@Override
	public double priority() {
		if (priority == null) {
			priority = calculatePriority(env);
		}
		return priority;
	}

	/**
	 * We define the priority of any {@link ConvertedOpInfo} as the sum of the
	 * following:
	 * <ul>
	 * <li>{@link Priority#VERY_LOW} to ensure that conversions are not chosen
	 * over a direct match.</li>
	 * <li>The {@link OpInfo#priority} of the source info to ensure that the
	 * conversion of a higher-priority Op wins out over the conversion of a
	 * lower-priority Op, all else equal.</li>
	 * <li>a penalty defined as a lossiness heuristic of this conversion. This
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
		double originalPriority = info.priority();

		// PENALTY
		double penalty = 0;

		List<Type> originalInputs = info.inputTypes();
		List<Type> inputs = inputTypes();
		for (int i = 0; i < inputs.size(); i++) {
			penalty += determineLoss(env, Nil.of(inputs.get(i)), Nil.of(originalInputs
				.get(i)));
		}

		Type opOutput = info.outputType();
		penalty += determineLoss(env, Nil.of(opOutput), Nil.of(outputType()));

		// PRIORITY = BASE + ORIGINAL - PENALTY
		return base + originalPriority - penalty;
	}

	/**
	 * Calls a {@code engine.lossReporter} Op to determine the <b>worst-case</b>
	 * loss from a {@code T} to a {@code R}. If no {@code engine.lossReporter}
	 * exists for such a conversion, we assume infinite loss.
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
			Hints h = new Hints( //
				BaseOpHints.Adaptation.FORBIDDEN, //
				BaseOpHints.Conversion.FORBIDDEN, //
				BaseOpHints.History.IGNORE //
			);
			LossReporter<T, R> op = env.op("engine.lossReporter", specialTypeNil,
				new Nil[] { Nil.of(nilFromType), Nil.of(nilToType) }, Nil.of(
					Double.class), h);
			return op.apply(from, to);
		}
		catch (OpMatchingException e) {
			return Double.POSITIVE_INFINITY;
		}
	}

	/**
	 * Determines the {@link Type} of a retyped Op using its old {@code Type}, a
	 * new set of {@code args} and a new {@code outType}. Used to create
	 * {@link ConvertedOpInfo}s. This method assumes that:
	 * <ul>
	 * <li>{@code originalOpRefType} is (or is a subtype of) some
	 * {@link FunctionalInterface}</li>
	 * <li>all {@link TypeVariable}s declared by that {@code FunctionalInterface}
	 * are present in the signature of that interface's single abstract
	 * method.</li>
	 * </ul>
	 *
	 * @param originalOpType - the {@link Type} declared by the source
	 *          {@link OpRequest}
	 * @param newArgs - the new argument {@link Type}s requested by the
	 *          {@link OpRequest}.
	 * @param newOutType - the new output {@link Type} requested by the
	 *          {@link OpRequest}.
	 * @return - a new {@code type} for a {@link ConvertedOpInfo}.
	 */
	private static ParameterizedType retypeOpType(Type originalOpType,
		Type[] newArgs, Type newOutType)
	{
		// only retype types that we know how to retype
		Class<?> opType = Types.raw(originalOpType);
		Method fMethod = FunctionalInterfaces.functionalMethodOf(opType);

		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();

		// solve input types
		Type[] genericParameterTypes = paramTypesFromOpType(opType, fMethod);
		GenericAssignability.inferTypeVariables(genericParameterTypes, newArgs,
			typeVarAssigns);

		// solve output type
		Type genericReturnType = returnTypeFromOpType(opType, fMethod);
		if (genericReturnType != void.class) {
			GenericAssignability.inferTypeVariables(new Type[] { genericReturnType },
				new Type[] { newOutType }, typeVarAssigns);
		}

		// build new (read: converted) Op type
		return Types.parameterize(opType, typeVarAssigns);
	}

	private static Type[] paramTypesFromOpType(Class<?> opType, Method fMethod) {
		Type[] genericParameterTypes = fMethod.getGenericParameterTypes();
		if (fMethod.getDeclaringClass().equals(opType))
			return genericParameterTypes;
		return typesFromOpType(opType, fMethod, genericParameterTypes);

	}

	private static Type returnTypeFromOpType(Class<?> opType, Method fMethod) {
		Type genericReturnType = fMethod.getGenericReturnType();
		if (fMethod.getDeclaringClass().equals(opType)) return genericReturnType;
		return typesFromOpType(opType, fMethod, genericReturnType)[0];
	}

	private static Type[] typesFromOpType(Class<?> opType, Method fMethod,
		Type... types)
	{
		Map<TypeVariable<?>, Type> map = new HashMap<>();
		Class<?> declaringClass = fMethod.getDeclaringClass();
		Type genericDeclaringClass = Types.parameterizeRaw(declaringClass);
		Type genericClass = Types.parameterizeRaw(opType);
		Type superGenericClass = Types.getExactSuperType(genericClass,
			declaringClass);
		GenericAssignability.inferTypeVariables(new Type[] {
			genericDeclaringClass }, new Type[] { superGenericClass }, map);

		return Types.mapVarToTypes(types, map);
	}

}
