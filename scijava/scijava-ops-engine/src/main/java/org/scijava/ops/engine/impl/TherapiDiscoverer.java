
package org.scijava.ops.engine.impl;

import com.github.therapi.runtimejavadoc.BaseJavadoc;
import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;

public class TherapiDiscoverer implements Discoverer {

	private static Class<?> getClass(String name) throws ClassNotFoundException {
		return getClassLoader().loadClass(name);
	}

	/**
	 * Gets the class loader to use. This will be the current thread's context
	 * class loader if non-null; otherwise it will be the system class loader.
	 * <p>
	 * Forked from SciJava Common's Context class.
	 * 
	 * @see Thread#getContextClassLoader()
	 * @see ClassLoader#getSystemClassLoader()
	 */
	private static ClassLoader getClassLoader() {
		final ClassLoader contextCL = Thread.currentThread()
			.getContextClassLoader();
		return contextCL != null ? contextCL : ClassLoader.getSystemClassLoader();
	}

	BiFunction<BaseJavadoc, String, Optional<String>> getTag = (javadoc, tagType) -> {
			return javadoc.getOther().stream() //
			.filter(m -> m.getName().equals("implNote") && m.getComment().toString()
				.startsWith(tagType)).map(m -> m.getComment().toString()).findFirst();
			
	};

	@Override
	public List<Discovery<AnnotatedElement>> elementsTaggedWith(String tagType) {
		List<String> paths = new ArrayList<>(Arrays.asList(System.getProperty(
			"java.class.path").split(File.pathSeparator)));
		paths.addAll(Arrays.asList(System.getProperty("jdk.module.path").split(
			File.pathSeparator)));
		Map<String, List<String>> javadocedFiles = new HashMap<>();
		paths.parallelStream().forEach(p -> {
			List<String> files = getJavadocedFiles(p).parallelStream().map(
				f -> getFullyQualifiedName(f, p)).collect(Collectors.toList());
			if (!files.isEmpty()) javadocedFiles.put(p, files);
		});
		Map<ClassJavadoc, String> javadocData = new HashMap<>();
		javadocedFiles.entrySet().parallelStream().forEach(e -> {
			Class<?> c;
			try {
				c = getClass(e.getValue().get(0));
				e.getValue().parallelStream().forEach(s -> javadocData.put(
					RuntimeJavadoc.getJavadoc(s, c), s));
			}
			catch (ClassNotFoundException exc) {
				return;
			}
		});

		List<Discovery<AnnotatedElement>> taggedClasses = javadocData.entrySet()
			.parallelStream() //
			.map(e -> {
				try {
					Class<?> taggedClass = getClass(e.getValue());
					Optional<String> tag = getTag.apply(e.getKey(), tagType);
					if (tag.isEmpty()) return null;
					return new Discovery<AnnotatedElement>(taggedClass, tag.get());
				}
				catch (ClassNotFoundException exc) {
					return null;
				}
			}).filter(c -> c != null).collect(Collectors.toList());
		List<Discovery<AnnotatedElement>> taggedMethods = javadocData.entrySet()
			.parallelStream() //
			.map(e -> taggedMethodFinder.apply(e, tagType)) //
			.flatMap(list -> list.parallelStream()) //
			.collect(Collectors.toList());
		List<Discovery<AnnotatedElement>> taggedFields = javadocData.entrySet()
			.parallelStream() //
			.map(e -> taggedFieldFinder.apply(e, tagType)) //
			.flatMap(list -> list.parallelStream()) //
			.collect(Collectors.toList());
		taggedClasses.addAll(taggedMethods);
		taggedClasses.addAll(taggedFields);
		return taggedClasses;
	}

		private ThreadLocal<WeakHashMap<MethodJavadoc, String>> methodJavadocMap = new ThreadLocal<>() {
			@Override
			protected WeakHashMap<MethodJavadoc, String> initialValue() {
				return new WeakHashMap<>();
			}
			
		};

