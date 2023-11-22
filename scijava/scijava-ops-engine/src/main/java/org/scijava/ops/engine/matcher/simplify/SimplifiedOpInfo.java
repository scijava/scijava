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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.common3.Comparisons;
import org.scijava.function.Computers;
import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.Ops;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.OpDescription;
import org.scijava.ops.engine.struct.OpRetypingMemberParser;
import org.scijava.ops.engine.struct.RetypingRequest;
import org.scijava.priority.Priority;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.types.Types;
import org.scijava.types.inference.FunctionalInterfaces;
import org.scijava.types.inference.GenericAssignability;

/**
 * An {@link OpInfo} whose inputs and outputs have been simplified to "simple"
 * types. The inputs are simplified through the use of {@code focus}
 * {@link Function} Ops, enabling the extensible definition of "simple" types
 * and of the set of "focused" types that reduce down to that "simple" type. The
 * output is simplified through the user of a {@code simplify} {@link Function}
 * Op, and, if the Op defines a preallocated output buffer, a {@code copy}
 * {@link Computers.Arity1} Op to place the result of the Op back into the
 * passed output buffer.
 * <p>
 * As an example, consider a {@code Function<Double, Double>}. If we have a
 * {@code simplify} Op that goes from {@link Double} to {@link Number}
 * <b>and</b> a {@code focus} Op that can go from {@link Number} to
 * {@link Double}, then we can construct a {@code Function<Number, Number>} Op
 * by using the {@code focus} Op on our input {@link Number}, passing that
 * output into our original Op, and passing the output {@link Double} through
 * the {@code simplify} Op before yielding the output back to the user.
 * </p>
 * 
 * @author Gabriel Selzer
 */
public class SimplifiedOpInfo implements OpInfo {

	/** Identifiers for declaring a simplification in an Op signature **/
	protected static final String IMPL_DECLARATION = "|Simplification:";
	protected static final String INPUT_FOCUSER_DELIMITER = "|InputFocuser:";
	protected static final String OUTPUT_SIMPLIFIER_DELIMITER =
		"|OutputSimplifier:";
	protected static final String OUTPUT_COPIER_DELIMITER = "|OutputCopier:";
	protected static final String ORIGINAL_INFO = "|OriginalInfo:";

	private final OpInfo info;
	final List<RichOp<Function<?, ?>>> inputFocusers;
	final RichOp<Function<?, ?>> outputSimplifier;
	final RichOp<Computers.Arity1<?, ?>> copyOp;
	private final ParameterizedType opType;
	private final Struct struct;
	private final double priority;
	private final Hints hints;

	public SimplifiedOpInfo(OpInfo info,
		List<RichOp<Function<?, ?>>> inputFocusers,
		RichOp<Function<?, ?>> outputSimplifier,
		final RichOp<Computers.Arity1<?, ?>> copyOp)
	{
		this.info = info;
		this.inputFocusers = inputFocusers;
		this.outputSimplifier = outputSimplifier;
		this.copyOp = copyOp;

		Type[] inTypes = inTypes(info.inputTypes().toArray(Type[]::new),
			inputFocusers);
		Type outType = outType(info.outputType(), outputSimplifier);

		List<Member<?>> ioMembers = info.struct().members();
		int index = 0;
		List<FunctionalMethodType> fmts = new ArrayList<>();
		for (Member<?> m : ioMembers) {
			if (m.getIOType() == ItemIO.NONE) continue;
			Type newType = m.isInput() ? inTypes[index++] : m.isOutput() ? outType
				: null;
			fmts.add(new FunctionalMethodType(newType, m.getIOType()));
		}
		// generate new output fmt
		Class<?> fIface = FunctionalInterfaces.findFrom(info.opType());
		this.opType = SimplificationUtils.retypeOpType(fIface, inTypes, outType);
		RetypingRequest r = new RetypingRequest(info.struct(), fmts);
		this.struct = Structs.from(r, opType, new OpRetypingMemberParser());

		this.priority = Priority.LAST;
		this.hints = info.declaredHints().plus(BaseOpHints.Simplification.FORBIDDEN,
			"simple");
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
	public double priority() {
		return priority;
	}

	@Override
	public String implementationName() {
		return info.implementationName() + "|simple";
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return info.getAnnotationBearer();
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
		final Object op = info.createOpInstance(dependencies).object();
		try {
			Object simpleOp = SimplificationUtils.javassistOp( //
				op, //
				this, //
				this.inputFocusers.stream().map(RichOp::asOpType).collect(Collectors
					.toList()), //
				this.outputSimplifier.asOpType(), //
				this.copyOp == null ? null : this.copyOp.asOpType() //
			);
			return struct().createInstance(simpleOp);
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException(
				"Failed to invoke simplification of Op: \n" + info +
					"\nProvided Op dependencies were: " + dependencies, ex);
		}
	}

	@Override
	public String toString() {
		return OpDescription.verbose(this);
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

		// compare structs if the OpInfos are "sibling" SimplifiedOpInfos
		if (that instanceof SimplifiedOpInfo) return compareToSimplifiedInfo(
			(SimplifiedOpInfo) that);

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
	 * For a simplified Op, we define the implementation as the concatenation of:
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
		// input focusers
		for (RichOp<Function<?, ?>> i : inputFocusers) {
			sb.append(INPUT_FOCUSER_DELIMITER);
			sb.append(i.infoTree().signature());
		}
		// output simplifier
		sb.append(OUTPUT_SIMPLIFIER_DELIMITER);
		sb.append(outputSimplifier.infoTree().signature());
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

	private Type[] inTypes(Type[] originalInputs,
		List<RichOp<Function<?, ?>>> inputFocusers)
	{
		Map<TypeVariable<?>, Type> typeAssigns = new HashMap<>();
		Type[] inTypes = new Type[originalInputs.length];
		// NB: This feels kind of inefficient, but we have to do each individually
		// to avoid the case that the same Op is being used for different types.
		// Here's one use edge case - suppose the Op Identity<T> maps A, B, and C.
		// The new inTypes should thus be A, B, and C, but if we do them all
		// together we'll get inTypes Object from the output of the focusers, as
		// there's only one type variable to map across the three inputs.
		for (int i = 0; i < originalInputs.length; i++) {
			typeAssigns.clear();
			var info = Ops.info(inputFocusers.get(i));
			GenericAssignability.inferTypeVariables(new Type[] { info.outputType() },
				new Type[] { originalInputs[i] }, typeAssigns);
			inTypes[i] = Types.mapVarToTypes(info.inputTypes().get(0), typeAssigns);
		}
		return inTypes;
	}

	private Type outType(Type originalOutput,
		RichOp<Function<?, ?>> outputSimplifier)
	{
		Map<TypeVariable<?>, Type> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables( //
			new Type[] { Ops.info(outputSimplifier).inputTypes().get(0) }, //
			new Type[] { originalOutput }, //
			typeAssigns //
		);
		return Types.mapVarToTypes(Ops.info(outputSimplifier).outputType(),
			typeAssigns);
	}

}
