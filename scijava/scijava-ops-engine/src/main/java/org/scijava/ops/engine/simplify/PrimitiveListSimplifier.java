package org.scijava.ops.engine.simplify;

import java.util.List;
import java.util.function.Function;

import org.scijava.ops.api.Op;
import org.scijava.plugin.Plugin;

/**
 * Simplifies {@link List}s of types extending {@link Number}.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = Op.class, name = "simplify")
public class PrimitiveListSimplifier<T extends Number> implements Function<List<T>, List<Number>>{

	@Override
	/*
	* @param t the input List
	* @return a List whose elements have been simplified
	*/
	public List<Number> apply(List<T> t) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Number> numberList = (List) t;
		return numberList;
	}

}

