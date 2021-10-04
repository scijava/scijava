
package org.scijava.ops.engine.impl;

import com.github.therapi.runtimejavadoc.BaseJavadoc;
import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.FieldJavadoc;
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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;

public class TherapiDiscoverer implements Discoverer {

	BiPredicate<BaseJavadoc, String[]> hasTags = (mjavadoc, tags) -> {
		for (String tag : tags) {
			boolean hasTag = mjavadoc.getOther().parallelStream() //
					.anyMatch(m -> m
				.getName().equals("implNote") && m.getComment().toString().contains(
					tag));
			if (!hasTag)
				return false;
		}
		return true;
	};

	@Override
	public List<AnnotatedElement> elementsTaggedWith(String... tags) {
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
				c = Class.forName(e.getValue().get(0));
				e.getValue().parallelStream().forEach(s -> javadocData.put(
					RuntimeJavadoc.getJavadoc(s, c), s));
			}
			catch (ClassNotFoundException exc) {
				return;
			}
		});

		List<AnnotatedElement> taggedClasses = javadocData.entrySet()
			.parallelStream() //
			.filter(e -> hasTags.test(e.getKey(), tags)) //
			.map(e -> {
				try {
					return Class.forName(e.getValue());
				}
				catch (ClassNotFoundException exc) {
					return null;
				}
			}).filter(c -> c != null).collect(Collectors.toList());
		List<AnnotatedElement> taggedMethods = javadocData.entrySet()
			.parallelStream() //
			.map(e -> taggedMethodFinder.apply(e, tags)) //
			.flatMap(list -> list.parallelStream()) //
			.collect(Collectors.toList());
		List<AnnotatedElement> taggedFields = javadocData.entrySet()
			.parallelStream() //
			.map(e -> taggedFieldFinder.apply(e, tags)) //
			.flatMap(list -> list.parallelStream()) //
			.collect(Collectors.toList());
		taggedClasses.addAll(taggedMethods);
		taggedClasses.addAll(taggedFields);
		return taggedClasses;
	}

	/**
	 * Using a string {@code className}, finds a list of tagged methods
	 */
	private final BiFunction<Map.Entry<ClassJavadoc, String>, String[], List<Method>> taggedMethodFinder =
		(entry, tags) -> {
			List<MethodJavadoc> taggedJavadoc = entry.getKey().getMethods()
				.parallelStream().filter(m -> hasTags.test(m, tags)).collect(Collectors.toList());

			if (taggedJavadoc.isEmpty()) return Collections.emptyList();

			Method[] methods;
			try {
				methods = Class.forName(entry.getValue()).getDeclaredMethods();
			}
			catch (SecurityException | ClassNotFoundException exc) {
				return Collections.emptyList();
			}
			List<Method> taggedClassMethods = taggedJavadoc.parallelStream() //
				.map(mJavadoc -> Arrays.stream(methods).filter(m -> mJavadoc.matches(m))
					.findAny()) //
				.filter(optional -> optional.isPresent()) //
				.map(o -> o.get()).collect(Collectors.toList());
			return taggedClassMethods;

		};

	private final BiFunction<Map.Entry<ClassJavadoc, String>, String[], List<Field>> taggedFieldFinder =
		(entry, tags) -> {
			List<FieldJavadoc> taggedJavadoc = entry.getKey().getFields()
				.parallelStream().filter(j -> hasTags.test(j, tags)).collect(Collectors.toList());

			if (taggedJavadoc.isEmpty()) return Collections.emptyList();

			Field[] fields;
			try {
				fields = Class.forName(entry.getValue()).getDeclaredFields();
			}
			catch (SecurityException | ClassNotFoundException exc) {
				return Collections.emptyList();
			}
			List<Field> taggedClassFields = taggedJavadoc.parallelStream() //
				.map(fJavadoc -> Arrays.stream(fields).filter(f -> fJavadoc.getName()
					.equals(f.getName())).findAny()) //
				.filter(optional -> optional.isPresent()) //
				.map(o -> o.get()).collect(Collectors.toList());
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
