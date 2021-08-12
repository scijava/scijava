
package org.scijava.ops.engine.matcher.impl;

import com.google.common.collect.Streams;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.scijava.ValidityProblem;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpDependencyMember;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.api.features.BaseOpHints.Adaptation;
import org.scijava.ops.engine.hint.ImmutableHints;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.struct.OpRetypingMemberParser;
import org.scijava.ops.engine.struct.RetypingRequest;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.struct.ValidityException;

/**
 * {@link OpInfo} for ops that have been adapted to some other Op type.
 * 
 * @author Gabriel Selzer
 * @see OpInfo
 */
public class OpAdaptationInfo implements OpInfo {

	private static final String IMPL_DELIMITER = "|Adaptation|";

	private final OpInfo srcInfo;
	private final OpInfo adaptorInfo;
	private final Type type;
	private final Function<Object, Object> adaptor;
	private final Hints hints;

	private Struct struct;
	private ValidityException validityException;

	public OpAdaptationInfo(OpInfo srcInfo, Type type,
		OpInfo adaptorInfo, Function<Object, Object> adaptor)
	{
		this.srcInfo = srcInfo;
		this.adaptorInfo = adaptorInfo;
		this.type = type;
		this.adaptor = adaptor;

		// NOTE: since the source Op has already been shown to be valid, there is
		// not
		// much for us to do here.
		List<ValidityProblem> problems = new ArrayList<>();
		List<FunctionalMethodType> fmts = FunctionalParameters.findFunctionalMethodTypes(type);
		
		RetypingRequest r = new RetypingRequest(srcInfo.struct(), fmts);
		struct = Structs.from(r, problems, new OpRetypingMemberParser());
		try {
			OpUtils.checkHasSingleOutput(struct);
		}
		catch (ValidityException exc) {
			problems.addAll(exc.problems());
		}
		if (!problems.isEmpty()) validityException = new ValidityException(
			problems);

		List<String> hintList = new ArrayList<>(srcInfo.declaredHints().getHints()
			.values());
		hintList.add(Adaptation.FORBIDDEN);
		this.hints = new ImmutableHints(hintList.toArray(String[]::new));
	}

	@Override
	public List<OpDependencyMember<?>> dependencies() {
		return srcInfo.dependencies();
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
		return srcInfo.implementationName() + IMPL_DELIMITER + adaptorInfo.implementationName();
	}

	/**
	 * @param dependencies - the list of depencies <b>for the source Op</b>
	 */
	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		final Object op = srcInfo.createOpInstance(dependencies).object();
		final Object adaptedOp = adaptor.apply(op);
		return struct().createInstance(adaptedOp);
	}

	@Override
	public boolean isValid() {
		return validityException == null;
	}

	@Override
	public ValidityException getValidityException() {
		return validityException;
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
		return adaptorInfo.version();
	}

	/**
	 * For an adapted Op, we define the implementation name as the concatenation
	 * of:
	 * <ol>
	 * <li>The implementation name of the <b>original info</b>
	 * <li>The adaptation delimiter
	 * <li>The implementation name of the <b>adaptor</b>
	 * </ol>
	 * <p>
	 * For example, for a source {@code com.example.foo.Bar@1.0.0} with adaptor
	 * {@code com.example.foo.BazAdaptor@1.0.0} with delimiter
	 * {@code |Adaptation|}, you might have
	 * <p>
	 * {@code com.example.foo.Bar@1.0.0|Adaptation|com.example.foo.BazAdaptor@1.0.0}
	 * <p>
	 */
	@Override
	public String id() {
		return srcInfo.id() + IMPL_DELIMITER + adaptorInfo.id();
	}

}
