
package org.scijava.discovery.therapi;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;
import org.scijava.parse2.Parser;

public abstract class TherapiDiscoverer implements Discoverer {

	private static Class<?> getClass(String name) throws ClassNotFoundException {
		return getClassLoader().loadClass(name);
	}

	/**
	 * Gets the class loader to use. This will be the current thread's context class
	 * loader if non-null; otherwise it will be the system class loader.
	 * <p>
	 * Forked from SciJava Common's Context class.
	 *
	 * @see Thread#getContextClassLoader()
	 * @see ClassLoader#getSystemClassLoader()
	 */
	private static ClassLoader getClassLoader() {
		final ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
		return contextCL != null ? contextCL : ClassLoader.getSystemClassLoader();
	}

	private static String getFullyQualifiedName(File f, String path) {
		if (f.getPath().contains(path)) {
			return f.getPath().substring(path.length() + 1, f.getPath().indexOf("__Javadoc.json"))
					.replace(System.getProperty("file.separator"), ".");
		}
		return f.getPath().substring(0, f.getPath().indexOf("__Javadoc.json"))
				.replace(System.getProperty("file.separator"), ".");
	}

	/**
	 * List the content of the given jar
	 * 
	 * @param jarPath the path to a jar
	 * @return the contents of the jar
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

	/**
	 * list files in the given directory and subdirs (with recursion)
	 * 
	 * @param path the directory to recurse through
	 * @return a list of all files contained within the root directory {@code path}
	 */
	private static List<File> getJavadocedFiles(String path) {
		List<File> filesList = new ArrayList<>();
		final File file = new File(path);
		if (file.isDirectory()) {
			recurse(filesList, file);
		} else if (file.getPath().endsWith(".jar")) {
			try {
				for (String s : getJarContent(path))
					if (s.endsWith("__Javadoc.json")) {
						filesList.add(new File(s));
					}
			} catch (IOException exc) {
				// TODO Auto-generated catch block
				exc.printStackTrace();
			}
		} else {
			if (path.endsWith("__Javadoc.json"))
				filesList.add(file);
		}
		return filesList;
	}

	private static void recurse(List<File> filesList, File f) {
		File[] list = f.listFiles();
		if (list == null)
			return;
		for (File file : list) {
			if (file.isDirectory()) {
				recurse(filesList, file);
			} else {
				if (file.getPath().endsWith("__Javadoc.json"))
					filesList.add(file);
			}
		}
	}

	private final Parser parser;

	public TherapiDiscoverer(Parser parser) {
		this.parser = parser;
	}

	public abstract boolean canDiscover(Class<?> cls);

	private List<String> classAndModulePathResources() {
		// get classpath resources
		List<String> paths = new ArrayList<>(
				Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator)));
		// add modulepath resources
		String modulePath = System.getProperty("jdk.module.path");
		if (modulePath != null) {
			paths.addAll(Arrays.asList(modulePath.split(File.pathSeparator)));
		}
		return paths;
	}

	protected abstract <U> Optional<U> convert(TaggedElement e, Class<U> c);

	private <U> List<U> convertEach(List<TaggedElement> elements, Class<U> c) {
		return elements.parallelStream() //
				.map(e -> convert(e, c)) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.collect(Collectors.toList());
	}

	@Override
	public <U> List<U> discover(Class<U> c) {
		if (!canDiscover(c))
			return Collections.emptyList();
		List<TaggedElement> elements = elementsTaggedWith(tagType());
		return convertEach(elements, c);
	}

	public List<TaggedElement> elementsTaggedWith(String tagType) {
		// combine class and module path resources into a single list
		List<String> paths = classAndModulePathResources();

		// for each path element, find the list of classes along that path with recorded
		// javadoc
		List<Class<?>> classesWithJavadoc = javadocClasses(paths);

		return classesWithJavadoc.parallelStream() //
				.flatMap(c -> TherapiDiscoveryUtils.taggedElementsFrom(c, tagType, parser).stream()) //
				.collect(Collectors.toList());
	}

	private List<Class<?>> javadocClasses(List<String> paths) {
		// for each path, find the list of classes whose javadoc has been retained
		Map<String, List<String>> javadocedFiles = new HashMap<>();
		paths.parallelStream().forEach(p -> {
			List<String> files = getJavadocedFiles(p).parallelStream().map(field -> getFullyQualifiedName(field, p))
					.collect(Collectors.toList());
			if (!files.isEmpty())
				javadocedFiles.put(p, files);
		});

		// for each javadoc'd class, find its javadoc.
		List<Class<?>> javadocData = new ArrayList<>();
		javadocedFiles.forEach((key, value) -> {
			Class<?> c;
			try {
				c = getClass(value.get(0));
				// TODO: Using parallelStream() here introduces unreliability...
				value.stream().filter(path -> TherapiDiscoveryUtils.hasJavadoc(path, c)).forEach(path -> {
					try {
						javadocData.add(c.getClassLoader().loadClass(path));
					} catch (ClassNotFoundException ex) {
						ex.printStackTrace();
					}
				});
			} catch (ClassNotFoundException ignored) {
			}
		});

		return javadocData;
	}

	public abstract String tagType();
}
