package org.scijava.ops.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.scijava.types.Types;

public interface OpWrapper<T> {
	RichOp<T> wrap(OpInstance<T> op, OpMetadata metadata);
	default Class<?> type() {
		Type wrapperType = getClass().getGenericInterfaces()[0];
		if (wrapperType instanceof ParameterizedType) {
			return Types.raw(((ParameterizedType) wrapperType).getActualTypeArguments()[0]);
		}
		return null;
	}
}
