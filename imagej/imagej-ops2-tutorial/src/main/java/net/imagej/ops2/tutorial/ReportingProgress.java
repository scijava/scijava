package net.imagej.ops2.tutorial;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.progress.Progress;
import org.scijava.progress.ProgressListener;
import org.scijava.types.Nil;

/**
 * Long-running Ops can be confusing for users. By defining and then reporting
 * progress, Ops can tell the user how far it has gotten in the computation.
 * <p>
 * At the heart of progress reporting is the {@link Progress} class, responsible
 * for conveying Ops' progress to users. SciJava Progress defines progress on a
 * scale of [0, 1], where:
 * <ul>
 * <li>0 defines work that has not yet started</li>
 * <li>1 defines work that has been completed</li>
 * <li>values in between define work in progress</li>
 * </ul>
 * <p>
 * Ops tell the {@link Progress} a few things:
 * <ol>
 * <li>The number of "stages" of computation, including any "subtasks"</li>
 * <li>The number of tasks in each stage</li>
 * <li>When each task has been completed</li>
 * </ol>
 * <p>
 * Note the difference between a "stage" of computation, and a "subtask"
 * <ul>
 * <li><em>stage</em>s are phases of computation done <b>by the Op</b></li>
 * <li><em>subtask</em>s are phases of computation done <b>by other Ops</b></li>
 * </ul>
 * Users are then notified by the progress of Ops by installing
 * {@link ProgressListener}s.
 * 
 * @author Gabriel Selzer
 */
public class ReportingProgress implements OpCollection {

	@OpField(names="tutorial.long.op")
	public final Function<Integer, List<Long>> primes = numPrimes -> {
		var primes = new ArrayList<Long>();
		long val = 1, sqrt;
		boolean couldBePrime;

		// Define the number of stages, and the number of subtasks
		// One stage - finding the primes
		// Zero subtasks - we call no other Ops
		Progress.defineTotalProgress(1, 0);
		// Progress is defined within the range [0, 1],
		// where 0 denotes an Op that has not yet started.
		// and 1 denotes completion.

		// setStageMax is used to define the denominator for the Progress fraction.
		// If you have N discrete packets of computation, you should call
		// Progress.setStageMax(N)
		Progress.setStageMax(numPrimes);
		// Find each of our primes
		while(primes.size() < numPrimes) {
			sqrt = (long) Math.sqrt(++val);
			couldBePrime = true;
			// Evaluate "prime-ness" of number
			for(int i = 2; i <= sqrt; i++) {
				if (val % i == 0) {
					couldBePrime = false;
					break;
				}
			}
			if (couldBePrime) {
				// val is a prime!
				primes.add(val);
				// Progress.update() increments the numerator of Progress,
				// identifying that one (more) discrete packet of computation is done.
				Progress.update();
			}
		}

		return primes;
	};

	public static void main(String... args) {
		OpEnvironment ops = OpEnvironment.getEnvironment();

		// ProgressListeners consume task updates.
		// This ProgressListener simply prints out the status of the Op
		// to the console, but we could print out something else,
		// or pass this information somewhere else.
		ProgressListener l = //
			task -> System.out.printf("Op progress: %.2f\n", task.progress());

		// Get the function.
		var op = ops.unary("tutorial.long.op") //
			.inType(Integer.class) //
			.outType(new Nil<List<Long>>() {}) //
			.function();
		// Listening to every Op would be overwhelming.
		// Ops must be deliberately linked to a ProgressListener through the
		// Progress API.
		Progress.addListener(op, l);

		// When we apply the Op, we will automatically print the progress out to
		// the console, thanks to our ProgressListener above.
		var numPrimes = 100;
		var primes = op.apply(numPrimes);
		System.out.println("First " + numPrimes + " primes: " + primes);


	}

}
