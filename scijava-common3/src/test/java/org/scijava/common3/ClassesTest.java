/*-
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.common3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.testutil.ExampleTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Classes}
 *
 * @author Curtis Rueden
 * @author Mark Hiner
 * @author Johannes Schindelin
 * @author Gabriel Selzer
 */
public class ClassesTest {

	/**
	 * Tests {@link Classes#load}.
	 */
	@Test
	public void testLoad() {
		assertLoaded(boolean.class, "boolean");
		assertLoaded(byte.class, "byte");
		assertLoaded(char.class, "char");
		assertLoaded(double.class, "double");
		assertLoaded(float.class, "float");
		assertLoaded(int.class, "int");
		assertLoaded(long.class, "long");
		assertLoaded(short.class, "short");
		assertLoaded(void.class, "void");
		assertLoaded(String.class, "string");
		assertLoaded(Number.class, "java.lang.Number");
		assertLoaded(boolean[].class, "boolean[]");
		assertLoaded(byte[].class, "byte[]");
		assertLoaded(char[].class, "char[]");
		assertLoaded(double[].class, "double[]");
		assertLoaded(float[].class, "float[]");
		assertLoaded(int[].class, "int[]");
		assertLoaded(long[].class, "long[]");
		assertLoaded(short[].class, "short[]");
		assertLoaded(null, "void[]");
		assertLoaded(String[].class, "string[]");
		assertLoaded(Number[].class, "java.lang.Number[]");
		assertLoaded(boolean[][].class, "boolean[][]");
		assertLoaded(byte[][].class, "byte[][]");
		assertLoaded(char[][].class, "char[][]");
		assertLoaded(double[][].class, "double[][]");
		assertLoaded(float[][].class, "float[][]");
		assertLoaded(int[][].class, "int[][]");
		assertLoaded(long[][].class, "long[][]");
		assertLoaded(short[][].class, "short[][]");
		assertLoaded(null, "void[][]");
		assertLoaded(String[][].class, "string[][]");
		assertLoaded(Number[][].class, "java.lang.Number[][]");
		assertLoaded(boolean[].class, "[Z");
		assertLoaded(byte[].class, "[B");
		assertLoaded(char[].class, "[C");
		assertLoaded(double[].class, "[D");
		assertLoaded(float[].class, "[F");
		assertLoaded(int[].class, "[I");
		assertLoaded(long[].class, "[J");
		assertLoaded(short[].class, "[S");
		assertLoaded(null, "[V");
		assertLoaded(String[].class, "[Lstring;");
		assertLoaded(Number[].class, "[Ljava.lang.Number;");
		assertLoaded(boolean[][].class, "[[Z");
		assertLoaded(byte[][].class, "[[B");
		assertLoaded(char[][].class, "[[C");
		assertLoaded(double[][].class, "[[D");
		assertLoaded(float[][].class, "[[F");
		assertLoaded(int[][].class, "[[I");
		assertLoaded(long[][].class, "[[J");
		assertLoaded(short[][].class, "[[S");
		assertLoaded(null, "[[V");
		assertLoaded(String[][].class, "[[Lstring;");
		assertLoaded(Number[][].class, "[[Ljava.lang.Number;");
	}

	/**
	 * Tests {@link Classes#load}.
	 */
	@Test
	public void testLoadFailureQuiet() {
		// test quiet failure
		assertNull(Classes.load("a.non.existent.class"));
	}

	/**
	 * Tests {@link Classes#load}.
	 */
	@Test
	public void testLoadFailureLoud() {
		assertThrows(IllegalArgumentException.class, //
			() -> Classes.load("a.non.existent.class", false));
	}

	/** Tests {@link Classes#location} with a class on the file system. */
	@Test
	public void testLocationUnpackedClass() throws IOException,
		URISyntaxException
	{
		final File tmpDir = Files.createTempDirectory("class-utils-test").toFile();
		final String path = getClass().getName().replace('.', '/') + ".class";
		final File classFile = new File(tmpDir, path);
		assertTrue(classFile.getParentFile().exists() || classFile.getParentFile()
			.mkdirs());
		URL url = Objects.requireNonNull(getClass().getResource("/" + path));
		copy(url.openStream(), new FileOutputStream(classFile));

		final ClassLoader classLoader = new URLClassLoader(new URL[] { tmpDir
			.toURI().toURL() }, null);
		final Class<?> c = Classes.load(getClass().getName(), classLoader);
		final URL location = Classes.location(c);
		File actual = new File(location.toURI());
		actual.deleteOnExit();

		Assertions.assertEquals(tmpDir, new File(location.toURI()));
	}

