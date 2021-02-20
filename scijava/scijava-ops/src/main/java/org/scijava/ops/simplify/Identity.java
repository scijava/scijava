package org.scijava.ops.simplify;

import java.util.function.Function;

import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Unsimplifiable
@Plugin(type = Op.class, name = "simplify, focus")
@Parameter(key = "input")
@Parameter(key = "output")
public class Identity<T> implements Function<T, T> {

	public Identity() {
	}

	@Override
	public T apply(T t) {
		return t;
	}
}
