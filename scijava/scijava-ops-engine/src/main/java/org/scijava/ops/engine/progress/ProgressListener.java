
package org.scijava.ops.engine.progress;

@FunctionalInterface
public interface ProgressListener {

	void acknowledgeUpdate(Task task);

}
