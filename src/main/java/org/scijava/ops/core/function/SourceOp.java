package org.scijava.ops.core.function;

import java.lang.reflect.Type;

import org.scijava.ops.core.GenericOp;
import org.scijava.ops.matcher.OpInfo;

/**
 * GenericFunctions are wrapped functions that know their generic typing at
 * runtime (because whoever wrapped the function knew it at the time of
 * wrapping).
 * 
 * TODO: can we make these classes protected/private? Is there a place to move
 * SourceOp that makes sense that would allow protected/private?
 * 
 * @author Gabriel Selzer
 */
public final class SourceOp<T> extends GenericOp implements Source<T>{
	private Source<T> c;
	private Type type;

	public SourceOp(Source<T> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public T create() {
		return c.create();
	}
}
