/*-
 * #%L
 * SciJava library for generic type reasoning.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.types.extract;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.scijava.common3.Any;
import org.scijava.common3.GenericTyped;
import org.scijava.common3.Types;

import com.google.common.reflect.TypeToken;

public interface TypeReifier {

	/**
	 * Gets the type extractor which handles the given class, or null if none.
	 */
	Optional<TypeExtractor> getExtractor(Class<?> c);

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
	 * <p>
	 * For example, {@link IterableTypeExtractor} knows how to guess a {@code T}
	 * for any {@code Iterable<T>} by examining the type of the elements in its
	 * iteration. (Of course, this may be inaccurate if the elements in the
	 * iteration are heterogeneously typed, but for many use cases this guess is
	 * better than nothing.)
	 * </p>
	 * <p>
	 * In this way, the behavior of the generic type extraction is fully
	 * extensible, since additional {@link TypeExtractor} plugins can always be
	 * introduced which extract types more intelligently in cases where more <em>a
	 * priori</em> knowledge about that type is available at runtime.
	 * </p>
	 */
	default Type reify(final Object o) {
		// Anys cannot be resolved
		if (o == null) return new Any();

		// GenericTyped objects are easy - they know their type!
		if (o instanceof GenericTyped) {
			// Object implements the GenericTyped interface; it explicitly declares
			// the generic type by which it wants to be known. This makes life easy!
			return ((GenericTyped) o).type();
		}

		// Otherwise, we'll need to look at the class
		final Class<?> c = o.getClass();
		// NB TypeToken.getTypes() returns all subtypes before all supertypes.
		// This means that if we write a TypeExtractor that works on a subtype
		// and a TypeExtractor that will work on the supertype, we will always use
		// the subtype TypeExtractor first because we'll encounter the subtype
		// first.
		for (final var token : TypeToken.of(c).getTypes()) {
			Optional<TypeExtractor> opt = getExtractor(token.getRawType());
			// If token has a TypeExtractor
			if (opt.isPresent()) {
				// Use it!
				return opt.get().reify(this, o);
			}
		}

		// Otherwise, we aren't going to gain any extra information
		final TypeVariable<?>[] typeVars = c.getTypeParameters();
		// If the class has no type variables, just return it. Note that, for
		// extensibility reasons, this should happen after we check the type
		// extractors
		if (typeVars.length == 0) {
			return c;
		}
		// Otherwise parameterize with all Anys
		final Map<TypeVariable<?>, Type> resolved = new HashMap<>();
		for (final TypeVariable<?> typeVar : typeVars) {
			resolved.putIfAbsent(typeVar, Any.class);
		}
		return Types.parameterize(c, resolved);
	}
}
