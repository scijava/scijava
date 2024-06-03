/*
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;

/**
 * Useful methods for working with {@link Class} objects and primitive types.
 *
 * @author Curtis Rueden
 */
public final class Classes {

	private Classes() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the class loader to use. This will be the current thread's context
	 * class loader if non-null; otherwise it will be the system class loader.
	 *
	 * @see Thread#getContextClassLoader()
	 * @see ClassLoader#getSystemClassLoader()
	 */
	public static ClassLoader classLoader() {
		final ClassLoader contextCL = Thread.currentThread()
			.getContextClassLoader();
		return contextCL != null ? contextCL : ClassLoader.getSystemClassLoader();
	}

	/**
	 * Loads the class with the given name, using the current thread's context
	 * class loader, or null if it cannot be loaded.
	 *
	 * @param name The name of the class to load.
	 * @return The loaded class, or null if the class could not be loaded.
	 * @see #load(String, ClassLoader, boolean)
	 */
	public static Class<?> load(final String name) {
		return load(name, null, true);
	}

	/**
	 * Loads the class with the given name, using the specified
	 * {@link ClassLoader}, or null if it cannot be loaded.
	 *
	 * @param name The name of the class to load.
	 * @param classLoader The class loader with which to load the class; if null,
	 *          the current thread's context class loader will be used.
	 * @return The loaded class, or null if the class could not be loaded.
	 * @see #load(String, ClassLoader, boolean)
	 */
	public static Class<?> load(final String name,
		final ClassLoader classLoader)
	{
		return load(name, classLoader, true);
	}

	/**
	 * Loads the class with the given name, using the current thread's context
	 * class loader.
	 *
	 * @param className the name of the class to load.
	 * @param quietly Whether to return {@code null} (rather than throwing
	 *          {@link IllegalArgumentException}) if something goes wrong loading
	 *          the class.
	 * @return The loaded class, or {@code null} if the class could not be loaded
	 *         and the {@code quietly} flag is set.
	 * @see #load(String, ClassLoader, boolean)
	 * @throws IllegalArgumentException If the class cannot be loaded and the
	 *           {@code quietly} flag is not set.
	 */
	public static Class<?> load(final String className, final boolean quietly) {
		return load(className, null, quietly);
	}

	/**
	 * Loads the class with the given name, using the specified
	 * {@link ClassLoader}, or null if it cannot be loaded.
	 * <p>
	 * This method is capable of parsing several different class name syntaxes. In
	 * particular, array classes (including primitives) represented using either
	 * square brackets or internal Java array name syntax are supported. Examples:
	 * </p>
	 * <ul>
	 * <li>{@code boolean} is loaded as {@code boolean.class}</li>
	 * <li>{@code Z} is loaded as {@code boolean.class}</li>
	 * <li>{@code double[]} is loaded as {@code double[].class}</li>
	 * <li>{@code string[]} is loaded as {@code java.lang.String.class}</li>
	 * <li>{@code [F} is loaded as {@code float[].class}</li>
	 * </ul>
	 *
	 * @param name The name of the class to load.
	 * @param classLoader The class loader with which to load the class; if null,
	 *          the current thread's context class loader will be used.
	 * @param quietly Whether to return {@code null} (rather than throwing
	 *          {@link IllegalArgumentException}) if something goes wrong loading
	 *          the class
	 * @return The loaded class, or {@code null} if the class could not be loaded
	 *         and the {@code quietly} flag is set.
	 * @throws IllegalArgumentException If the class cannot be loaded and the
	 *           {@code quietly} flag is not set.
	 */
	public static Class<?> load(final String name, final ClassLoader classLoader,
		final boolean quietly)
	{
		// handle primitive types
		if (name.equals("Z") || name.equals("boolean")) return boolean.class;
		if (name.equals("B") || name.equals("byte")) return byte.class;
		if (name.equals("C") || name.equals("char")) return char.class;
		if (name.equals("D") || name.equals("double")) return double.class;
		if (name.equals("F") || name.equals("float")) return float.class;
		if (name.equals("I") || name.equals("int")) return int.class;
		if (name.equals("J") || name.equals("long")) return long.class;
		if (name.equals("S") || name.equals("short")) return short.class;
		if (name.equals("V") || name.equals("void")) return void.class;

		// handle built-in class shortcuts
		final String className;
		if (name.equals("string")) className = "java.lang.String";
		else className = name;

		// handle source style arrays (e.g.: "java.lang.String[]")
		if (name.endsWith("[]")) {
			final String elementClassName = name.substring(0, name.length() - 2);
			return arrayOrNull(load(elementClassName, classLoader));
		}

		// handle non-primitive internal arrays (e.g.: "[Ljava.lang.String;")
		if (name.startsWith("[L") && name.endsWith(";")) {
			final String elementClassName = name.substring(2, name.length() - 1);
			return arrayOrNull(load(elementClassName, classLoader));
		}

		// handle other internal arrays (e.g.: "[I", "[[I", "[[Ljava.lang.String;")
		if (name.startsWith("[")) {
			final String elementClassName = name.substring(1);
			return arrayOrNull(load(elementClassName, classLoader));
		}

		// load the class!
		try {
			final ClassLoader cl = classLoader == null ? classLoader() : classLoader;
			return cl.loadClass(className);
		}
		catch (final Throwable t) {
			// NB: Do not allow any failure to load the class to crash us.
			// Not ClassNotFoundException.
			// Not NoClassDefFoundError.
			// Not UnsupportedClassVersionError!
			if (quietly) return null;
			throw iae(t, "Cannot load class: %s", className);
		}
	}

