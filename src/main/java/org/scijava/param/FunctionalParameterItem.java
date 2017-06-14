
package org.scijava.param;

import java.lang.reflect.Type;

import org.scijava.ValidityException;
import org.scijava.struct.StructInfo;
import org.scijava.struct.StructItem;

/**
 * {@link StructItem} backed by a generic parameter of a
 * {@link FunctionalInterface}-annotated interface.
 * <p>
 * The generic parameter in question is indicated by annotating a {@link Class}
 * with {@link Parameter}.
 * </p>
 *
 * @author Curtis Rueden
 * @param <T>
 */
public class FunctionalParameterItem<T> extends AnnotatedParameterItem<T> {

	private StructInfo<? extends ParameterItem<?>> info;

	public FunctionalParameterItem(final Type itemType,
		final Parameter annotation) throws ValidityException
	{
		super(itemType, annotation);
		final String key = getKey();
		if (key == null || key.isEmpty()) {
			throw new ValidityException("Functional parameter must specify key");
		}
		info = isStruct() ? ParameterStructs.infoOf(getRawType()) : null;
	}

	@Override
	public StructInfo<? extends ParameterItem<?>> childInfo() {
		return info;
	}
}
