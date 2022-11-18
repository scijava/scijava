
package org.scijava.ops.engine.reduce;

import org.scijava.ops.api.OpInfo;

public interface InfoReducer {

	boolean canReduce(OpInfo info);

	ReducedOpInfo reduce(OpInfo info, int numReductions);
}
