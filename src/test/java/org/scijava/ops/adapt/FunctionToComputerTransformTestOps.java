package org.scijava.ops.adapt;

import java.util.function.Function;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class FunctionToComputerTransformTestOps {
	
	@OpField(names = "test.FtC")
	@Parameter(key = "in")
	@Parameter(key = "out")
	public static final Function<double[], double[]> toFunc1 = (in) -> {
		double[] out = new double[in.length];
		for(int i = 0; i < in.length; i++) out[i] = in[i];
		return out;
	};

}
