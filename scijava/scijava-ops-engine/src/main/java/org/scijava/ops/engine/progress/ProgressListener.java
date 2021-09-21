
package org.scijava.ops.engine.progress;

@FunctionalInterface
public interface ProgressListener {

	void updateProgress(Task task);

}
