package org.scijava.ops.matcher;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.ops.Hints;
import org.scijava.ops.OpDependencyMember;
import org.scijava.ops.OpHints;
import org.scijava.ops.OpInfo;
import org.scijava.ops.OpUtils;
import org.scijava.ops.BaseOpHints.Adaptation;
import org.scijava.ops.hint.ImmutableHints;
import org.scijava.param.ParameterStructs;
import org.scijava.param.ValidityException;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * {@link OpInfo} for ops that have been adapted to some other Op type.
 * 
 * @author Gabriel Selzer
 * @see OpInfo
 */
public class OpAdaptationInfo implements OpInfo {

	private OpInfo srcInfo;
	private Type type;
	private Function<Object, Object> adaptor;
	private final Hints hints;

	private Struct struct;
	private ValidityException validityException;

	public OpAdaptationInfo(OpInfo srcInfo, Type type, Function<Object, Object> adaptor) {
		this.srcInfo = srcInfo;
		this.type = type;
		this.adaptor = adaptor;

		// NOTE: since the source Op has already been shown to be valid, there is not
		// much for us to do here.
		try {
			struct = ParameterStructs.structOf(srcInfo, type);
			OpUtils.checkHasSingleOutput(struct);
		} catch (ValidityException e) {
			validityException = e;
		}

		List<String> hintList = new ArrayList<>(srcInfo.declaredHints().getHints().values());
		hintList.remove(Adaptation.ALLOWED);
		hintList.add(Adaptation.FORBIDDEN);
		this.hints = new ImmutableHints(hintList.toArray(String[]::new));
	}

	@Override
	public Hints formHints(OpHints h) {
		// NB we don't use Arrays.toList() here because we cannot add to that list!
		List<String> hintList = Arrays.stream(h.hints()).collect(Collectors.toList());
		hintList.remove(Adaptation.ALLOWED);
		hintList.add(Adaptation.FORBIDDEN);
		return new ImmutableHints(hintList.toArray(String[]::new));
	}

	@Override
	public List<OpDependencyMember<?>> dependencies() {
		return srcInfo.dependencies();
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
		return srcInfo.implementationName() + " adapted to " + type
			.toString();
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

}
