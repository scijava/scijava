package org.scijava.ops.engine.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;
import org.scijava.log2.Logger;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;

/**
 * Shared functionality for {@link OpInfoGenerator}s that operate from a set of
 * {@link Discoverer}s.
 * 
 * @author Gabriel Selzer
 */
public abstract class DiscoveryBasedOpInfoGenerator implements OpInfoGenerator {

	protected final Logger log;
	protected final Collection<Discoverer> discoverers;

	public DiscoveryBasedOpInfoGenerator(Logger log, Discoverer... d) {
		this(log, Arrays.asList(d));
	}

	public DiscoveryBasedOpInfoGenerator(Logger log, Collection<Discoverer> d) {
		this.log = log;
		this.discoverers = d;
	}

	/**
	 * Declares the {@link Class} this {@link OpInfoGenerator} converts into
	 * {@link OpInfo}(s)
	 * 
	 * @return the {@link Class}
	 */
	protected abstract Class<?> implClass();

	/**
	 * This method should <b>always</b> return a non-{@code null} {@link List} of
	 * {@link OpInfo}s. If {@code c} has no elements that can be used to generate
	 * an {@code OpInfo}, this method should return
	 * {@link Collections#emptyList()}
	 * 
	 * @param c the {@link Class} to parse
	 * @return a {@link List} of {@link OpInfo}s.
	 */
	protected abstract List<OpInfo> processClass(Class<?> c);

	/**
	 * Calls {@link #processClass(Class)} for each {@link Class} implementing the
	 * class declared by {@link #implClass()}
	 * 
	 */
	@Override
	public List<OpInfo> generateInfos() {
		List<OpInfo> infos = discoverers.stream() //
			.flatMap(d -> d.implsOfType(implClass()).stream()) //
			.map(cls -> processClass(cls)) //
			.flatMap(list -> list.stream()) //
			.collect(Collectors.toList());
		return infos;
	}

}
