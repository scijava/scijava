package org.scijava.ops;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Objects;

/**
 * An instance of an {@link OpInfo}
 * 
 * @author Gabriel Selzer
 */
public class OpInstance {

	private final Object op;
	private final OpInfo info;
	private final Map<TypeVariable<?>, Type> typeVarAssigns;

	public OpInstance(final Object op, final OpInfo backingInfo, final Map<TypeVariable<?>, Type> typeVarAssigns) {
		this.op = op;
		this.info = backingInfo;
		this.typeVarAssigns = typeVarAssigns;
	}

	public static OpInstance of(Object op, OpInfo backingInfo, Map<TypeVariable<?>, Type> typeVarAssigns) {
		return new OpInstance(op, backingInfo, typeVarAssigns);
	}

	public Object op() {
		return op;
	}

	public OpInfo info() {
		return info;
	}

	public Map<TypeVariable<?>, Type> typeVarAssigns() {
		return typeVarAssigns;
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof OpInstance)) return false;
		OpInstance thatInstance = (OpInstance) that;
		boolean infosEqual = info().equals(thatInstance.info());
		boolean objectsEqual = op().equals(thatInstance.op());
		boolean typeVarAssignsEqual = typeVarAssigns().equals(thatInstance.typeVarAssigns());
		return infosEqual && objectsEqual && typeVarAssignsEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(info(), op(), typeVarAssigns());
	}

}
