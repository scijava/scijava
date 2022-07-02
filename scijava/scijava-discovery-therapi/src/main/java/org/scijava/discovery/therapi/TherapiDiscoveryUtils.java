package org.scijava.discovery.therapi;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import org.scijava.parse2.Parser;

import com.github.therapi.runtimejavadoc.*;

/**
 * Utilities for discovery using Therapi
 * 
 * @author Gabriel Selzer
 */
public class TherapiDiscoveryUtils {

	/**
	 * Utility method to get the {@link Class} of name {@code qualifiedClassName}
	 * using {@link Class} {@code loader}
	 * 
	 * @param qualifiedClassName the name of the {@link Class} we want to laad
	 * @param loader             the {@link Class} responsible for loading.
	 * @return the {@link Class} of name {@code qualifiedClassName}
	 * @throws ClassNotFoundException if {@code qualifiedClassName} cannot be loaded
	 *                                using {@code loader}.
	 */
	private static Class<?> getClass(String qualifiedClassName, Class<?> loader) throws ClassNotFoundException {
		return loader.getClassLoader().loadClass(qualifiedClassName);
	}

	/**
	 * Uses {@link #javadocMethods(Class)}, but will not load any classes unless
	 * valid Javadoc can be found.
	 * 
	 * @param qualifiedClassName the qualifiedClassName to the class whose Javadoc
	 *                           we are interested in.
	 * @param loader             the {@link Class} able to load
	 *                           {@code qualifiedClassName}
	 * @return all {@link Method}s that have javadoc
	 * @see #javadocMethods(Class)
	 */
	public static Map<Method, MethodJavadoc> javadocMethods(String qualifiedClassName, Class<?> loader) {
		ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(qualifiedClassName, loader);
		if (classJavadoc.isEmpty())
			return Collections.emptyMap();
		try {
			return javadocMethods(getClass(qualifiedClassName, loader));
		} catch (ClassNotFoundException e) {
			return Collections.emptyMap();
		}
	}

	/**
	 * Finds all {@link Method}s of {@code c} that have Javadoc written for them
	 * 
	 * @param c the {@link Class} to search
	 * @return all {@link Method}s that have javadoc
	 */
	public static Map<Method, MethodJavadoc> javadocMethods(Class<?> c) {
		Method[] methods = c.getDeclaredMethods();

		List<MethodJavadoc> list = RuntimeJavadoc.getJavadoc(c).getMethods();

		Map<Method, MethodJavadoc> map = new HashMap<>(methods.length);
		for (MethodJavadoc javadoc : list) {
			Arrays.stream(methods) //
					.filter(javadoc::matches) //
					.findFirst() //
					.ifPresent(method -> map.put(method, javadoc));
		}
		return map;
	}

	/**
	 * Uses {@link #javadocFields(Class)}, but will not load any classes unless
	 * valid Javadoc can be found.
	 * 
	 * @param qualifiedClassName the fully qualified name of the {@link Class} we
	 *                           are interested in
	 * @param loader             the {@link Class} able to load
	 *                           {@code qualifiedClassName}
	 * @return all {@link Field}s that have javadoc
	 * @see #javadocFields(Class)
	 */
	public static Map<Field, FieldJavadoc> javadocFields(String qualifiedClassName, Class<?> loader) {
		ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(qualifiedClassName, loader);
		if (classJavadoc.isEmpty())
			return Collections.emptyMap();
		try {
			return javadocFields(getClass(qualifiedClassName, loader));
		} catch (ClassNotFoundException e) {
			return Collections.emptyMap();
		}
	}

	/**
	 * Finds all {@link Field}s of {@code c} that have Javadoc written for them
	 *
	 * @param c the {@link Class} to search
	 * @return all {@link Field}s that have javadoc
	 */
	public static Map<Field, FieldJavadoc> javadocFields(Class<?> c) {
		Field[] fields = c.getDeclaredFields();

		List<FieldJavadoc> list = RuntimeJavadoc.getJavadoc(c).getFields();

		Map<Field, FieldJavadoc> map = new HashMap<>(fields.length);
		for (FieldJavadoc javadoc : list) {
			Arrays.stream(fields) //
					.filter(f -> javadoc.getName().equals(f.getName())) //
					.findFirst() //
					.ifPresent(field -> map.put(field, javadoc));
		}
		return map;
	}

	/**
	 * Gets the javadoc for {@code c}
	 * 
	 * @param c the {@link Class} whose javadoc we are interested in
	 * @return the {@link ClassJavadoc} containing {@code c}'s javadoc
	 */
	public static ClassJavadoc javadoc(Class<?> c) {
		return RuntimeJavadoc.getJavadoc(c);
	}

	/**
	 * Gets the javadoc for {@code qualifiedClassName}, using {@code loader} to load
	 * the resource
	 * 
	 * @param qualifiedClassName the name of the {@link Class} whose javadoc we are
	 *                           interested in
	 * @param loader             the {@link Class} used to load the javadoc of
	 *                           {@code qualifiedClassName}
	 * @return the {@link ClassJavadoc} containing {@code qualifiedClassName}'s
	 *         javadoc
	 */
	public static ClassJavadoc javadoc(String qualifiedClassName, Class<?> loader) {
		return RuntimeJavadoc.getJavadoc(qualifiedClassName, loader);
	}

