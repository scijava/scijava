
package org.scijava.progress;

/**
 * Simple {@link ProgressListener} logging updates to standard output.
 *
 * @author Gabriel Selzer
 */
public class StandardOutputProgressLogger implements ProgressListener {

	@Override
	public void acknowledgeUpdate(Task task) {
		if (task.isComplete()) {
			System.out.printf("Progress of %s: Complete\n", task.description());
		}
		else {
			System.out.printf( //
				"Progress of %s: %.2f\n", //
				task.description(), //
				task.progress() //
			);
		}
	}
}
