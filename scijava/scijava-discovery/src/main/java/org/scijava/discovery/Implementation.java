package org.scijava.discovery;

import java.lang.reflect.Type;

/**
 * An implementation of a particular base {@link Class}
 * @author Gabriel Selzer
 *
 * @param <T> the base class {@link Type}
 */
public class Implementation<T> {

	private final Class<T> c;
	private final Class<T> implOf; 
	private final String name;

	public Implementation(final Class<T> c, final Class<T> implOf, final String name) {
		this.c = c;
		this.implOf = implOf;
		this.name = name;
	}

	public Class<T> implementation() {
		return c;
	}

	public Class<T> implementingClass() {
		return implOf;
	}

	public String name() {
		return name;
	}

}
