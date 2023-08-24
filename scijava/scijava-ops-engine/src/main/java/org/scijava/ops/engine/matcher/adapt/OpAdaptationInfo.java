
package org.scijava.ops.engine.matcher.adapt;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.scijava.common3.validity.ValidityException;
import org.scijava.common3.validity.ValidityProblem;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpDescription;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.engine.BaseOpHints.Adaptation;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.struct.OpRetypingMemberParser;
import org.scijava.ops.engine.struct.RetypingRequest;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.types.Nil;

/**
 * {@link OpInfo} for ops that have been adapted to some other Op type.
 * 
 * @author Gabriel Selzer
 * @see OpInfo
 */
public class OpAdaptationInfo implements OpInfo {

	/** Identifiers used for an adapted Op in a signature **/
	protected static final String IMPL_DECLARATION = "|Adaptation:";
	protected static final String ADAPTOR = "|Adaptor:";
	protected static final String ORIGINAL = "|OriginalOp:";

	private final OpInfo srcInfo;
	private final InfoChain adaptorChain;
	private final Type type;
	private final Hints hints;

	private Struct struct;

	public OpAdaptationInfo(OpInfo srcInfo, Type type,
		InfoChain adaptorChain)
	{
		this.srcInfo = srcInfo;
		this.adaptorChain = adaptorChain;
		this.type = type;
		this.hints = srcInfo.declaredHints().plus(Adaptation.FORBIDDEN);

		// NOTE: since the source Op has already been shown to be valid, there is
		// not much for us to do here.
		List<ValidityProblem> problems = new ArrayList<>();
		List<FunctionalMethodType> fmts = FunctionalParameters.findFunctionalMethodTypes(type);
		
		RetypingRequest r = new RetypingRequest(srcInfo.struct(), fmts);
		struct = Structs.from(r, type, problems, new OpRetypingMemberParser());
		OpUtils.ensureHasSingleOutput(struct, problems);
		if (!problems.isEmpty()) {
			throw new ValidityException(problems);
		}
	}

	@Override
	public List<String> names() {
		return srcInfo.names();
	}

	@Override
	public Type opType() {
		return type;
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public Hints declaredHints() {
		return hints;
	}

	// we want the original op to have priority over this one.
	@Override
	public double priority() {
		return srcInfo.priority() - 1;
	}

	@Override
	public String implementationName() {
		return srcInfo.implementationName() + ADAPTOR + adaptorChain.signature();
	}

	/**
	 * @param dependencies - the list of depencies <b>for the source Op</b>
	 */
	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		@SuppressWarnings("unchecked")
		OpInstance<Function<Object, Object>> adaptorInstance =
			(OpInstance<Function<Object, Object>>) adaptorChain.newInstance(
				new Nil<Function<Object, Object>>()
				{}.getType());
		final Object op = srcInfo.createOpInstance(dependencies).object();
		final Object adaptedOp = adaptorInstance.op().apply(op);
		return struct().createInstance(adaptedOp);
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return srcInfo.getAnnotationBearer();
	}

	/**
	 * Returns the version of the adapted Op.
	 * <p>
	 * Note that {@code adaptorInfo.version()} is used as the Op returned is an
	 * inner class of the adaptor Op, and will thus have the same version as the
	 * adaptor.
	 */
	@Override
	public String version() {
		return adaptorChain.info().version();
	}

	/**
	 * For an adapted Op, we define the implementation name as the concatenation
	 * of:
	 * <ol>
	 * <li>The signature of the <b>adaptor</b> {@link InfoChain}</li>
	 * <li>The adaptation delimiter</li>
	 * <li>The implementation name of the <b>original info</b></li>
	 * </ol>
	 * <p>
	 * For example, for a source {@code com.example.foo.Bar@1.0.0} with adaptor
	 * {@code com.example.foo.BazAdaptor@1.0.0} with delimiter
	 * {@code |Adaptation|}, you might have
	 * <p>
	 * {@code com.example.foo.BazAdaptor@1.0.0{}|Adaptation|com.example.foo.Bar@1.0.0}
	 * <p>
	 */
	@Override
	public String id() {
		return IMPL_DECLARATION + ADAPTOR + adaptorChain.signature() + ORIGINAL + srcInfo.id();
	}

	@Override
	public String toString() {
		return OpDescription.basic(this);
	}
}