	/** Tests {@link Classes#location} with a class in a JAR file. */
	@Test
	public void testLocationClassInJar() throws IOException, URISyntaxException {
		final File tmpDir = Files.createTempDirectory("class-utils-test").toFile();
		// final File tmpDir = createTemporaryDirectory("class-utils-test-");
		final File jar = new File(tmpDir, "test.jar");
		final JarOutputStream out = new JarOutputStream(new FileOutputStream(jar));
		final String path = getClass().getName().replace('.', '/') + ".class";
		out.putNextEntry(new ZipEntry(path));
		URL url = Objects.requireNonNull(getClass().getResource("/" + path));
		copy(url.openStream(), out);

		final ClassLoader classLoader = new URLClassLoader(new URL[] { jar.toURI()
			.toURL() }, null);
		final Class<?> c = Classes.load(getClass().getName(), classLoader);
		final URL location = Classes.location(c);
		assertEquals(jar, new File(location.toURI()));
		jar.deleteOnExit();
	}

	/** Tests quiet behavior of {@link Classes#location(Class, boolean)}. */
	@Test
	public void testLocationFailureQuiet() {
		final Class<?> weirdClass = loadCustomClass();
		assertEquals("Hello", weirdClass.getName());
		assertNull(Classes.location(weirdClass));
	}

	/** Tests exceptions from {@link Classes#location(Class, boolean)}. */
	@Test
	public void testLocationFailureLoud() {
		final Class<?> weirdClass = loadCustomClass();
		assertEquals("Hello", weirdClass.getName());
		assertThrows(IllegalArgumentException.class, //
			() -> Classes.location(weirdClass, false));
	}

	/** Tests {@link Classes#unbox(Class)}. */
	@Test
	public void testUnbox() {
		final Class<?> booleanType = Classes.unbox(Boolean.class);
		assertSame(boolean.class, booleanType);

		final Class<?> byteType = Classes.unbox(Byte.class);
		assertSame(byte.class, byteType);

		final Class<?> charType = Classes.unbox(Character.class);
		assertSame(char.class, charType);

		final Class<?> doubleType = Classes.unbox(Double.class);
		assertSame(double.class, doubleType);

		final Class<?> floatType = Classes.unbox(Float.class);
		assertSame(float.class, floatType);

		final Class<?> intType = Classes.unbox(Integer.class);
		assertSame(int.class, intType);

		final Class<?> longType = Classes.unbox(Long.class);
		assertSame(long.class, longType);

		final Class<?> shortType = Classes.unbox(Short.class);
		assertSame(short.class, shortType);

		final Class<?> voidType = Classes.unbox(Void.class);
		assertSame(void.class, voidType);

		final Class<?>[] types = { //
			boolean.class, byte.class, char.class, double.class, //
			float.class, int.class, long.class, short.class, //
			void.class, //
			String.class, //
			Number.class, BigInteger.class, BigDecimal.class, //
			boolean[].class, byte[].class, char[].class, double[].class, //
			float[].class, int[].class, long[].class, short[].class, //
			Boolean[].class, Byte[].class, Character[].class, Double[].class, //
			Float[].class, Integer[].class, Long[].class, Short[].class, //
			Void[].class, //
			Object.class, Object[].class, String[].class, //
			Object[][].class, String[][].class, //
			Collection.class, //
			List.class, ArrayList.class, LinkedList.class, //
			Set.class, HashSet.class, //
			Map.class, HashMap.class, //
			Collection[].class, List[].class, Set[].class, Map[].class };
		for (final Class<?> c : types) {
			final Class<?> type = Classes.unbox(c);
			assertSame(c, type);
		}
	}

