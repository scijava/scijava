
package org.scijava.ops.engine.progress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.function.Functions;
import org.scijava.ops.api.ProgressReporter;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

@Plugin(type = OpCollection.class)
public class DefaultProgressReporterTest extends AbstractTestEnvironment {

	@OpField(names = "test.progressReporter")
	public final Functions.Arity3<Integer, Semaphore, Semaphore, List<Number>> primeFinder =
		(numPrimes, startLock, loopLock) -> {
			// set up progress reporter
			ProgressReporter p = new DefaultProgressReporter(numPrimes);
			ProgressReporters.setReporter(p);

			startLock.release();
			List<Number> returns = new ArrayList<>(numPrimes);
			long i = 1;
			while (returns.size() < numPrimes) {
				if (!isPrime(++i)) continue;
				returns.add(i);
				p.reportElement();
				waitOn(loopLock);
			}
			return returns;
		};

	private static void waitOn(Semaphore s) {
		try {
			s.acquire();
		}
		catch (InterruptedException exc) {
			Assert.fail();
		}
	}

	private static boolean isPrime(Long number) {
		for (int i = 2; i * i <= number; i++) {
			if (number % i == 0) {
				return false;
			}
		}
		return true;
	}

	@Test
	public void testLongOp() {
		// obtain the Op
		Functions.Arity3<Integer, Semaphore, Semaphore, List<Number>> primeFinder = //
			ops.op("test.progressReporter") //
				.inType(Integer.class, Semaphore.class, Semaphore.class)//
				.outType(new Nil<List<Number>>()
				{}) //
				.function();

		// call the Op
		Semaphore startLock = new Semaphore(0);
		Semaphore loopLock = new Semaphore(0);
		Thread t = new Thread(() -> primeFinder.apply(100, startLock, loopLock));
		t.setPriority(Thread.currentThread().getPriority() + 1);
		t.start();

		// Grab the op's ProgressReporter once it has set it up
		waitOn(startLock);
		ProgressReporter opReporter = ops.history().currentExecutions().iterator()
			.next().reporter();

		double lastProgress = 0;
		while (t.isAlive()) {
			// check for progress increases
			Assert.assertTrue(lastProgress <= opReporter.getProgress());
			lastProgress = opReporter.getProgress();
			loopLock.release();
			Thread.yield();
		}

		Assert.assertEquals(1., opReporter.getProgress(), 0.);

	}

}
