/*-
 * #%L
 * Interactive tutorial for SciJava Ops.
 * %%
 * Copyright (C) 2023 - 2025 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.tutorial;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.progress.Progress;
import org.scijava.progress.Task;
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
 * <li>The number of "elements" in a computation, as well as "subtasks", i.e.
 * dependent Ops</li>
 * <li>When each "element" has been completed</li>
 * </ol>
 * <p>
 * Note the difference between an "element" of computation, and a "subtask"
 * <ul>
 * <li><em>elements</em>s are pieces of computation done <b>by the Op</b></li>
 * <li><em>subtask</em>s are phases of computation done <b>by other Ops</b></li>
 * </ul>
 * Users are then notified by the progress of Ops by installing
 * {@link Consumer}{@code <Task>}s.
 *
 * @author Gabriel Selzer
 */
public class ReportingProgress {

	/**
	 * An Op that reports its progress while finding prime numbers.
	 *
	 * @input numPrimes the quantity of unique prime numbers to find
	 * @output a {@link List} of prime numbers
	 * @implNote op names="tutorial.long.op"
	 */
	public final Function<Integer, List<Long>> primes = numPrimes -> {
		var primes = new ArrayList<Long>();
		long val = 1, sqrt;
		boolean couldBePrime;

		// If you have N discrete packets of computation, you should call
		// Progress.defineTotalProgress(N)
		// If you have N discrete packets of computation and want to call Op
		// dependencies M times, you should call
		// Progress.defineTotalProgress(N, M)

		// Here, we want to update the progress every time we find a new prime.
		// We call no Op dependencies, thus the call looks like:
		Progress.defineTotal(numPrimes);

		// Progress is defined within the range [0, 1],
		// where 0 denotes an Op that has not yet started.
		// and 1 denotes completion.

		// Find each of our primes
		while (primes.size() < numPrimes) {
			sqrt = (long) Math.sqrt(++val);
			couldBePrime = true;
			// Evaluate "prime-ness" of number
			for (var i = 2; i <= sqrt; i++) {
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
        var ops = OpEnvironment.build();
		// To enable Progress Reporting, you must enable the progress tracking hint!
		ops.setDefaultHints(new Hints("progress.TRACK"));

		// Consumer<Task>s consume task updates.
		// This Consumer simply logs to standard output, but we could print
		// out something else, or pass this information somewhere else.
		Consumer<Task> l = System.out::println;
		// To listen to Op progress updates, the Consumer must be registered
		// through the Progress API. To listen to all Op executions, use the
		// following call:
		Progress.addGlobalListener(l);
		// If listening to every Op would be overwhelming, the Progress API also
		// allows Consumers to be registered for a specific Op, using the
		// following call:
		// Progress.addListener(op, l);

		// Get the function.
		var op = ops.op("tutorial.long.op") //
			.inType(Integer.class) //
			.outType(new Nil<List<Long>>()
			{}) //
			.function();

		// When we apply the Op, we will automatically print the progress out to
		// the console, thanks to our Consumers above.
		var numPrimes = 100;
		var primes = op.apply(numPrimes);
		System.out.println("First " + numPrimes + " primes: " + primes);

	}

}
