/*-
 * #%L
 * Jython code intelligence for SciJava, via Jython's own parser.
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
package org.scijava.code.lsp.jython;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.scijava.code.lsp.jvm.ClassIndex;

/**
 * Writes Python type stubs ({@code .pyi}) of Java packages, so that a Python
 * language server (e.g. jedi-language-server) knows the Java classes a Jython
 * script uses.
 * <p>
 * For example, for {@code from ij import IJ}, the server finds
 * {@code ij/__init__.pyi}, with {@code IJ}'s methods, their parameters and
 * return types.
 * </p>
 * <p>
 * Stubs are written on demand, per package, from the classes themselves (as
 * the given class loader loads them), in the background; and so are the
 * stubs of the packages their signatures refer to, a few steps on, within a
 * limit. Java types become Python ones where Jython converts them (e.g.
 * {@code String} becomes {@code str}); overloads become
 * {@code typing.overload}s; getters become properties too, as Jython offers
 * them.
 * </p>
 * <p>
 * TODO: Consider scyjava's stubs instead (see
 * <a href="https://github.com/scijava/scyjava/pull/86">scyjava.types</a>).
 * </p>
 *
 * @author Gabriel Selzer
 */
class JavaStubs {

	/** How many packages to write stubs for, at most, per session. */
	private static final int MAX_PACKAGES = 400;

	/** How many steps to follow the packages signatures refer to. */
	private static final int DEPTH = 2;

