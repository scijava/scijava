package org.scijava.types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;
import org.scijava.log2.Logger;

public class DefaultTypeReifier implements TypeReifier {

	private List<TypeExtractor> extractors;
	private final Logger log;
	private final Collection<Discoverer> discoverers;

	public DefaultTypeReifier(Logger log, Discoverer... discoverers) {
		this(log, Arrays.asList(discoverers));
	}

	public DefaultTypeReifier(Logger log, Collection<Discoverer> discoverers) {
		this.log = log;
		this.discoverers = discoverers;
	}

	@Override
	public Optional<TypeExtractor> getExtractor(final Class<?> c) {
		return extractors().stream().filter(t -> t.canReify(this, c)).findFirst();
	}

	@Override
	public Logger log() {
		return log;
	}

	// -- Helper methods --

	private List<TypeExtractor> extractors() {
		if (extractors == null) initExtractors();
		return extractors;
	}

	private synchronized void initExtractors() {
		if (extractors != null) return;
		extractors = getInstances();
	}

	private List<TypeExtractor> getInstances() {
		return discoverers.parallelStream() //
				.flatMap(d -> d.discover(TypeExtractor.class).stream()) //
				.sorted()
				.collect(Collectors.toList());
	}

	/**
	 * Extracts the generic {@link Type} of the given {@link Object}.
	 * <p>
	 * The ideal goal of the extraction is to reconstitute a fully concrete
	 * generic type, with all type variables fully resolved&mdash;e.g.:
	 * {@code ArrayList<Integer>} rather than a raw {@code ArrayList} class or
	 * even {@code ArrayList<N extends Number>}. Failing that, though, type
	 * variables which are still unknown after analysis will be replaced with
	 * wildcards&mdash;e.g., {@code HashMap} might become
	 * {@code HashMap<String, ?>} if a concrete type for the map values cannot be
	 * determined.
	 * </p>
	 * <p>
	 * For objects whose concrete type has no parameters, this method simply
	 * returns {@code o.getClass()}. For example:
	 * 
	 * <pre>
	 *      StringList implements List&lt;String&gt;
	 * </pre>
	 * 
	 * will return {@code StringList.class}.
	 * <p>
	 * The interesting case is for objects whose concrete class <em>does</em> have
	 * type parameters. E.g.:
	 * 
	 * <pre>
	 *      NumberList&lt;N extends Number&gt; implements List&lt;N&gt;
	 *      ListMap&lt;K, V, T&gt; implements Map&lt;K, V&gt;, List&lt;T&gt;
	 * </pre>
	 * 
	 * For such types, we try to fill the type parameters recursively, using
	 * {@link TypeExtractor} plugins that know how to glean types at runtime from
	 * specific sorts of objects.
	 * </p>
	 * <p>
	 * For example, {@link org.scijava.types.extractors.IterableTypeExtractor}
	 * knows how to guess a {@code T} for any {@code Iterable<T>} by examining the
	 * type of the elements in its iteration. (Of course, this may be inaccurate
	 * if the elements in the iteration are heterogeneously typed, but for many
	 * use cases this guess is better than nothing.)
	 * </p>
	 * <p>
	 * In this way, the behavior of the generic type extraction is fully
	 * extensible, since additional {@link TypeExtractor} plugins can always be
	 * introduced which extract types more intelligently in cases where more <em>a
	 * priori</em> knowledge about that type is available at runtime.
	 * </p>
	 */
	@Override
	public Type reify(final Object o) {
		if (o == null) return new Any();
		
		if (o instanceof GenericTyped) {
			// Object implements the GenericTyped interface; it explicitly declares
			// the generic type by which it wants to be known. This makes life easy!
			return ((GenericTyped) o).getType();
		}

		final Class<?> c = o.getClass();
		// if the class is synthetic, we are probably missing something due to
		// type erasure.
		if (c.isSynthetic()) {
			log().warn("Object " + o + " is synthetic. " +
					"Its type parameters are not reifiable and thus will likely cause unintended behavior!");
		}

		Optional<TypeExtractor> extractor = getExtractor(c);
		if (extractor.isPresent()) {
			return extractor.get().reify(this, o);
		}
		else {
			return parameterizeWithAny(o);
		}
	}

	private Type parameterizeWithAny(Object o) {
		final Class<?> c = o.getClass();
		final TypeVariable<?>[] typeVars = c.getTypeParameters();
		final Map<TypeVariable<?>, Type> map = new HashMap<>();
		for (TypeVariable<?> typeVar : typeVars) {
			map.putIfAbsent(typeVar, new Any());
		}
		if (typeVars.length > 0) {
			return Types.parameterize(c, map);
		}
		return c;
	}
}
