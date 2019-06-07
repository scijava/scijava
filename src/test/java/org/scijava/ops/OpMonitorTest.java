package org.scijava.ops;

import java.math.BigInteger;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.DefaultOpMonitor;
import org.scijava.ops.core.Op;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Tests correct functionality of {@link OpMonitor}
 * 
 * @author Marcel Wiedenmann
 * @author Gabriel Selzer
 *
 */
public class OpMonitorTest extends AbstractTestEnvironment {

	/**
	 * Basic test of cancellation on the same thread
	 */
	@Test(expected = CancellationException.class)
	public void testCancellation() {
		Function<OpMonitor, BigInteger> bigOp = Functions.unary(ops, "test.opMonitor", new Nil<OpMonitor>() {},
				new Nil<BigInteger>() {});
		OpMonitor monitor = new DefaultOpMonitor();
		monitor.cancel();
		// NOTE: we have to cancel the op before we call it because the execution of
		// this Op and the test that call it are on the same thread.
		bigOp.apply(monitor);
	}

	/**
	 * Tests cancellation over different threads
	 * 
	 * @throws InterruptedException
	 *             - should not happen
	 */
	@Test(expected = CancellationException.class)
	public void testCancellationDifferentThread() throws InterruptedException {
		Function<OpMonitor, BigInteger> bigOp = Functions.unary(ops, "test.opMonitor", new Nil<OpMonitor>() {},
				new Nil<BigInteger>() {});
		OpMonitor monitor = new DefaultOpMonitor();
		try {
			Future<BigInteger> future = Executors.newSingleThreadExecutor().submit(() -> bigOp.apply(monitor));
			monitor.cancel();
			future.get();
		} catch (ExecutionException exc) {
			Throwable cancellationException = exc.getCause();
			assert cancellationException instanceof CancellationException;
			throw (CancellationException) cancellationException;
		}
	}
	
	@Test
	public void testProgress() throws InterruptedException, ExecutionException{
		BiFunction<OpMonitor, BigInteger, BigInteger> bigOp = Functions.binary(ops, "test.progress", new Nil<OpMonitor>() {},
				new Nil<BigInteger>() {}, new Nil<BigInteger>() {});
		
		OpMonitor monitor = new DefaultOpMonitor();
		BigInteger target = BigInteger.valueOf(1000000);
		double progress = monitor.getProgress();
		assert progress == 0;
		Future<BigInteger> future = Executors.newSingleThreadExecutor().submit(() -> bigOp.apply(monitor, target));
		while(!future.isDone()){
			assert progress <= monitor.getProgress();
			progress = monitor.getProgress();
		}
		BigInteger bi = future.get();
		assert bi.equals(target);
		assert progress == 1;
	}

}

@Plugin(type = Op.class, name = "test.opMonitor")
@Parameter(key = "monitor")
@Parameter(key = "bigInteger", type = ItemIO.OUTPUT)
class InfiniteOp implements Function<OpMonitor, BigInteger> {

	@Override
	public BigInteger apply(OpMonitor opMonitor) {
		BigInteger bi = BigInteger.valueOf(0);
		while (true) {
			opMonitor.checkCanceled();
			bi.add(BigInteger.ONE);
			// will never be true
			if (bi.compareTo(BigInteger.valueOf(-1)) < 0)
				break;
		}
		return bi;
	}

}

@Plugin(type = Op.class, name = "test.progress")
@Parameter(key = "monitor")
@Parameter(key = "target")
@Parameter(key = "output", type = ItemIO.OUTPUT)
class CountingOp implements BiFunction<OpMonitor, BigInteger, BigInteger> {

	@Override
	public BigInteger apply(OpMonitor opMonitor, BigInteger target) {
		BigInteger bi = BigInteger.valueOf(0);
		while(target.compareTo(bi) > 0) {
			bi = bi.add(BigInteger.ONE);
			opMonitor.setProgress(bi.doubleValue() / target.doubleValue());
		}
		return bi;
	}
	
}