	/** Tests {@link Classes#box(Class)}. */
	@Test
	public void testBox() {
		final Class<Boolean> booleanType = Classes.box(boolean.class);
		assertSame(Boolean.class, booleanType);

		final Class<Byte> byteType = Classes.box(byte.class);
		assertSame(Byte.class, byteType);

		final Class<Character> charType = Classes.box(char.class);
		assertSame(Character.class, charType);

		final Class<Double> doubleType = Classes.box(double.class);
		assertSame(Double.class, doubleType);

		final Class<Float> floatType = Classes.box(float.class);
		assertSame(Float.class, floatType);

		final Class<Integer> intType = Classes.box(int.class);
		assertSame(Integer.class, intType);

		final Class<Long> longType = Classes.box(long.class);
		assertSame(Long.class, longType);

		final Class<Short> shortType = Classes.box(short.class);
		assertSame(Short.class, shortType);

		final Class<Void> voidType = Classes.box(void.class);
		assertSame(Void.class, voidType);

		final Class<?>[] types = { //
			Boolean.class, Byte.class, Character.class, Double.class, //
			Float.class, Integer.class, Long.class, Short.class, //
			Void.class, //
			String.class, //
			Number.class, BigInteger.class, BigDecimal.class, //
			boolean[].class, byte[].class, char[].class, double[].class, //
			float[].class, int[].class, long[].class, short[].class, //
			Boolean[].class, Byte[].class, Character[].class, Double[].class, //
			Float[].class, Integer[].class, Long[].class, Short[].class, //
			Void[].class, //
			Object.class, Object[].class, String[].class, //
			Object[][].class, String[][].class, //
			Collection.class, //
			List.class, ArrayList.class, LinkedList.class, //
			Set.class, HashSet.class, //
			Map.class, HashMap.class, //
			Collection[].class, List[].class, Set[].class, Map[].class };
		for (final Class<?> c : types) {
			final Class<?> type = Classes.box(c);
			assertSame(c, type);
		}
	}

	/** Tests {@link Classes#nullValue(Class)}. */
	@Test
	public void testNullValue() {
		final boolean booleanNull = Classes.nullValue(boolean.class);
		assertFalse(booleanNull);

		final byte byteNull = Classes.nullValue(byte.class);
		assertEquals(0, byteNull);

		final char charNull = Classes.nullValue(char.class);
		assertEquals('\0', charNull);

		final double doubleNull = Classes.nullValue(double.class);
		assertEquals(0.0, doubleNull, 0.0);

		final float floatNull = Classes.nullValue(float.class);
		assertEquals(0f, floatNull, 0f);

		final int intNull = Classes.nullValue(int.class);
		assertEquals(0, intNull);

		final long longNull = Classes.nullValue(long.class);
		assertEquals(0, longNull);

		final short shortNull = Classes.nullValue(short.class);
		assertEquals(0, shortNull);

		final Void voidNull = Classes.nullValue(void.class);
		assertNull(voidNull);

		final Class<?>[] types = { //
			Boolean.class, Byte.class, Character.class, Double.class, //
			Float.class, Integer.class, Long.class, Short.class, //
			Void.class, //
			String.class, //
			Number.class, BigInteger.class, BigDecimal.class, //
			boolean[].class, byte[].class, char[].class, double[].class, //
			float[].class, int[].class, long[].class, short[].class, //
			Boolean[].class, Byte[].class, Character[].class, Double[].class, //
			Float[].class, Integer[].class, Long[].class, Short[].class, //
			Void[].class, //
			Object.class, Object[].class, String[].class, //
			Object[][].class, String[][].class, //
			Collection.class, //
			List.class, ArrayList.class, LinkedList.class, //
			Set.class, HashSet.class, //
			Map.class, HashMap.class, //
			Collection[].class, List[].class, Set[].class, Map[].class };
		for (final Class<?> c : types) {
			final Object nullValue = Classes.nullValue(c);
			assertNull(nullValue, "Expected null for " + c.getName());
		}
	}

	/** Tests {@link Classes#field}. */
	@Test
	public void testField() {
		final Field field = Classes.field(ExampleTypes.Thing.class, "thing");
		assertEquals("thing", field.getName());
		assertSame(Object.class, field.getType());
		assertTrue(field.getGenericType() instanceof TypeVariable);
		assertEquals("T", ((TypeVariable<?>) field.getGenericType()).getName());
	}

