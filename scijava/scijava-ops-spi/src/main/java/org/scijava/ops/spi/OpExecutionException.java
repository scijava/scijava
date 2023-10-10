/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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

package org.scijava.ops.spi;

/**
 * Thrown to indicate that an Op failed in its execution
 * 
 * @author Gabriel Selzer
 */
public class OpExecutionException extends RuntimeException {

	/**
	 * Constructs an {@code OpExecutionException} with the specified reason for
	 * failure.
	 * 
	 * @param s the reason for the failure
	 */
	public OpExecutionException(String s) {
		super(s);
	}

	/**
	 * Constructs an {@code OpExecutionException} with the specified cause.
	 * 
	 * @param cause the cause of the failure
	 */
	public OpExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an {@code OpExecutionException} with the specified reason for
	 * failure and cause.
	 * 
	 * @param message the reason for the failure
	 * @param cause the cause of the failure
	 */
	public OpExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Runs a {@link Runnable} that throws some checked {@link Exception},
	 * wrapping the {@link Exception} into an {@link OpExecutionException} if it
	 * is thrown.
	 * 
	 * @param runnable the {@link Runnable}
	 */
	public static void wrapAndRun(ThrowingRunnable runnable) {
		try {
			runnable.run();
		}
		catch (Exception exc) {
			throw new OpExecutionException(exc);
		}
	}

	public interface ThrowingRunnable {

		void run() throws Exception;
	}

	/**
	 * Runs a {@link java.util.function.Supplier} that throws some checked
	 * {@link Exception}, wrapping the {@link Exception} into an
	 * {@link OpExecutionException} if it is thrown.
	 *
	 * @param supplier the {@link java.util.function.Supplier}
	 */
	public static <T> T wrapAndRun(ThrowingSupplier<T> supplier) {
		try {
			return supplier.get();
		}
		catch (Exception exc) {
			throw new OpExecutionException(exc);
		}
	}

	public interface ThrowingSupplier<T> {

		T get() throws Exception;
	}

}
