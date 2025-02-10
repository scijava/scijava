/*-
 * #%L
 * Shared test utilities for SciJava projects.
 * %%
 * Copyright (C) 2020 - 2025 SciJava developers.
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

package org.scijava.testutil;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Example Types useful for general testing
 *
 * @author Gabriel Selzer
 */
public final class ExampleTypes {

	private ExampleTypes() {
		// Prevent instantiation of static utility class
	}

	public static class Thing<T> {

		@SuppressWarnings("unused")
		private T thing;
	}

	public static class NumberThing<N extends Number> extends Thing<N> {
		// NB: No implementation needed.
	}

	public static class IntegerThing extends NumberThing<Integer> {
		// NB: No implementation needed.
	}

	public static class ComplexThing<T extends Serializable & Cloneable> extends
		Thing<T>
	{
		// NB: No implementation needed.
	}

	public static class StrangeThing<S extends Number> extends Thing<Integer> {
		// NB: No implementation needed.
	}

	public static class StrangerThing<R extends String> extends
		StrangeThing<Double>
	{
		// NB: No implementation needed.
	}

	public static class RecursiveThing<T extends RecursiveThing<T>> extends
		Thing<Integer>
	{
		// NB: No implementation needed.
	}

	public static interface Loop {
		// NB: No implementation needed.
	}

	public static class CircularThing extends RecursiveThing<CircularThing>
		implements Loop
	{
		// NB: No implementation needed.
	}

	public static class LoopingThing extends RecursiveThing<LoopingThing>
		implements Loop
	{
		// NB: No implementation needed.
	}

	public interface NestedThing<I1, I2> extends Function<I1, I2> {
		// NB: No implementation needed.
	}

	/** Enumeration for testing conversion to enum types. */
	public static enum Words {
			FOO, BAR, FUBAR
	}

}