	/**
	 * Using a string {@code className}, finds a list of tagged methods
	 */
	private final BiFunction<Map.Entry<ClassJavadoc, String>, String, List<Discovery<AnnotatedElement>>> taggedMethodFinder =
		(entry, tagType) -> {
			methodJavadocMap.get().clear();
			entry.getKey().getMethods().parallelStream() //
			.forEach(j -> {
				Optional<String> tag = getTag.apply(j, tagType);
				if (tag.isPresent()) {
					methodJavadocMap.get().put(j, tag.get());
				}

			});

			if(methodJavadocMap.get().isEmpty()) return Collections.emptyList();

			Method[] methods;
			try {
				methods = getClass(entry.getValue()).getDeclaredMethods();
			}
			catch (ClassNotFoundException exc) {
				return Collections.emptyList();
			}
			List<Discovery<AnnotatedElement>> taggedClassMethods = methodJavadocMap.get().entrySet().parallelStream() //
				.map(e -> {
					Optional<Method> taggedMethod = Arrays.stream(methods).filter(m -> e.getKey().matches(m))
					.findAny(); //
					if (taggedMethod.isEmpty()) return null;
					return new Discovery<AnnotatedElement>(taggedMethod.get(), e.getValue());
				})
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
			return taggedClassMethods;

		};

		private ThreadLocal<WeakHashMap<BaseJavadoc, String>> funcJavadocMap = new ThreadLocal<>() {
			@Override
			protected WeakHashMap<BaseJavadoc, String> initialValue() {
				return new WeakHashMap<>();
			}
			
		};


	private final BiFunction<Map.Entry<ClassJavadoc, String>, String, List<Discovery<AnnotatedElement>>> taggedFieldFinder =
		(entry, tagType) -> {
			funcJavadocMap.get().clear();
			entry.getKey().getFields().parallelStream() //
			.forEach(j -> {
				Optional<String> tag = getTag.apply(j, tagType);
				if (tag.isPresent()) {
					funcJavadocMap.get().put(j, tag.get());
				}

			});

			if (funcJavadocMap.get().isEmpty()) return Collections.emptyList();

			Field[] fields;
			try {
				fields = getClass(entry.getValue()).getDeclaredFields();
			}
			catch (ClassNotFoundException exc) {
				return Collections.emptyList();
			}
			List<Discovery<AnnotatedElement>> taggedClassFields = funcJavadocMap.get().entrySet().parallelStream() //
				.map(e -> {
					Optional<Field> taggedField = Arrays.stream(fields).filter(f -> e.getKey().getName()
					.equals(f.getName())).findAny(); //
					if (taggedField.isEmpty()) return null;
					return new Discovery<AnnotatedElement>(taggedField.get(), e.getValue());
				})
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
			return taggedClassFields;

		};

	/**
	 * list files in the given directory and subdirs (with recursion)
	 * 
	 * @param paths
	 * @return
	 */
	public static List<File> getJavadocedFiles(String path) {
		List<File> filesList = new ArrayList<File>();
		final File file = new File(path);
		if (file.isDirectory()) {
			recurse(filesList, file);
		}
		else if (file.getPath().endsWith(".jar")) {
			try {
				for (String s : getJarContent(path))
					if (s.endsWith("__Javadoc.json")) filesList.add(file);
			}
			catch (IOException exc) {
				// TODO Auto-generated catch block
				exc.printStackTrace();
			}
		}
		else {
			if (path.endsWith("__Javadoc.json")) filesList.add(file);
		}
		return filesList;
	}

	private static void recurse(List<File> filesList, File f) {
		File list[] = f.listFiles();
		for (File file : list) {
			if (file.isDirectory()) {
				recurse(filesList, file);
			}
			else {
				if (file.getPath().endsWith("__Javadoc.json")) filesList.add(file);
			}
		}
	}

	private static String getFullyQualifiedName(File f, String path) {
		return f.getPath().substring(path.length() + 1, f.getPath().indexOf(
			"__Javadoc.json")).replace(System.getProperty("file.separator"), ".");
	}

	/**
	 * List the content of the given jar
	 * 
	 * @param jarPath
	 * @return
	 * @throws IOException
	 */
	public static List<String> getJarContent(String jarPath) throws IOException {
		List<String> content = new ArrayList<String>();
		JarFile jarFile = new JarFile(jarPath);
		Enumeration<JarEntry> e = jarFile.entries();
		while (e.hasMoreElements()) {
			JarEntry entry = (JarEntry) e.nextElement();
			String name = entry.getName();
			content.add(name);
		}
		return content;
	}

}