	/** Tests {@link Classes#method}. */
	@Test
	public void testMethod() {
		final Method objectMethod = Classes.method(ExampleTypes.Thing.class,
			"toString");
		assertSame(Object.class, objectMethod.getDeclaringClass());
		assertEquals("toString", objectMethod.getName());
		assertSame(String.class, objectMethod.getReturnType());
		assertEquals(0, objectMethod.getParameterTypes().length);

		final Method wordsMethod = //
			Classes.method(ExampleTypes.Words.class, "valueOf", String.class);
		// NB: What is going on under the hood to make the Enum
		// subtype Words be the declaring class for the 'valueOf'
		// method? The compiler must internally override the valueOf
		// method for each enum type, to narrow the return type...
		assertSame(ExampleTypes.Words.class, wordsMethod.getDeclaringClass());
		assertEquals("valueOf", wordsMethod.getName());
		assertSame(ExampleTypes.Words.class, wordsMethod.getReturnType());
		assertEquals(1, wordsMethod.getParameterTypes().length);
		assertSame(String.class, wordsMethod.getParameterTypes()[0]);
	}

	/** Tests {@link Classes#array}. */
	@Test
	public void testArray() {
		// 1-dimensional cases
		assertSame(boolean[].class, Classes.array(boolean.class));
		assertSame(String[].class, Classes.array(String.class));
		assertSame(Number[].class, Classes.array(Number.class));
		assertSame(boolean[][].class, Classes.array(boolean[].class));
		assertSame(String[][].class, Classes.array(String[].class));
		assertSame(Number[][].class, Classes.array(Number[].class));
		assertThrows(IllegalArgumentException.class, () -> Classes.array(
			void.class), "Unexpected success creating void[]");

		// multidimensional cases
		assertSame(Number[][].class, Classes.array(Number.class, 2));
		assertSame(boolean[][][].class, Classes.array(boolean.class, 3));
		assertSame(String.class, Classes.array(String.class, 0));
		assertThrows(IllegalArgumentException.class, () -> Classes.array(char.class,
			-1), "Unexpected success creating negative dimensional array");
	}

	// -- Helper methods -- //

	private void assertLoaded(final Class<?> c, final String name) {
		assertSame(c, Classes.load(name));
	}

	/**
	 * Copies bytes from an {@link InputStream} to an {@link OutputStream}.
	 *
	 * @param in the source
	 * @param out the sink
	 */
	private void copy(final InputStream in, final OutputStream out)
		throws IOException
	{
		final byte[] buffer = new byte[16384];
		for (;;) {
			final int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
	}

	private Class<?> loadCustomClass() {
		// NB: The bytecode below was compiled from the following source:
		//
		// public class Hello {}
		//
		final byte[] bytecode = { -54, -2, -70, -66, 0, 0, 0, 52, 0, 13, 10, 0, 3,
			0, 10, 7, 0, 11, 7, 0, 12, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3,
			40, 41, 86, 1, 0, 4, 67, 111, 100, 101, 1, 0, 15, 76, 105, 110, 101, 78,
			117, 109, 98, 101, 114, 84, 97, 98, 108, 101, 1, 0, 10, 83, 111, 117, 114,
			99, 101, 70, 105, 108, 101, 1, 0, 10, 72, 101, 108, 108, 111, 46, 106, 97,
			118, 97, 12, 0, 4, 0, 5, 1, 0, 5, 72, 101, 108, 108, 111, 1, 0, 16, 106,
			97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 0, 33,
			0, 2, 0, 3, 0, 0, 0, 0, 0, 1, 0, 1, 0, 4, 0, 5, 0, 1, 0, 6, 0, 0, 0, 29,
			0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 1, -79, 0, 0, 0, 1, 0, 7, 0, 0, 0, 6,
			0, 1, 0, 0, 0, 1, 0, 1, 0, 8, 0, 0, 0, 2, 0, 9 };

		class BytesClassLoader extends ClassLoader {

			public Class<?> load(final String name, final byte[] b) {
				return defineClass(name, b, 0, b.length);
			}
		}
		return new BytesClassLoader().load("Hello", bytecode);
	}

}
