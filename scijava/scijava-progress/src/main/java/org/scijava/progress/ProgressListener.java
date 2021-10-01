
package org.scijava.progress;

@FunctionalInterface
public interface ProgressListener {

	void acknowledgeUpdate(Task task);

}
