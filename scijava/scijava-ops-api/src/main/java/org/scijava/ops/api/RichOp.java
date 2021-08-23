
package org.scijava.ops.api;

import java.lang.reflect.Type;

import org.scijava.types.GenericTyped;

/**
 * An {@link OpInstance} with state (i.e. an {@link OpMetadata})
 * 
 * @author Gabriel Selzer
 * @param <T>
 */
public interface RichOp<T> extends GenericTyped {

	OpInstance<T> instance();

	default T op() {
		return instance().op();
	}

	default InfoChain infoChain() {
		return instance().infoChain();
	}

	/**
	 * Returns this {@link RichOp} as its op interface {@link Type}
	 * <p>
	 * NB Implementations of {@link RichOp} must also implement the op interface
	 * to ensure that this method works.
	 * 
	 * @return this {@link RichOp} as the type of its op interface
	 */
	@SuppressWarnings("unchecked")
	default T asOpType() {
		return (T) this;
	}

	OpMetadata metadata();

	void preprocess(Object... inputs);

	void postprocess(Object output);

	@Override
	default Type getType() {
		return metadata().type();
	}

}
