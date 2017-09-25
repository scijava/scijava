
package org.scijava.param;

import java.lang.reflect.Type;

import org.scijava.ValidityException;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;

/**
 * {@link Member} backed by a generic parameter of a
 * {@link FunctionalInterface}-annotated interface.
 * <p>
 * The generic parameter in question is indicated by annotating a {@link Class}
 * with {@link Parameter}.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T>
 */
public class FunctionalParameterMember<T> extends AnnotatedParameterMember<T> {

	private Struct struct;

	public FunctionalParameterMember(final Type itemType,
		final Parameter annotation) throws ValidityException
	{
		super(itemType, annotation);
		final String key = getKey();
		if (key == null || key.isEmpty()) {
			throw new ValidityException("Functional parameter must specify key");
		}
		struct = isStruct() ? ParameterStructs.structOf(getRawType()) : null;
	}

	@Override
	public Struct childStruct() {
		return struct;
	}
}
