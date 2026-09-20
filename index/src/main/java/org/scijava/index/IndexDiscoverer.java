/*
 * #%L
 * Annotation indexing, for discovery without class loading.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.index;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;
import org.scijava.priority.Priority;

/**
 * A {@link Discoverer} backed by an annotation {@link Index}.
 * <p>
 * This is the discovery mechanism that can answer <em>what is available</em>
 * without loading a single implementation class: the index records each
 * implementation's class name and its annotation values, so filtering,
 * sorting and display all happen on metadata alone. Only
 * {@link Discovery#type()} and {@link Discovery#get()} touch the class.
 * </p>
 * <p>
 * Which type an indexed implementation provides has to be answered from that
 * metadata too, hence {@code typeNameOf}: given an indexed item, it yields the
 * name of the type that item provides. For an annotation in the shape of
 * {@code @Plugin(type = Service.class)}, that is
 * {@code item -> item.annotation().type().getName()} - which loads the
 * <em>declared</em> type, never the implementation.
 * </p>
 *
 * @param <A> the indexed annotation type
 * @author Curtis Rueden
 */
public class IndexDiscoverer<A extends Annotation> implements Discoverer {

	private final Class<A> annotation;
	private final ClassLoader classLoader;
	private final Function<IndexItem<A>, String> typeNameOf;
	private final Function<IndexItem<A>, Map<String, String>> attrsOf;
	private final ToDoubleFunction<IndexItem<A>> priorityOf;
	private final Function<Class<?>, ?> instantiator;

	/**
	 * Creates a discoverer over the index of the given annotation.
	 *
	 * @param annotation the indexed annotation
	 * @param typeNameOf yields the name of the type an indexed item provides
	 */
	public IndexDiscoverer(final Class<A> annotation,
		final Function<IndexItem<A>, String> typeNameOf)
	{
		this(annotation, typeNameOf, item -> Collections.emptyMap(),
			item -> Priority.NORMAL, null, null);
	}

	/**
	 * Creates a discoverer over the index of the given annotation.
	 *
	 * @param annotation the indexed annotation
	 * @param typeNameOf yields the name of the type an indexed item provides
	 * @param attrsOf yields the metadata of an indexed item
	 * @param priorityOf yields the priority of an indexed item
	 * @param classLoader the class loader to query, or null for the thread's
	 *          context class loader
	 */
	public IndexDiscoverer(final Class<A> annotation,
		final Function<IndexItem<A>, String> typeNameOf,
		final Function<IndexItem<A>, Map<String, String>> attrsOf,
		final ToDoubleFunction<IndexItem<A>> priorityOf,
		final ClassLoader classLoader)
	{
		this(annotation, typeNameOf, attrsOf, priorityOf, classLoader, null);
	}

	/**
	 * Creates a discoverer over the index of the given annotation.
	 *
	 * @param annotation the indexed annotation
	 * @param typeNameOf yields the name of the type an indexed item provides
	 * @param attrsOf yields the metadata of an indexed item
	 * @param priorityOf yields the priority of an indexed item
	 * @param classLoader the class loader to query, or null for the thread's
	 *          context class loader
	 * @param instantiator constructs a discovered object, or null to construct
	 *          it here with the no-argument constructor
	 *          <p>
	 *          NB: this matters under JPMS. Reflective construction is checked
	 *          against the module performing it, so a class in a package opened
	 *          only to some container cannot be constructed by this module on
	 *          that container's behalf. A container passes an instantiator
	 *          defined in its own module, and users open their implementation
	 *          packages to that container rather than to this one.
	 *          </p>
	 */
	public IndexDiscoverer(final Class<A> annotation,
		final Function<IndexItem<A>, String> typeNameOf,
		final Function<IndexItem<A>, Map<String, String>> attrsOf,
		final ToDoubleFunction<IndexItem<A>> priorityOf,
		final ClassLoader classLoader, final Function<Class<?>, ?> instantiator)
	{
		this.annotation = annotation;
		this.typeNameOf = typeNameOf;
		this.attrsOf = attrsOf;
		this.priorityOf = priorityOf;
		this.classLoader = classLoader;
		this.instantiator = instantiator;
	}

	@Override
	public <U> List<Discovery<U>> discover(final Class<U> c) {
		final ClassLoader loader = classLoader != null ? classLoader : //
			Thread.currentThread().getContextClassLoader();
		final List<Discovery<U>> discoveries = new ArrayList<>();
		for (final IndexItem<A> item : Index.load(annotation, loader)) {
			final String typeName;
			try {
				typeName = typeNameOf.apply(item);
			}
			catch (final RuntimeException exc) {
				// NB: a malformed index entry must not sink every other discovery.
				continue;
			}
			if (!c.getName().equals(typeName)) continue;
			@SuppressWarnings("unchecked")
			final Function<Class<? extends U>, ? extends U> construct =
				instantiator == null ? null //
					: t -> (U) instantiator.apply(t);
			discoveries.add(Discovery.of(item.className(), c, loader, //
				attrsOf.apply(item), priorityOf.applyAsDouble(item), construct));
		}
		return discoveries;
	}
}
