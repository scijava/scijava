package org.scijava.ops.api;

import java.util.UUID;

import org.scijava.types.GenericTyped;

public interface RichOp extends GenericTyped {

	OpHistory history();

	OpInfo info();

	UUID executionID();

	Hints hints();

	void preprocess(Object... inputs);

	void postprocess(Object output);

}
