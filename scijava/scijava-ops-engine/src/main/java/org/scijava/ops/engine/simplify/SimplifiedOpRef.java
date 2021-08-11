
package org.scijava.ops.engine.simplify;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.scijava.function.Computers;
import org.scijava.function.Computers.Arity1;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.api.BaseOpHints.Adaptation;
import org.scijava.ops.api.BaseOpHints.Simplification;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.OpMatchingException;
import org.scijava.types.Nil;
import org.scijava.types.Types;

public class SimplifiedOpRef implements OpRef {

	/** Name of the op, or null for any name. */
	private final String name;

	/** Raw type of the request */
	private final Class<?> rawType;

	private final OpRef srcRef;
	private final List<List<OpInfo>> simplifierSets;
	private final List<OpInfo> outputFocusers;
	private final Optional<Computers.Arity1<?, ?>> copyOp;

	private SimplifiedOpRef(OpRef ref, OpEnvironment env) {
		// TODO: this is probably incorrect
		this.name = ref.getName();
		this.rawType = Types.raw(ref.getType());
		this.srcRef = ref;
		this.simplifierSets = SimplificationUtils.simplifyArgs(env, ref.getArgs());
		this.outputFocusers = SimplificationUtils.getFocusers(env, ref
			.getOutType());
		this.copyOp = Optional.empty();
	}

	private SimplifiedOpRef(OpRef ref, OpEnvironment env,
		Computers.Arity1<?, ?> copyOp)
	{
		this.name = ref.getName();
		this.rawType = Types.raw(ref.getType());
		this.srcRef = ref;
		this.simplifierSets = SimplificationUtils.simplifyArgs(env, ref.getArgs());
		this.outputFocusers = SimplificationUtils.getFocusers(env, ref
			.getOutType());
		this.copyOp = Optional.of(copyOp);
	}

	public OpRef srcRef() {
		return srcRef;
	}

	public Class<?> rawType() {
		return rawType;
	}

	public List<List<OpInfo>> simplifierSets() {
		return simplifierSets;
	}

	public List<OpInfo> outputFocusers() {
		return outputFocusers;
	}

	public Optional<Computers.Arity1<?, ?>> copyOp() {
		return copyOp;
	}

	public static SimplifiedOpRef simplificationOf(OpEnvironment env, OpRef ref,
		Hints hints)
	{
		Class<?> opType = Types.raw(ref.getType());
		int mutableIndex = SimplificationUtils.findMutableArgIndex(opType);
		if (mutableIndex == -1) return new SimplifiedOpRef(ref, env);

		// if the Op's output is mutable, we will also need a copy Op for it.
		Computers.Arity1<?, ?> copyOp = simplifierCopyOp(env, ref
			.getArgs()[mutableIndex], hints);
		return new SimplifiedOpRef(ref, env, copyOp);
	}

	/**
	 * Finds a {@code copy} Op designed to copy an Op's output (of {@link Type}
	 * {@code copyType}) back into the preallocated output during simplification.
	 * <p>
	 * NB Simplification is forbidden here because we are asking for a
	 * {@code Computers.Arity1<T, T>} copy Op (for some {@link Type}
	 * {@code type}). Suppose that no direct match existed, and we tried to find a
	 * simplified version. This simplified version, because it is a
	 * Computers.Arity1, would need a {@lnk Computers.Arity<T, T>} copy Op to copy
	 * the output of the simplified Op back into the preallocated output. But this
	 * call is already identical to the Op we asked for, and we know that there is
	 * no direct match, thus we go again into simplification. This thus causes an
	 * infinite loop (and eventually a {@link StackOverflowError}. This means that
	 * we cannot find a simplified copy Op <b>unless a direct match can be
	 * found</b>, at which point we might as well just use the direct match.
	 * <p>
	 * Adaptation is similarly forbidden, as to convert most Op types to
	 * {@link Arity1} you would need an identical copy Op.
	 * 
	 * @param copyType - the {@link Type} that we need to be able to copy
	 * @param hints
	 * @return an {@code Op} able to copy data between {@link Object}s of
	 *         {@link Type} {@code copyType}
	 * @throws OpMatchingException
	 */
	private static Computers.Arity1<?, ?> simplifierCopyOp(OpEnvironment env, Type copyType, Hints hints) throws OpMatchingException{
			Hints hintsCopy = new DefaultHints(hints.getHints());
			hintsCopy.setHint(Adaptation.FORBIDDEN);
			hintsCopy.setHint(Simplification.FORBIDDEN);

			Nil<?> copyNil = Nil.of(copyType);
			Type copierType = Types.parameterize(Computers.Arity1.class, new Type[] {copyType, copyType});
			return (Arity1<?, ?>) env.op("copy", Nil.of(copierType), new Nil<?>[] {copyNil, copyNil}, copyNil, hintsCopy);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		throw new UnsupportedOperationException(
			"The type of a SimplifiedOpRef is indeterminate; it must be matched with a OpInfo to form a concrete Type");
	}

	@Override
	public Type getOutType() {
		throw new UnsupportedOperationException(
			"The output type of a SimplifiedOpRef is indeterminate; it must be matched with a OpInfo to form a concrete Type");
	}

	@Override
	public Type[] getArgs() {
		throw new UnsupportedOperationException(
			"The output type of a SimplifiedOpRef is indeterminate; it must be matched with a OpInfo to form a concrete Type");
	}

	@Override
	public String getLabel() {
		return "Simplification of " + srcRef.getLabel();
	}

	@Override
	public boolean typesMatch(Type opType) {
		return typesMatch(opType, new HashMap<>());
	}

	@Override
	public boolean typesMatch(Type opType,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		throw new UnsupportedOperationException(
			"The type of a SimplifiedOpRef is indeterminate; it must be matched with an OpInfo to form a concrete Type!");
	}

}
