package org.scijava.ops.core.function;

import java.lang.reflect.Type;

import org.scijava.ops.types.GenericTyped;

/**
 * A GenericSource is a wrapped {@link Source} that knows its generic typing at
 * runtime (because whoever wrapped the source knew its type when it was
 * wrapped).
 * 
 * @author Gabriel Selzer
 */
public class GenericSource<O> implements Source<O>, GenericTyped {

	private Source<O> c;
	private Type type;

	public GenericSource(Source<O> c, Type type) {
		this.c = c;
		this.type = type;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public O create() {
		return c.create();
	}
}
