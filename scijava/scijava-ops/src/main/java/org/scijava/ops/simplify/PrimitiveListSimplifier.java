package org.scijava.ops.simplify;

import java.util.List;
import java.util.function.Function;

import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Simplifies {@link List}s of types extending {@link Number}.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = Op.class, name = "simplify")
@Parameter(key = "inList")
@Parameter(key = "simpleList", itemIO = ItemIO.OUTPUT)
public class PrimitiveListSimplifier<T extends Number> implements Function<List<T>, List<Number>>{

	@Override
	public List<Number> apply(List<T> t) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Number> numberList = (List) t;
		return numberList;
	}

}

