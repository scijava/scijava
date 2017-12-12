package org.scijava.ops.examples;

import org.scijava.ops.ComputerOp;
import org.scijava.ops.OutputAware;
import org.scijava.util.DoubleArray;

/** Unary computer/function hybrid. */
public class Square implements ComputerOp<DoubleArray, DoubleArray>, OutputAware<DoubleArray, DoubleArray> {
	
	@Override
	public void accept(DoubleArray in, DoubleArray out) {
		for (int i=0; i< in.size(); i++) {
			double v = in.getValue(i);
			out.set(i, v * v);
		}
	}

	@Override
	public DoubleArray createOutput(DoubleArray in) {
		return new DoubleArray(in.size());
	}
}

// use function as map
// accept(I in, Consumer<O> out):
//    out.accept(f(in))

// use function as computer  --------- 
// compute(in, out):
//    o = f(in)
//    copy(o -> out)

// use computer as function  --------- needs OutputAware
// out = f(in):
//    o = create(in)
//    compute(in, o)
//    return o

// use inplace as function  --------- 
// out = f(in):
//    o = create(in)
//    copy(in -> o)
//    mutate(o)

// use inplace as computer  --------- 
// compute(in, out):
//    copy(in -> out)
//    mutate(out)

////////////////////
// Possibly problematic

// use function as inplace  --------- only allowed if Function<T, T> and structures match
// mutate(arg):
//    o = f(arg)
//    copy(o -> arg)

// use computer as inplace  --------- 
// mutate(arg):
//    o = create(arg)
//    compute(arg, o)
//    copy(o -> arg)
