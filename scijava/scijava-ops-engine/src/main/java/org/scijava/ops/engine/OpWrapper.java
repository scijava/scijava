package org.scijava.ops.engine;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.RichOp;
import org.scijava.types.Types;

/**
 * An object that can wrap an Op into a {@link RichOp}
 * @param <T> the {@link Type} of the Op
 */
public interface OpWrapper<T> {

	/**
	 * Wraps an Op into a {@link RichOp}
	 * @param op an Op
	 * @param env the {@link OpEnvironment} that produced the Op
	 * @param hints the {@link Hints} used to produce the Op
	 * @return a {@link RichOp} wrapping {@code op}
	 */
	RichOp<T> wrap(OpInstance<T> op, OpEnvironment env, Hints hints);

	default Class<?> type() {
		Type wrapperType = getClass().getGenericInterfaces()[0];
		if (wrapperType instanceof ParameterizedType) {
			return Types.raw(((ParameterizedType) wrapperType).getActualTypeArguments()[0]);
		}
		return null;
	}
}
