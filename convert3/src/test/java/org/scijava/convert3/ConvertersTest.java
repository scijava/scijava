/*
 * #%L
 * Converting a value to the type something else wants.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.convert3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.scijava.common3.Types;

/**
 * Tests conversion.
 *
 * @author Curtis Rueden
 */
public class ConvertersTest {

	private final Converters converters = Converters.get();

	/** The converters are found without a container of any kind. */
	@Test
	public void testDiscovery() {
		assertFalse(converters.converters().isEmpty(),
			"converters should come from ServiceLoader");
	}

	/** A value already of the wanted type is returned, not rebuilt. */
	@Test
	public void testNoConversionNeeded() {
		final List<String> list = List.of("a");
		assertSame(list, converters.convert(list, List.class));
		assertEquals("x", converters.convert("x", String.class));
	}

	/** A boxed value satisfies a primitive parameter, and the reverse. */
	@Test
	public void testPrimitives() {
		assertEquals(5, converters.convert(5, int.class));
		assertEquals(5.0, converters.convert(5, double.class));
		assertEquals(5L, converters.convert(5.9, long.class), "narrows as a cast");
		assertEquals(true, converters.convert(true, boolean.class));
	}

	/** Text becomes whatever kind of value was asked for. */
	@Test
	public void testFromString() {
		assertEquals(42, converters.convert("42", int.class));
		assertEquals(4.5, converters.convert("4.5", double.class));
		assertEquals('a', converters.convert("abc", char.class));
		assertEquals(true, converters.convert("TRUE", boolean.class));
		assertEquals(Season.WINTER, converters.convert("WINTER", Season.class));
		assertEquals(new BigDecimal("0.1"), converters.convert("0.1",
			BigDecimal.class));
	}

	/** A BigDecimal from a double keeps what the text said, not the binary. */
	@Test
	public void testExactness() {
		assertEquals(new BigDecimal("0.1"), converters.convert(0.1,
			BigDecimal.class));
	}

	/** Text that is not a value of that type converts to nothing. */
	@Test
	public void testUnparseableText() {
		assertEquals(Optional.empty(), converters.tryConvert("-", int.class));
		assertEquals(Optional.empty(), converters.tryConvert("", int.class));
		assertEquals(Optional.empty(), converters.tryConvert("SPRINGISH",
			Season.class));
		assertThrows(ConversionException.class, () -> converters.convert("nope",
			int.class));
	}

	/** Anything can be described as text. */
	@Test
	public void testToString() {
		assertEquals("42", converters.convert(42, String.class));
		assertEquals("WINTER", converters.convert(Season.WINTER, String.class));
	}

	/** A lone value becomes an array of one, which is what scripts pass. */
	@Test
	public void testArrays() {
		assertArrayEquals(new int[] { 3 }, (int[]) converters.convert("3",
			int[].class));
		assertArrayEquals(new double[] { 1, 2, 3 }, (double[]) converters.convert(
			List.of("1", "2", "3"), double[].class), 0);
		assertArrayEquals(new String[] { "1", "2" }, (String[]) converters.convert(
			new int[] { 1, 2 }, String[].class));
	}

	/** A collection's element type is honored, not merely its raw type. */
	@Test
	public void testCollections() {
		// NB: the element type comes from the destination type, so this is
		// List<Integer> rather than a raw List.
		final List<?> list = (List<?>) converters.convert(new String[] { "1", "2" },
			Types.parameterize(List.class, Integer.class));
		assertEquals(List.of(1, 2), list);

		final Set<?> set = (Set<?>) converters.convert(List.of("1", "1", "2"),
			Types.parameterize(Set.class, Integer.class));
		assertEquals(Set.of(1, 2), set);
	}

	/**
	 * A collection of the wrong element type is converted, not waved through.
	 * <p>
	 * NB: erasure makes {@code List<String>} and {@code List<File>} the same
	 * raw type, so a cast would return the right kind of container full of the
	 * wrong things - which is the sort of bug that surfaces much later, as a
	 * ClassCastException somewhere else entirely.
	 * </p>
	 */
	@Test
	public void testCollectionElementsAreConverted() {
		final List<?> converted = (List<?>) converters.convert(List.of("1", "2"),
			Types.parameterize(List.class, Integer.class));

		assertEquals(List.of(1, 2), converted);

		// and a list that is already right comes back equal
		assertEquals(List.of(1, 2), converters.convert(List.of(1, 2), Types
			.parameterize(List.class, Integer.class)));
	}

	/** The three ways of naming a file all convert to one another. */
	@Test
	public void testFilesAndPaths() {
		final File file = new File("/tmp/x.txt");
		assertEquals(file.toPath(), converters.convert(file, Path.class));
		assertEquals(file, converters.convert(file.toPath(), File.class));
		assertEquals(file, converters.convert("/tmp/x.txt", File.class));
		assertEquals(file.toPath(), converters.convert("/tmp/x.txt", Path.class));
	}

	/** A constructor taking the value is the last resort, and it works. */
	@Test
	public void testWrappingConstructor() {
		final Object wrapped = converters.convert("hello", Wrapper.class);
		assertEquals("hello", ((Wrapper) wrapped).value);
	}

	/** Null is null, except where a primitive cannot hold one. */
	@Test
	public void testNull() {
		assertEquals(Optional.empty(), converters.tryConvert(null, String.class));
		assertEquals(0, converters.convert(null, int.class));
		assertEquals(false, converters.convert(null, boolean.class));
	}

	/** What cannot be converted says so, rather than returning null. */
	@Test
	public void testUnsupported() {
		assertFalse(converters.supports(new Object(), Season.class));
		assertThrows(ConversionException.class, () -> converters.convert(
			new Object(), Season.class));
	}

	/** An explicit list of converters is the form for a test or an embedder. */
	@Test
	public void testExplicitConverters() {
		final Converters only = new Converters(List.of( //
			Converter.of(String.class, Season.class, s -> Season.SUMMER)));
		assertEquals(Season.SUMMER, only.convert("anything", Season.class));
		assertTrue(only.tryConvert(42, String.class).isEmpty(),
			"nothing else should be available");
	}

	// -- Test fixtures --

	public enum Season {
			WINTER, SPRING, SUMMER, AUTUMN
	}

	public static class Wrapper {

		public final String value;

		public Wrapper(final String value) {
			this.value = value;
		}
	}
}
