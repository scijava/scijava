
package org.scijava.ops.engine;

import java.lang.reflect.Type;
import java.util.Objects;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpInfo;
import org.scijava.types.GenericTyped;

/**
 * An instance of an {@link OpInfo}
 * 
 * @author Gabriel Selzer
 */
public class OpInstance<T> implements GenericTyped {

	private final T op;
	private final InfoChain info;
	private final Type reifiedType;

	public OpInstance(final T op, final InfoChain backingInfo,
		final Type reifiedType)
	{
		this.op = op;
		this.info = backingInfo;
		this.reifiedType = reifiedType;
	}

	public static <T> OpInstance<T> of(T op, InfoChain backingInfo,
		final Type reifiedType)
	{
		return new OpInstance<>(op, backingInfo, reifiedType);
	}

	public T op() {
		return op;
	}

	public InfoChain infoChain() {
		return info;
	}

	public Type type() {
		return reifiedType;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof OpInstance)) return false;
		OpInstance<?> thatInstance = (OpInstance<?>) that;
		boolean infosEqual = infoChain().equals(thatInstance.infoChain());
		boolean objectsEqual = op().equals(thatInstance.op());
		boolean typesEqual = type().equals(thatInstance.type());
		return infosEqual && objectsEqual && typesEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(infoChain(), op(), type());
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
