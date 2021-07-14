package org.scijava.ops.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;

import org.scijava.ops.OpHistoryService;
import org.scijava.ops.OpInfo;
import org.scijava.ops.hints.Hints;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.types.Types;

public interface OpWrapper<T> extends SciJavaPlugin {
	T wrap(T op, OpInfo info, Hints hints, OpHistoryService history, UUID uuid, Type reifiedType);
	default Class<?> type() {
		Type wrapperType = getClass().getGenericInterfaces()[0];
		if (wrapperType instanceof ParameterizedType) {
			return Types.raw(((ParameterizedType) wrapperType).getActualTypeArguments()[0]);
		}
		return null;
	}
}
