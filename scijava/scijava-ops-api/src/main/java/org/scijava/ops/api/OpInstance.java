
package org.scijava.ops.api;

import java.lang.reflect.Type;
import java.util.Objects;

import org.scijava.types.GenericTyped;

/**
 * An instance of an {@link OpInfo}. They can be constructed directly, but are
 * easily generated from {@link InfoTree}s.
 * <p>
 * Each {@link OpInstance} has an Op and its corresponding {@link OpInfo}.
 * </p>
 * 
 * @author Gabriel Selzer
 */
public class OpInstance<T> implements GenericTyped {

	private final T op;
	private final InfoTree info;
	private final Type reifiedType;

	public OpInstance(final T op, final InfoTree backingInfo,
		final Type reifiedType)
	{
		this.op = op;
		this.info = backingInfo;
		this.reifiedType = reifiedType;
	}

	public static <T> OpInstance<T> of(T op, InfoTree backingInfo,
		final Type reifiedType)
	{
		return new OpInstance<>(op, backingInfo, reifiedType);
	}

	public T op() {
		return op;
	}

	public InfoTree infoTree() {
		return info;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof OpInstance)) return false;
		OpInstance<?> thatInstance = (OpInstance<?>) that;
		boolean infosEqual = infoTree().equals(thatInstance.infoTree());
		boolean objectsEqual = op().equals(thatInstance.op());
		boolean typesEqual = getType().equals(thatInstance.getType());
		return infosEqual && objectsEqual && typesEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(infoTree(), op(), getType());
	}

	@Override
	public Type getType() {
		return reifiedType;
	}

}
