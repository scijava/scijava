package org.scijava.ops.engine.struct;

import java.lang.reflect.Field;

public class FieldInstance {

	private final Field field;
	private final Object instance;

	public FieldInstance(Field field, Object instance) {
		this.field = field;
		this.instance = instance;
	}

	public Field field() {
		return field;
	}

	public Object instance() {
		return instance;
	}
}
