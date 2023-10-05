
package org.scijava.ops.api;

import java.lang.reflect.Type;

import org.scijava.types.GenericTyped;

/**
 * An {@link OpInstance} with state
 * <p>
 * Each {@link RichOp} has <b>one</b> {@link OpInstance}, and <b>one</b>
 * </p>
 * 
 * @author Gabriel Selzer
 * @param <T> the functional {@link Type} of the wrapped Op
 */
public interface RichOp<T> extends GenericTyped {

	OpInstance<T> instance();

	OpEnvironment env();

	Hints hints();

	default T op() {
		return instance().op();
	}

	@Override
	default Type getType() {
		return instance().getType();
	}

	default InfoTree infoChain() {
		return instance().infoTree();
	}

	/**
	 * Returns this {@link RichOp} as its op interface {@link Type}
	 *
	 * @return this {@link RichOp} as the type of its op interface
	 */
	T asOpType();

	void preprocess(Object... inputs);

	void postprocess(Object output);

}
