
package org.scijava.ops.api;

import java.lang.reflect.Type;

import org.scijava.types.GenericTyped;

public interface RichOp extends GenericTyped {

	Object op();

	OpMetadata metadata();

	void preprocess(Object... inputs);

	void postprocess(Object output);

	@Override
	default Type getType() {
		return metadata().type();
	}

}
