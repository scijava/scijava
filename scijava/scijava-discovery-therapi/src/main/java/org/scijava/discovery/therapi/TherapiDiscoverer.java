
package org.scijava.discovery.therapi;

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
import java.util.Collection;
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
import java.util.stream.Stream;

import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;

public class TherapiDiscoverer implements Discoverer {

	@Override
	public List<Discovery<AnnotatedElement>> elementsTaggedWith(String tagType) {
		// combine class and module path resources into a single list
		List<String> paths = classAndModulePathResources();

		// for each path element, find the list of files whose javadoc has been
		// retained.
		Map<ClassJavadoc, String> javadocData = getJavadocs(paths);

		List<Discovery<AnnotatedElement>> taggedClasses = discoverTaggedClasses(
			tagType, javadocData);
		List<Discovery<AnnotatedElement>> taggedMethods = discoverTaggedMethods(
			tagType, javadocData);
		List<Discovery<AnnotatedElement>> taggedFields = discoverTaggedFields(
			tagType, javadocData);

		// return concatenation of classes, methods, and fields.
		return Stream.of(taggedClasses, taggedMethods, taggedFields) //
			.flatMap(Collection::stream) //
			.collect(Collectors.toList());
	}

	private List<String> classAndModulePathResources() {
		// get classpath resources
		List<String> paths = new ArrayList<>(Arrays.asList(System.getProperty(
			"java.class.path").split(File.pathSeparator)));
		// add modulepath resources
		paths.addAll(Arrays.asList(System.getProperty("jdk.module.path").split(
			File.pathSeparator)));
		return paths;
	}

	private Map<ClassJavadoc, String> getJavadocs(List<String> paths) {
		// for each path, find the list of classes whose javadoc has been retained
		Map<String, List<String>> javadocedFiles = new HashMap<>();
		paths.parallelStream().forEach(p -> {
			List<String> files = getJavadocedFiles(p).parallelStream().map(
				field -> getFullyQualifiedName(field, p)).collect(Collectors.toList());
			if (!files.isEmpty()) javadocedFiles.put(p, files);
		});

		// for each javadoc'd class, find its javadoc.
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

		return javadocData;
	}

