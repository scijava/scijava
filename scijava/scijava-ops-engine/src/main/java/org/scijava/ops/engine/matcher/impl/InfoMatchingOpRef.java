package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRef;
import org.scijava.types.Nil;
import org.scijava.types.Types;
import org.scijava.types.inference.GenericAssignability;


public class InfoMatchingOpRef implements OpRef {

	/** Name of the op, or null for any name. */
	private final String name;

	/** Type which the op must match. */
	private final Type type;

	/** The op's output parameter types, or null for no constraints. */
	private final Type outType;

	/** Arguments to be passed to the op. */
	private final Type[] args;

	/**
	 * Mapping of TypeVariables of the {@link OpInfo} to the {@link Type}s of
	 * {@code specialType}
	 */
	private final Map<TypeVariable<?>, Type> map = new HashMap<>();

	public InfoMatchingOpRef(OpInfo info, Nil<?> specialType) {
		this.name = info.names().get(0);
		Type from = specialType.getType();
		Type to = info.opType();
		this.type = Types.getExactSuperType(to, Types.raw(from));
		if (this.type instanceof ParameterizedType) {
			if (!GenericAssignability.checkGenericAssignability(from,
				(ParameterizedType) this.type, this.map, true))
				throw new IllegalArgumentException();
		}
		else {
			if (!Types.isAssignable(from, this.type, this.map))
				throw new IllegalArgumentException();
		}
		args = info.inputs().stream().map(m -> Types
			.substituteTypeVariables(m.getType(), this.map)).toArray(Type[]::new);
		outType = Types.substituteTypeVariables(info.output().getType(),
			this.map);
		
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public Type getOutType() {
		return this.outType;
	}

	@Override
	public Type[] getArgs() {
		return this.args;
	}

	@Override
	public String getLabel() {
		final StringBuilder sb = new StringBuilder();
		OpRef.append(sb, name);
		if (type != null) {
			OpRef.append(sb, Types.name(type));
		}
		return sb.toString();
	}

	@Override
	public boolean typesMatch(Type opType) {
		return typesMatch(opType, new HashMap<>());
	}

	@Override
	public boolean typesMatch(Type opType,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		if (type == null) return true;
		if (type instanceof ParameterizedType) {
			if (!GenericAssignability.checkGenericAssignability(opType,
				(ParameterizedType) type, typeVarAssigns, true))
			{
				return false;
			}
		}
		else {
			if (!Types.isAssignable(opType, type)) {
				return false;
			}
		}
		return true;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return refString();
	}

	@Override
	public boolean equals(final Object obj) {
		return refEquals(obj);
	}

	@Override
	public int hashCode() {
		return refHashCode();
	}

}
