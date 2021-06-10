package org.scijava.ops.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.scijava.ops.OpInfo;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.types.Types;

public interface OpWrapper<T> extends SciJavaPlugin {
	T wrap(T op, OpInfo info, Type reifiedType);
	default Class<?> type() {
		Type wrapperType = getClass().getGenericInterfaces()[0];
		if (wrapperType instanceof ParameterizedType) {
			return Types.raw(((ParameterizedType) wrapperType).getActualTypeArguments()[0]);
		}
		return null;
	}
}
