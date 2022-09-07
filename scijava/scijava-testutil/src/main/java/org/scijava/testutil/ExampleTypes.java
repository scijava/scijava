package org.scijava.testutil;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Example Types useful for general testing
 *
 * @author Gabriel Selzer
 */
public class ExampleTypes {

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

	public interface NestedThing<I1, I2> extends Function<I1, I2>
	{
		// NB: No implementation needed.
	}

	/** Enumeration for testing conversion to enum types. */
	public static enum Words {
		FOO, BAR, FUBAR
	}


}
