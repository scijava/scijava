package org.scijava.ops.matcher;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.List;

import org.scijava.ops.OpUtils;
import org.scijava.param.ParameterStructs;
import org.scijava.param.ValidityException;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * {@link OpInfo} for ops that have been adapted to some other Op type.
 * 
 * @author Gabriel Selzer
 *
 */
public class OpAdaptationInfo implements OpInfo {

	private OpInfo srcInfo;
	private Type type;

	private Struct struct;
	private ValidityException validityException;

	public OpAdaptationInfo(OpInfo srcInfo, Type type) {
		this.srcInfo = srcInfo;
		this.type = type;

		// NOTE: since the source Op has already been shown to be valid, there is not
		// much for us to do here.
		try {
			struct = ParameterStructs.structOf(srcInfo, type);
			OpUtils.checkHasSingleOutput(struct);
		} catch (ValidityException e) {
			validityException = e;
		}
	}

	@Override
	public Type opType() {
		return type;
	}

	@Override
	public Struct struct() {
		// TODO Can we build this struct?
		return struct;
	}

	// we want the original op to have priority over this one.
	@Override
	public double priority() {
		return srcInfo.priority() - 1;
	}

	@Override
	public String implementationName() {
		return srcInfo.implementationName();
	}

	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		// TODO: does this method make sense for this kind of Op? It really doesn't have
		// any dependencies...if so, we should implement this method.
		throw new UnsupportedOperationException(
				"StructInstance unavailable for adaptation of " + srcInfo.implementationName());

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