	/**
	 * Gets the base location of the given class.
	 *
	 * @param c The class whose location is desired.
	 * @return URL pointing to the class, or null if the location could not be
	 *         determined.
	 * @see #location(Class, boolean)
	 */
	public static URL location(final Class<?> c) {
		return location(c, true);
	}

	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "file:/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "file:/path/to/my-jar.jar").
	 * </p>
	 *
	 * @param c The class whose location is desired.
	 * @param quietly Whether to return {@code null} (rather than throwing
	 *          {@link IllegalArgumentException}) if something goes wrong
	 *          determining the location.
	 * @return URL pointing to the class, or null if the location could not be
	 *         determined and the {@code quietly} flag is set.
	 * @throws IllegalArgumentException If the location cannot be determined and
	 *           the {@code quietly} flag is not set.
	 */
	public static URL location(final Class<?> c, final boolean quietly) {
		Exception cause = null;
		String why = null;

		// try the easy way first
		try {
			final CodeSource codeSource = c.getProtectionDomain().getCodeSource();
			if (codeSource != null) {
				final URL location = codeSource.getLocation();
				if (location != null) return location;
				why = "null code source location";
			}
			else why = "null code source";
		}
		catch (final SecurityException exc) {
			// NB: Cannot access protection domain.
			cause = exc;
			why = "cannot access protection domain";
		}

		// NB: The easy way failed, so we try the hard way. We ask for the class
		// itself as a resource, then strip the class's path from the URL string,
		// leaving the base path.

		// get the class's raw resource path
		final URL classResource = c.getResource(c.getSimpleName() + ".class");
		if (classResource == null) {
			// cannot find class resource
			if (quietly) return null;
			throw iae(cause, "No class resource for class: %s (%s)", c.getName(), why);
		}

		final String url = classResource.toString();
		final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
		if (!url.endsWith(suffix)) {
			// weird URL
			if (quietly) return null;
			throw iae(cause, "Unsupported URL format: %s (%s)", url, why);
		}

		// strip the class's path from the URL string
		final String base = url.substring(0, url.length() - suffix.length());

		String path = base;

		// remove the "jar:" prefix and "!/" suffix, if present
		if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

		try {
			return new URL(path);
		}
		catch (final MalformedURLException e) {
			if (quietly) return null;
			throw iae(e, "Malformed URL: %s (%s)", path, why);
		}
	}

	// -- Primitives --

	public static boolean isBoolean(final Class<?> type) {
		return type == boolean.class || Boolean.class.isAssignableFrom(type);
	}

	public static boolean isByte(final Class<?> type) {
		return type == byte.class || Byte.class.isAssignableFrom(type);
	}

	public static boolean isCharacter(final Class<?> type) {
		return type == char.class || Character.class.isAssignableFrom(type);
	}

	public static boolean isDouble(final Class<?> type) {
		return type == double.class || Double.class.isAssignableFrom(type);
	}

	public static boolean isFloat(final Class<?> type) {
		return type == float.class || Float.class.isAssignableFrom(type);
	}

	public static boolean isInteger(final Class<?> type) {
		return type == int.class || Integer.class.isAssignableFrom(type);
	}

	public static boolean isLong(final Class<?> type) {
		return type == long.class || Long.class.isAssignableFrom(type);
	}

	public static boolean isShort(final Class<?> type) {
		return type == short.class || Short.class.isAssignableFrom(type);
	}

