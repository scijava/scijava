package org.scijava.ops.engine.util;

import org.scijava.ops.spi.OpExecutionException;

public class ExceptionUtils {
	// For void methods
	public static <T> void execute(ThrowingRunnable runnable) {
		try {
			runnable.run();
		} catch (Exception exc) {
			throw new OpExecutionException(exc);
		}
	}

	public interface ThrowingRunnable {
		void run() throws Exception;
	}

	// For non-void methods
	public static <T> T execute(ThrowingSupplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Exception exc) {
			throw new OpExecutionException(exc);
		}
	}

	public interface ThrowingSupplier<T> {
		T get() throws Exception;
	}
}
