package org.scijava.ops;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

/**
 * 
 * @author G
 *
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

}
