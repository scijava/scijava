package org.scijava.ops.engine.simplify;

import java.util.List;
import java.util.function.Function;

import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

/**
 * Simplifies {@link List}s of types extending {@link Number}.
 * 
 * @author Gabriel Selzer
 */
@OpClass(names = "simplify")
public class PrimitiveListSimplifier<T extends Number> implements Function<List<T>, List<Number>>, Op {

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