	/**
	 * Finds the (first) tag of {@code tagType} in {@code javadoc}, if it exists.
	 * 
	 * @param javadoc the {@link BaseJavadoc} to scan
	 * @param tagType the type of the tag
	 * @return the (first) tag of type {@code tagType}, if one exists.
	 */
	public static Optional<String> findTag(BaseJavadoc javadoc, String tagType) {
		return javadoc.getOther().stream() //
				.filter(m -> m.getName().equals("implNote") && m.getComment().toString().startsWith(tagType))
				.map(m -> m.getComment().toString()).findFirst();
	}

	/**
	 * Parses {@code tag} into a {@link Map} of items.
	 * 
	 * @param tagType the type of {@code tag}
	 * @param tag     the {@link String} containing items
	 * @param parser  the {@link Parser} that will parse {@code tag}
	 * @return a {@link Map} of items in {@code tag}
	 */
	private static Map<String, ?> itemsFromTag(String tagType, String tag, Parser parser) {
		String tagBody = tag.substring(tag.indexOf(tagType) + tagType.length()).trim();
		return parser.parse(tagBody.replaceAll("\\s+", ""), false).asMap();
	}

	/**
	 * Constructs a {@link TaggedElement} from an {@link AnnotatedElement} and its
	 * tag
	 * 
	 * @param javadoc the {@link BaseJavadoc} on {@code element}
	 * @param element the {@link AnnotatedElement} with javadoc
	 * @param tagType the type of the tag
	 * @param parser  the {@link Parser} to parse the tag
	 * @return a {@link TaggedElement}, if one can be made with {@code javadoc}
	 */
	public static Optional<TaggedElement> constructTaggedElement(BaseJavadoc javadoc, AnnotatedElement element,
			String tagType, Parser parser) {
		Optional<String> tag = findTag(javadoc, tagType);
		if (tag.isEmpty())
			return Optional.empty();
		return Optional.of(new TaggedElement(element, tagType, () -> itemsFromTag(tagType, tag.get(), parser)));
	}

	/**
	 * Finds all tagged classes, fields, and methods in {@code c}. Tags should
	 * follow the format:
	 * <p>
	 * {@code @implNote <tagType> [options]
	 * 
	<p>
	 * Tag options are parsed with {@link Parser} {@code parser}. @param c the
	 * {@Class} to search
	 *
	 * @param c the {@link Class} to search
	 * 
	 * @param tagType the {@link String} defining the type of tags to look for
	 * @param parser  the {@link Parser} to parse the options
	 * @return a {@link List} of {@link TaggedElement}s
	 */
	public static List<TaggedElement> taggedElementsFrom(Class<?> c, String tagType, Parser parser) {
		// fail fast if Therapi hasn't found any javadoc for that class.
		ClassJavadoc javadoc = javadoc(c);
		if (javadoc.isEmpty())
			return Collections.emptyList();

		List<TaggedElement> elements = new ArrayList<>();

		// Add class, if tagged
		Optional<TaggedElement> clsElement = constructTaggedElement(javadoc(c), c, tagType, parser);
		clsElement.ifPresent(elements::add);

		// Add all tagged methods
		List<TaggedElement> methodElements = javadocMethods(c).entrySet().stream() //
				.map(e -> constructTaggedElement(e.getValue(), e.getKey(), tagType, parser)) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.collect(Collectors.toList());
		elements.addAll(methodElements);

		// Add all tagged methods
		List<TaggedElement> fieldElements = javadocFields(c).entrySet().stream() //
				.map(e -> constructTaggedElement(e.getValue(), e.getKey(), tagType, parser)) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.collect(Collectors.toList());
		elements.addAll(fieldElements);

		return elements;
	}

	/**
	 * Returns true iff {@code c} has written javadoc
	 * 
	 * @param c the {@link Class} that may or may not have javadoc
	 * @return true iff {@code c} has written javadoc
	 */
	public static boolean hasJavadoc(Class<?> c) {
		return !javadoc(c).isEmpty();
	}

	/**
	 * Returns true iff the {@link Class} of name {@code qualifiedClassName} has
	 * written javadoc
	 * 
	 * @param qualifiedClassName the {@link Class} that may or may not have javadoc
	 * @param loader             the {@link Class} that can load the javadoc for
	 *                           {@code qualifiedClassName}
	 * @return true iff {@code c} has written javadoc
	 */
	public static boolean hasJavadoc(String qualifiedClassName, Class<?> loader) {
		return !javadoc(qualifiedClassName, loader).isEmpty();
	}

	/**
	 * Finds all tagged classes, fields, and methods in {@code c}. Tags should
	 * follow the format:
	 * <p>
	 * {@code @implNote <tagType> [options]
	 *
	<p>
	 * Tag options are parsed with {@link Parser} {@code parser}. @param c the
	 * {@Class} to search
	 *
	 * @param qualifiedClassName the <b>fully qualified</b> qualifiedClassName of
	 * the {@link Class} containing javadoc
	 * 
	 * @param loader  the {@link Class} to use to load the javadoc
	 * @param tagType the {@link String} defining the type of tags to look for
	 * @param parser  the {@link Parser} to parse the options
	 * @return a {@link List} of {@link TaggedElement}s
	 */
	public static List<TaggedElement> taggedElementsFrom(String qualifiedClassName, Class<?> loader, String tagType,
			Parser parser) {
		// fail fast if Therapi hasn't found any javadoc for that class.
		ClassJavadoc javadoc = javadoc(qualifiedClassName, loader);
		if (javadoc.isEmpty())
			return Collections.emptyList();
		try {
			return taggedElementsFrom(loader.getClassLoader().loadClass(qualifiedClassName), tagType, parser);
		} catch (ClassNotFoundException e) {
			return Collections.emptyList();
		}
	}

}