	public static boolean isNumber(final Class<?> type) {
		return Number.class.isAssignableFrom(type) || type == byte.class ||
			type == double.class || type == float.class || type == int.class ||
			type == long.class || type == short.class;
	}

	public static boolean isText(final Class<?> type) {
		return String.class.isAssignableFrom(type) || isCharacter(type);
	}

	/**
	 * Returns the non-primitive {@link Class} closest to the given type.
	 * <p>
	 * Specifically, the following type conversions are done:
	 * <ul>
	 * <li>boolean.class becomes Boolean.class</li>
	 * <li>byte.class becomes Byte.class</li>
	 * <li>char.class becomes Character.class</li>
	 * <li>double.class becomes Double.class</li>
	 * <li>float.class becomes Float.class</li>
	 * <li>int.class becomes Integer.class</li>
	 * <li>long.class becomes Long.class</li>
	 * <li>short.class becomes Short.class</li>
	 * <li>void.class becomes Void.class</li>
	 * </ul>
	 * All other types are unchanged.
	 */
	public static <T> Class<T> box(final Class<T> type) {
		final Class<?> destType;
		if (type == boolean.class) destType = Boolean.class;
		else if (type == byte.class) destType = Byte.class;
		else if (type == char.class) destType = Character.class;
		else if (type == double.class) destType = Double.class;
		else if (type == float.class) destType = Float.class;
		else if (type == int.class) destType = Integer.class;
		else if (type == long.class) destType = Long.class;
		else if (type == short.class) destType = Short.class;
		else if (type == void.class) destType = Void.class;
		else destType = type;
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) destType;
		return result;
	}

	/**
	 * Returns the primitive {@link Class} closest to the given type.
	 * <p>
	 * Specifically, the following type conversions are done:
	 * <ul>
	 * <li>Boolean.class becomes boolean.class</li>
	 * <li>Byte.class becomes byte.class</li>
	 * <li>Character.class becomes char.class</li>
	 * <li>Double.class becomes double.class</li>
	 * <li>Float.class becomes float.class</li>
	 * <li>Integer.class becomes int.class</li>
	 * <li>Long.class becomes long.class</li>
	 * <li>Short.class becomes short.class</li>
	 * <li>Void.class becomes void.class</li>
	 * </ul>
	 * All other types are unchanged.
	 */
	public static <T> Class<T> unbox(final Class<T> type) {
		final Class<?> destType;
		if (type == Boolean.class) destType = boolean.class;
		else if (type == Byte.class) destType = byte.class;
		else if (type == Character.class) destType = char.class;
		else if (type == Double.class) destType = double.class;
		else if (type == Float.class) destType = float.class;
		else if (type == Integer.class) destType = int.class;
		else if (type == Long.class) destType = long.class;
		else if (type == Short.class) destType = short.class;
		else if (type == Void.class) destType = void.class;
		else destType = type;
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) destType;
		return result;
	}

	/**
	 * Gets the "null" value for the given type. For non-primitives, this will
	 * actually be null. For primitives, it will be zero for numeric types, false
	 * for boolean, and the null character for char.
	 */
	public static <T> T nullValue(final Class<T> type) {
		final Object defaultValue;
		if (type == boolean.class) defaultValue = false;
		else if (type == byte.class) defaultValue = (byte) 0;
		else if (type == char.class) defaultValue = '\0';
		else if (type == double.class) defaultValue = 0d;
		else if (type == float.class) defaultValue = 0f;
		else if (type == int.class) defaultValue = 0;
		else if (type == long.class) defaultValue = 0L;
		else if (type == short.class) defaultValue = (short) 0;
		else defaultValue = null;
		@SuppressWarnings("unchecked")
		final T result = (T) defaultValue;
		return result;
	}

	/**
	 * Gets the field with the specified name, of the given class, or superclass
	 * thereof.
	 * <p>
	 * Unlike {@link Class#getField(String)}, this method will return fields of
	 * any visibility, not just {@code public}. And unlike
	 * {@link Class#getDeclaredField(String)}, it will do so recursively,
	 * returning the first field of the given name from the class's superclass
	 * hierarchy.
	 * </p>
	 * <p>
	 * Note that this method does not guarantee that the returned field is
	 * accessible; if the field is not {@code public}, calling code will need to
	 * use {@link Field#setAccessible(boolean)} in order to manipulate the field's
	 * contents.
	 * </p>
	 *
	 * @param c The class (or subclass thereof) containing the desired field.
	 * @param name
	 * @return The first field with the given name in the class's superclass
	 *         hierarchy.
	 * @throws IllegalArgumentException if the specified class does not contain a
	 *           field with the given name
	 */
	public static Field field(final Class<?> c, final String name) {
		if (c == null) throw iae(null, "No such field: %s", name);
		try {
			return c.getDeclaredField(name);
		}
		catch (final NoSuchFieldException e) {
			// Try the next class level up
			return field(c.getSuperclass(), name);
		}
	}

	/**
	 * Gets the method with the specified name and argument types, of the given
	 * class, or superclass thereof.
	 * <p>
	 * Unlike {@link Class#getMethod(String, Class[])}, this method will return
	 * methods of any visibility, not just {@code public}. And unlike
	 * {@link Class#getDeclaredMethod(String, Class[])}, it will do so
	 * recursively, returning the first method of the given name and argument
	 * types from the class's superclass hierarchy.
	 * </p>
	 * <p>
	 * Note that this method does not guarantee that the returned method is
	 * accessible; if the method is not {@code public}, calling code will need to
	 * use {@link Method#setAccessible(boolean)} in order to invoke the method.
	 * </p>
	 *
	 * @param c The class (or subclass thereof) containing the desired method.
	 * @param name Name of the method.
	 * @param parameterTypes Types of the method parameters.
	 * @return The first method with the given name and argument types in the
	 *         class's superclass hierarchy.
	 * @throws IllegalArgumentException If the specified class does not contain a
	 *           method with the given name and argument types.
	 */
	public static Method method(final Class<?> c, final String name,
		final Class<?>... parameterTypes)
	{
		if (c == null) throw iae(null, "No such field: %s", name);
		try {
			return c.getDeclaredMethod(name, parameterTypes);
		}
		catch (final NoSuchMethodException exc) {
			// NB: empty catch block
		}
		// Try the next class level up
		return method(c.getSuperclass(), name, parameterTypes);
	}

	/**
	 * Gets the array class corresponding to the given element type.
	 * <p>
	 * For example, {@code arrayType(double.class)} returns {@code double[].class}
	 * .
	 * </p>
	 *
	 * @param componentType The type of elements which the array possesses
	 * @throws IllegalArgumentException if the type cannot be the component type
	 *           of an array (this is the case e.g. for {@code void.class}).
	 */
	public static Class<?> array(final Class<?> componentType) {
		if (componentType == null) return null;
		// NB: It appears the reflection API has no built-in way to do this.
		// So unfortunately, we must allocate a new object and then inspect it.
		return Array.newInstance(componentType, 0).getClass();
	}

	/**
	 * Gets the array class corresponding to the given element type and
	 * dimensionality.
	 * <p>
	 * For example, {@code arrayType(double.class, 2)} returns
	 * {@code double[][].class}.
	 * </p>
	 *
	 * @param componentType The type of elements which the array possesses
	 * @param dim The dimensionality of the array
	 */
	public static Class<?> array(final Class<?> componentType, final int dim) {
		if (dim < 0) throw iae(null, "Negative dimension");
		if (dim == 0) return componentType;
		return array(array(componentType), dim - 1);
	}

	// -- Comparison --

	/**
	 * Compares two {@link Class} objects using their fully qualified names.
	 * <p>
	 * Note: this method provides a natural ordering that may be inconsistent with
	 * equals. Specifically, two unequal classes may return 0 when compared in
	 * this fashion if they represent the same class loaded using two different
	 * {@link ClassLoader}s. Hence, if this method is used as a basis for
	 * implementing {@link Comparable#compareTo} or
	 * {@link java.util.Comparator#compare}, that implementation may want to
	 * impose logic beyond that of this method, for breaking ties, if a total
	 * ordering consistent with equals is always required.
	 * </p>
	 */
	public static int compare(final Class<?> c1, final Class<?> c2) {
		if (c1 == c2) return 0;
		final String name1 = c1 == null ? null : c1.getName();
		final String name2 = c2 == null ? null : c2.getName();
		return Comparisons.compare(name1, name2);
	}

	// -- Helper methods --

	private static Class<?> arrayOrNull(final Class<?> componentType) {
		try {
			return Classes.array(componentType);
		}
		catch (final IllegalArgumentException exc) {
			return null;
		}
	}

	/**
	 * Creates a new {@link IllegalArgumentException} with the given cause and
	 * formatted message string.
	 */
	private static IllegalArgumentException iae(final Throwable cause,
		final String formattedMessage, final String... values)
	{
		final String s = String.format(formattedMessage, (Object[]) values);
		final IllegalArgumentException exc = new IllegalArgumentException(s);
		if (cause != null) exc.initCause(cause);
		return exc;
	}
}
