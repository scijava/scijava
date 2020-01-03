package org.scijava.ops.adapt;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class ComputerToFunctionTransformTestOps {
	
	@OpField(names = "test.CtF")
	@Parameter(key = "in")
	@Parameter(key = "out")
	public static final Computers.Arity1<double[], double[]> toFunc1 = (in, out) -> {
		for(int i = 0; i < in.length; i++) out[i] = in[i];
	};

}