	/**
	 * list files in the given directory and subdirs (with recursion)
	 * 
	 * @param path the directory to recurse through
	 * @return a list of all files contained within the root directory
	 *         {@code path}
	 */
	private static List<File> getJavadocedFiles(String path) {
		List<File> filesList = new ArrayList<>();
		final File file = new File(path);
		if (file.isDirectory()) {
			recurse(filesList, file);
		}
		else if (file.getPath().endsWith(".jar")) {
			try {
				for (String s : getJarContent(path))
					if (s.endsWith("__Javadoc.json")) {
						filesList.add(new File(s));
					}
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

	private static Class<?> getClass(String name) throws ClassNotFoundException {
		return getClassLoader().loadClass(name);
	}

	/**
	 * Gets the class loader to use. This will be the current thread's context
	 * class loader if non-null; otherwise it will be the system class loader.
	 * <p>
	 * Forked from SciJava Common's Context class.
	 *
	 * @implNote test
	 * @see Thread#getContextClassLoader()
	 * @see ClassLoader#getSystemClassLoader()
	 */
	private static ClassLoader getClassLoader() {
		final ClassLoader contextCL = Thread.currentThread()
			.getContextClassLoader();
		return contextCL != null ? contextCL : ClassLoader.getSystemClassLoader();
	}

	private static String getFullyQualifiedName(File f, String path) {
		if (f.getPath().contains(path)) {
			return f.getPath().substring(path.length() + 1, f.getPath().indexOf(
				"__Javadoc.json")).replace(System.getProperty("file.separator"), ".");
		}
		return f.getPath().substring(0, f.getPath().indexOf("__Javadoc.json"))
			.replace(System.getProperty("file.separator"), ".");
	}

	/**
	 * List the content of the given jar
	 * 
	 * @param jarPath the path to a jar
	 * @return the contents of the jar
	 * @throws IOException
	 */
	private static List<String> getJarContent(String jarPath) throws IOException {
		List<String> content = new ArrayList<>();
		try (JarFile jarFile = new JarFile(jarPath)) {
			Enumeration<JarEntry> e = jarFile.entries();
			while (e.hasMoreElements()) {
				JarEntry entry = e.nextElement();
				String name = entry.getName();
				content.add(name);
			}
		}
		return content;
	}

	private ThreadLocal<WeakHashMap<BaseJavadoc, String>> funcJavadocMap =
		new ThreadLocal<>()
		{

			@Override
			protected WeakHashMap<BaseJavadoc, String> initialValue() {
				return new WeakHashMap<>();
			}

		};

	BiFunction<BaseJavadoc, String, Optional<String>> getTag = (javadoc,
		tagType) -> {
		return javadoc.getOther().stream() //
			.filter(m -> m.getName().equals("implNote") && m.getComment().toString()
				.startsWith(tagType)).map(m -> m.getComment().toString()).findFirst();

	};

	private ThreadLocal<WeakHashMap<MethodJavadoc, String>> methodJavadocMap =
		new ThreadLocal<>()
		{

			@Override
			protected WeakHashMap<MethodJavadoc, String> initialValue() {
				return new WeakHashMap<>();
			}

		};

	private final BiFunction<Map.Entry<BaseJavadoc, String>, Field[], Discovery<AnnotatedElement>> fieldFinder =
		(e, fields) -> {
			Optional<Field> taggedField = Arrays.stream(fields).filter(field -> e
				.getKey().getName().equals(field.getName())).findAny(); //
			if (taggedField.isEmpty()) return null;
			return new Discovery<>(taggedField.get(), e.getValue());
		};

	private final BiFunction<Map.Entry<ClassJavadoc, String>, String, List<Discovery<AnnotatedElement>>> taggedFieldFinder =
		(entry, tagType) -> {
			// find each field with the tag in the given Class
			funcJavadocMap.get().clear();
			entry.getKey().getFields().parallelStream() //
				.forEach(j -> {
					Optional<String> tag = getTag.apply(j, tagType);
					if (tag.isPresent()) {
						funcJavadocMap.get().put(j, tag.get());
					}

				});
			if (funcJavadocMap.get().isEmpty()) return Collections.emptyList();

			// match each tagged FieldJavadoc with the Field it was generated from
			Field[] fields;
			try {
				fields = getClass(entry.getValue()).getDeclaredFields();
			}
			catch (ClassNotFoundException exc) {
				return Collections.emptyList();
			}
			List<Discovery<AnnotatedElement>> taggedClassFields = funcJavadocMap.get()
				.entrySet().parallelStream() //
				.map(e -> fieldFinder.apply(e, fields)) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
			return taggedClassFields;

		};

	private final BiFunction<Map.Entry<MethodJavadoc, String>, Method[], Discovery<AnnotatedElement>> methodFinder =
		(e, methods) -> {
			Optional<Method> taggedMethod = Arrays.stream(methods).filter(m -> e
				.getKey().matches(m)).findAny(); //
			if (taggedMethod.isEmpty()) return null;
			return new Discovery<>(taggedMethod.get(), e.getValue());
		};

	/**
	 * Using a string {@code className}, finds a list of tagged methods
	 */
	private final BiFunction<Map.Entry<ClassJavadoc, String>, String, List<Discovery<AnnotatedElement>>> taggedMethodFinder =
		(entry, tagType) -> {
			// finds each tagged method in the Class
			methodJavadocMap.get().clear();
			entry.getKey().getMethods().parallelStream() //
				.forEach(j -> {
					Optional<String> tag = getTag.apply(j, tagType);
					if (tag.isPresent()) {
						methodJavadocMap.get().put(j, tag.get());
					}

				});
			if (methodJavadocMap.get().isEmpty()) return Collections.emptyList();

			// maps each MethodJavadoc to the method it was scraped from
			Method[] methods;
			try {
				methods = getClass(entry.getValue()).getDeclaredMethods();
			}
			catch (ClassNotFoundException exc) {
				return Collections.emptyList();
			}
			List<Discovery<AnnotatedElement>> taggedClassMethods = methodJavadocMap
				.get().entrySet().parallelStream() //
				.map(e -> methodFinder.apply(e, methods)) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());
			return taggedClassMethods;

		};

	private List<Discovery<AnnotatedElement>> discoverTaggedClasses(
		String tagType, Map<ClassJavadoc, String> javadocData)
	{
		return javadocData.entrySet().parallelStream() //
			.map(e -> classFinder.apply(e, tagType)) //
			.filter(c -> c != null) //
			.collect(Collectors.toList());
	}

	private final BiFunction<Map.Entry<ClassJavadoc, String>, String, Discovery<AnnotatedElement>> classFinder =
		(e, tagType) -> {
			try {
				Class<?> taggedClass = getClass(e.getValue());
				Optional<String> tag = getTag.apply(e.getKey(), tagType);
				if (tag.isEmpty()) return null;
				return new Discovery<>(taggedClass, tag.get());
			}
			catch (ClassNotFoundException exc) {
				return null;
			}

		};

	private List<Discovery<AnnotatedElement>> discoverTaggedFields(String tagType,
		Map<ClassJavadoc, String> javadocData)
	{
		return javadocData.entrySet().parallelStream() //
			.map(e -> taggedFieldFinder.apply(e, tagType)) //
			.flatMap(list -> list.parallelStream()) //
			.collect(Collectors.toList());
	}

	private List<Discovery<AnnotatedElement>> discoverTaggedMethods(
		String tagType, Map<ClassJavadoc, String> javadocData)
	{
		return javadocData.entrySet().parallelStream() //
			.map(e -> taggedMethodFinder.apply(e, tagType)) //
			.flatMap(list -> list.parallelStream()) //
			.collect(Collectors.toList());
	}

}