	/** Names Python reserves (and so cannot be members' names in stubs). */
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		"False", "None", "True", "and", "as", "assert", "async", "await", "break",
		"class", "continue", "def", "del", "elif", "else", "except", "finally",
		"for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal",
		"not", "or", "pass", "raise", "return", "try", "while", "with", "yield"));

	private final File dir;
	private final ClassLoader loader;
	private final Function<String, List<String>> classNames;

	/** The packages done (or being done). */
	private final Set<String> done = ConcurrentHashMap.newKeySet();

	private final ExecutorService writer = Executors.newSingleThreadExecutor(
		r -> {
			final Thread t = new Thread(r, "Jython-stubs");
			t.setDaemon(true);
			return t;
		});

	/**
	 * @param dir Where to write the stubs.
	 * @param loader Loads the classes.
	 */
	public JavaStubs(final File dir, final ClassLoader loader) {
		this(dir, loader, JavaStubs::indexedClassNames);
	}

	/**
	 * @param dir Where to write the stubs.
	 * @param loader Loads the classes.
	 * @param classNames Lists the (fully qualified) names of the classes in a
	 *          package.
	 */
	JavaStubs(final File dir, final ClassLoader loader,
		final Function<String, List<String>> classNames)
	{
		this.dir = dir;
		this.loader = loader;
		this.classNames = classNames;
	}

	/** Where the stubs are written: a Python search path. */
	public File dir() {
		return dir;
	}

	/**
	 * Writes the stubs of the given packages, if not done yet; then, of the
	 * packages they refer to. Returns at once.
	 *
	 * @return Done once the given packages' stubs are written.
	 */
	public CompletableFuture<Void> ensure(final Collection<String> packages) {
		final List<String> todo = packages.stream().filter(p -> !done.contains(p))
			.collect(Collectors.toList());
		if (todo.isEmpty()) return CompletableFuture.completedFuture(null);
		final CompletableFuture<Void> written = new CompletableFuture<>();
		writer.execute(() -> {
			final Deque<String> queue = new ArrayDeque<>(todo);
			final Map<String, Integer> depth = new LinkedHashMap<>();
			for (final String p : todo) depth.put(p, 0);
			int first = todo.size();
			while (!queue.isEmpty()) {
				final String pkg = queue.poll();
				if (first-- == 0) written.complete(null);
				if (done.size() >= MAX_PACKAGES || !done.add(pkg)) continue;
				final Set<String> refs = new TreeSet<>();
				try {
					write(pkg, stub(pkg, refs));
				}
				catch (final IOException | RuntimeException | LinkageError exc) {
					// NB: No stubs for this package, then.
				}
				final int d = depth.get(pkg);
				if (d >= DEPTH) continue;
				for (final String r : refs) {
					if (!done.contains(r) && !depth.containsKey(r)) {
						depth.put(r, d + 1);
						queue.add(r);
					}
				}
			}
			written.complete(null);
		});
		return written;
	}

	/**
	 * Gets the Java packages a Jython script imports from, e.g. {@code ij} for
	 * {@code from ij import IJ}, and {@code java.util} for
	 * {@code import java.util}.
	 */
	public static Set<String> imports(final String script) {
		final Set<String> packages = new TreeSet<>();
		// NB: Statements on one line are separated by semicolons.
		for (final String line : script.split("[\n;]")) {
			final String l = line.trim();
			String module = null;
			if (l.startsWith("from ")) {
				final int imp = l.indexOf(" import");
				module = imp < 0 ? l.substring(5) : l.substring(5, imp);
			}
			else if (l.startsWith("import ")) {
				module = l.substring(7).split("[ ,]")[0];
			}
			if (module == null) continue;
			module = module.trim();
			if (module.matches("[\\w.]+") && !module.endsWith(".")) {
				packages.add(module);
			}
		}
		return packages;
	}

	// -- Stub writing --

	/** Writes a package's stub, and its parents' (empty, if missing). */
	private void write(final String pkg, final String stub) throws IOException {
		if (stub == null) return; // NB: Not a Java package.
		File d = dir;
		for (final String part : pkg.split("\\.")) {
			d = new File(d, part);
			d.mkdirs();
			final File init = new File(d, "__init__.pyi");
			if (!init.exists()) Files.write(init.toPath(), new byte[0]);
		}
		final File init = new File(d, "__init__.pyi");
		final byte[] bytes = stub.getBytes(StandardCharsets.UTF_8);
		// NB: Unchanged files stay as they are, so the server need not reload.
		if (!Arrays.equals(Files.readAllBytes(init.toPath()), bytes)) {
			Files.write(init.toPath(), bytes);
		}
	}

	/**
	 * Builds the stub of a package, noting the other packages it refers to;
	 * or returns null if the package has no (public) classes.
	 */
	String stub(final String pkg, final Set<String> refs) {
		final List<Class<?>> classes = new ArrayList<>();
		for (final String name : classNames.apply(pkg)) {
			if (name.indexOf('$') >= 0) continue; // NB: Nested ones, in their outer.
			try {
				final Class<?> c = Class.forName(name, false, loader);
				if (Modifier.isPublic(c.getModifiers()) && !c.isSynthetic()) {
					classes.add(c);
				}
			}
			catch (final ClassNotFoundException | LinkageError exc) {
				// NB: Not loadable here; skip it.
			}
		}
		if (classes.isEmpty()) return null;
		classes.sort(Comparator.comparing(Class::getSimpleName));
		final StringBuilder body = new StringBuilder();
		for (final Class<?> c : classes) {
			try {
				classStub(c, pkg, refs, "", body);
			}
			catch (final LinkageError | SecurityException exc) {
				// NB: Its members refer to classes not loadable here; skip it.
			}
		}
		refs.remove(pkg);
		final StringBuilder sb = new StringBuilder("# Stubs of the Java package " +
			pkg + ", for code completion. Generated; do not edit.\n");
		sb.append("import typing\n");
		for (final String r : refs) sb.append("import ").append(r).append('\n');
		return sb.append(body).toString();
	}

	private void classStub(final Class<?> c, final String pkg,
		final Set<String> refs, final String indent, final StringBuilder sb)
	{
		final List<String> bases = new ArrayList<>();
		final Class<?> sup = c.getSuperclass();
		if (sup != null && sup != Object.class && Modifier.isPublic(sup
			.getModifiers())) bases.add(type(sup, pkg, refs));
		for (final Class<?> i : c.getInterfaces()) {
			if (Modifier.isPublic(i.getModifiers())) bases.add(type(i, pkg, refs));
		}
		if (bases.isEmpty() && c != Object.class) {
			bases.add(type(Object.class, pkg, refs));
		}
		sb.append('\n').append(indent).append("class ").append(c.getSimpleName());
		if (!bases.isEmpty()) sb.append('(').append(String.join(", ", bases))
			.append(')');
		sb.append(":\n");
		final String in = indent + "    ";
		final int length = sb.length();
		final Set<String> names = new HashSet<>();

		// Fields.
		for (final Field f : sorted(c.getDeclaredFields(), Field::getName)) {
			if (!Modifier.isPublic(f.getModifiers()) || f.isSynthetic() || !valid(f
				.getName())) continue;
			names.add(f.getName());
			final String t = type(f.getType(), pkg, refs);
			sb.append(in).append(f.getName()).append(": ").append(Modifier.isStatic(
				f.getModifiers()) ? "typing.ClassVar[" + t + "]" : t).append('\n');
		}

		// Constructors.
		final List<Constructor<?>> ctors = new ArrayList<>();
		for (final Constructor<?> ctor : c.getDeclaredConstructors()) {
			if (Modifier.isPublic(ctor.getModifiers()) && !ctor.isSynthetic()) {
				ctors.add(ctor);
			}
		}
		ctors.sort(Comparator.comparing(JavaStubs::signatureKey));
		for (final Constructor<?> ctor : ctors) {
			if (ctors.size() > 1) sb.append(in).append("@typing.overload\n");
			sb.append(in).append("def __init__(self").append(parameters(ctor, pkg,
				refs)).append(") -> None: ...\n");
		}

		// Methods, by name.
		final Map<String, List<Method>> methods = new LinkedHashMap<>();
		for (final Method m : sorted(c.getDeclaredMethods(), JavaStubs::signatureKey)) {
			if (!Modifier.isPublic(m.getModifiers()) || m.isSynthetic() || m
				.isBridge() || !valid(m.getName())) continue;
			methods.computeIfAbsent(m.getName(), k -> new ArrayList<>()).add(m);
		}
		for (final Map.Entry<String, List<Method>> e : methods.entrySet()) {
			names.add(e.getKey());
			for (final Method m : e.getValue()) {
				final boolean isStatic = Modifier.isStatic(m.getModifiers());
				if (e.getValue().size() > 1) sb.append(in).append(
					"@typing.overload\n");
				if (isStatic) sb.append(in).append("@staticmethod\n");
				sb.append(in).append("def ").append(m.getName()).append('(').append(
					isStatic ? parameters(m, pkg, refs).replaceFirst("^, ", "")
						: "self" + parameters(m, pkg, refs)).append(") -> ").append(
							type(m.getReturnType(), pkg, refs)).append(": ...\n");
			}
		}

		// Bean properties, as Jython offers them: getTitle() as title.
		for (final List<Method> overloads : methods.values()) {
			for (final Method m : overloads) {
				final String property = property(m);
				if (property == null || !names.add(property)) continue;
				sb.append(in).append(property).append(": ").append(type(m
					.getReturnType(), pkg, refs)).append('\n');
			}
		}

		// Nested classes.
		for (final Class<?> nested : sorted(c.getDeclaredClasses(),
			Class::getSimpleName))
		{
			if (Modifier.isPublic(nested.getModifiers()) && !nested.isSynthetic() &&
				valid(nested.getSimpleName()) && names.add(nested.getSimpleName()))
			{
				classStub(nested, pkg, refs, in, sb);
			}
		}
		if (sb.length() == length) sb.append(in).append("...\n");
	}

	/** Renders the parameters, each preceded by ", ". */
	private String parameters(final Executable e, final String pkg,
		final Set<String> refs)
	{
		final StringBuilder sb = new StringBuilder();
		final Set<String> used = new HashSet<>();
		used.add("self");
		final Parameter[] params = e.getParameters();
		for (int i = 0; i < params.length; i++) {
			final Parameter p = params[i];
			String name = p.isNamePresent() ? p.getName() : derivedName(p.getType());
			if (KEYWORDS.contains(name)) name += "_";
			while (!used.add(name)) name += i;
			final Class<?> type = e.isVarArgs() && i == params.length - 1 ? p
				.getType().getComponentType() : p.getType();
			sb.append(", ");
			if (e.isVarArgs() && i == params.length - 1) sb.append('*');
			sb.append(name).append(": ").append(type(type, pkg, refs));
		}
		return sb.toString();
	}

	/** The Python type of a Java one, as written in a stub of {@code pkg}. */
	String type(final Class<?> c, final String pkg, final Set<String> refs) {
		if (c == void.class) return "None";
		if (c == boolean.class || c == Boolean.class) return "bool";
		if (c == int.class || c == long.class || c == short.class ||
			c == byte.class || c == Integer.class || c == Long.class ||
			c == Short.class || c == Byte.class || c == BigInteger.class)
		{
			return "int";
		}
		if (c == float.class || c == double.class || c == Float.class ||
			c == Double.class || c == BigDecimal.class) return "float";
		if (c == char.class || c == Character.class || c == String.class) {
			return "str";
		}
		if (c.isArray()) return "list[" + type(c.getComponentType(), pkg, refs) +
			"]";
		if (!Modifier.isPublic(c.getModifiers()) || c.isAnonymousClass() || c
			.isLocalClass()) return "typing.Any";
		final Class<?> top = topLevel(c);
		final int dot = top.getName().lastIndexOf('.');
		final String topPkg = dot < 0 ? "" : top.getName().substring(0, dot);
		if (topPkg.isEmpty()) return "typing.Any";
		// NB: Nested classes by their path from the top-level class.
		final String path = c.getName().substring(topPkg.length() + 1).replace('$',
			'.');
		if (topPkg.equals(pkg)) return path;
		refs.add(topPkg);
		return topPkg + "." + path;
	}

	// -- Helper methods --

	private static Class<?> topLevel(Class<?> c) {
		while (c.getEnclosingClass() != null) c = c.getEnclosingClass();
		return c;
	}

	/** The property a getter offers (e.g. title for getTitle()), or null. */
	private static String property(final Method m) {
		if (Modifier.isStatic(m.getModifiers()) || m.getParameterCount() != 0 || m
			.getReturnType() == void.class) return null;
		final String name = m.getName();
		String rest = null;
		if (name.startsWith("get") && name.length() > 3) rest = name.substring(3);
		else if (name.startsWith("is") && name.length() > 2 && (m
			.getReturnType() == boolean.class)) rest = name.substring(2);
		if (rest == null || !Character.isUpperCase(rest.charAt(0))) return null;
		final String property = Character.toLowerCase(rest.charAt(0)) + rest
			.substring(1);
		return valid(property) ? property : null;
	}

	/** A parameter name from its type, e.g. imageProcessor, or values. */
	private static String derivedName(final Class<?> type) {
		if (type.isArray()) return "array";
		final String simple = type.getSimpleName();
		if (simple.isEmpty()) return "arg";
		return Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
	}

	/** Whether a name can be a Python identifier in a stub. */
	private static boolean valid(final String name) {
		return !name.isEmpty() && !KEYWORDS.contains(name) && name.indexOf(
			'$') < 0 && Character.isJavaIdentifierStart(name.charAt(0));
	}

	/** Orders overloads (and members) the same each time. */
	private static String signatureKey(final Executable e) {
		return e.getName() + Arrays.stream(e.getParameterTypes()).map(
			Class::getName).collect(Collectors.joining(",", "(", ")"));
	}

	private static <T> List<T> sorted(final T[] items,
		final Function<T, String> key)
	{
		final List<T> list = new ArrayList<>(Arrays.asList(items));
		list.sort(Comparator.comparing(key));
		return list;
	}

	/** Lists a package's classes from the {@link ClassIndex}. */
	private static List<String> indexedClassNames(final String pkg) {
		final String prefix = pkg + ".";
		return ClassIndex.findClassNamesForPackage(pkg).filter(n -> n.startsWith(
			prefix) && n.indexOf('.', prefix.length()) < 0).collect(Collectors
				.toList());
	}
}
