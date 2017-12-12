package org.scijava.ops.examples;

import java.util.function.Function;

import org.scijava.ops.BiFunctionOp;
import org.scijava.param.Parameter;

@Parameter(key = "image")
@Parameter(key = "settings")
@Parameter(key = "result")
public class MyBiFunctionOp implements BiFunctionOp<String, Integer, Double>
{

	// FIXME THIS NEEDS TO WORK!!!!!!!!!!!!
//	@Op(type = Ops.Filter.Gauss.class)
	private Function<String, Double> doublifier;

	@Override
	public Double apply(String image, Integer settings) {
		Double d = doublifier.apply(image);
		Class<?> c = null;
		// TODO Auto-generated method stub
		return null;
	}
}
