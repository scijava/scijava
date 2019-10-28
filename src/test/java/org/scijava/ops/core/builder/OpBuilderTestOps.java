/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.core.builder;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Inplaces;
import org.scijava.ops.function.Producer;
import org.scijava.plugin.Plugin;

/**
 * Helper ops for {@link OpBuilderTest}.
 *
 * @author Curtis Rueden
 */
@Plugin(type = OpCollection.class)
public class OpBuilderTestOps {

	/*
	 * -- FUNCTIONS --
	 * 
	 * The general procedure for these Ops: Given a set of inputs in1, in2, ...,
	 * inN, The output will be sum(in1, in2, ..., inN).
	 */
	
	// ARITY 0
	@OpField(names = "test.addDoubles")
	public final Producer<Double> addDoubles0 = () -> 0.;
	
	// ARITY 1
	@OpField(names = "test.addDoubles")
	public final Function<Double, Double> addDoubles1 = (input) -> input;
	
	// ARITY 2
	@OpField(names = "test.addDoubles")
	public final BiFunction<Double, Double, Double> addDoubles2 = (input1, input2) -> input1 + input2;
	
	// ARITY 3
	@OpField(names = "test.addDoubles")
	public final Functions.Arity3<Double, Double, Double, Double> addDoubles3 = (input1, input2, input3) -> input1 + input2 + input3;
	
	// ARITY 4
	@OpField(names = "test.addDoubles")
	public final Functions.Arity4<Double, Double, Double, Double, Double> addDoubles4 = (input1, input2, input3, input4) -> input1 + input2 + input3 + input4;
	
	// ARITY 5
	@OpField(names = "test.addDoubles")
	public final Functions.Arity5<Double, Double, Double, Double, Double, Double> addDoubles5 = (input1, input2, input3, input4, input5) -> input1 + input2 + input3 + input4 + input5;
	

	/*
	 * -- INPLACES --
	 * 
	 * The general procedure for these Ops: Given a set of inputs in1, in2, ... ,
	 * io, ..., inN, the output will be io = in1 * in2 * ... * io * ... * inN.
	 * N.B. We do this in arrays since the doubles themselves are immutable. 
	 */
	// ARITY 1
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity1<double[]> powDoubles1_1 = (io) -> {
			for(int i = 0; i < io.length; i++) {
			}
		};
		
	// ARITY 2
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity2_1<double[], double[]> powDoubles2_1 = (io, in2) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
			}
		};
		
	// ARITY 2
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity2_2<double[], double[]> powDoubles2_2 = (in1, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
			}
		};
		
	// ARITY 3
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity3_1<double[], double[], double[]> powDoubles3_1 = (io, in2, in3) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
			}
		};
		
	// ARITY 3
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity3_2<double[], double[], double[]> powDoubles3_2 = (in1, io, in3) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
			}
		};
		
	// ARITY 3
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity3_3<double[], double[], double[]> powDoubles3_3 = (in1, in2, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
			}
		};
		
	// ARITY 4
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity4_1<double[], double[], double[], double[]> powDoubles4_1 = (io, in2, in3, in4) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
			}
		};
		
	// ARITY 4
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity4_2<double[], double[], double[], double[]> powDoubles4_2 = (in1, io, in3, in4) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
			}
		};
		
	// ARITY 4
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity4_3<double[], double[], double[], double[]> powDoubles4_3 = (in1, in2, io, in4) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
			}
		};
		
	// ARITY 4
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity4_4<double[], double[], double[], double[]> powDoubles4_4 = (in1, in2, in3, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
			}
		};
		
	// ARITY 5
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity5_1<double[], double[], double[], double[], double[]> powDoubles5_1 = (io, in2, in3, in4, in5) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
			}
		};
		
	// ARITY 5
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity5_2<double[], double[], double[], double[], double[]> powDoubles5_2 = (in1, io, in3, in4, in5) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
			}
		};
		
	// ARITY 5
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity5_3<double[], double[], double[], double[], double[]> powDoubles5_3 = (in1, in2, io, in4, in5) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
			}
		};
		
	// ARITY 5
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity5_4<double[], double[], double[], double[], double[]> powDoubles5_4 = (in1, in2, in3, io, in5) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
			}
		};
		
	// ARITY 5
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity5_5<double[], double[], double[], double[], double[]> powDoubles5_5 = (in1, in2, in3, in4, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
			}
		};
		

	/*
	 * -- COMPUTERS --
	 * 
	 * The general procedure: given a set of inputs in1, in2, ... , inN, the output
	 * is given as in1 * in2 * ... * inN. N.B. we use arrays here since the doubles
	 * themselves are immutable
	 */

	// ARITY 0
	@OpField(names = "test.addArrays")
	public final Computers.Arity0<double[]> mulStrings0 = (output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
		}
	};

	// ARITY 1
	@OpField(names = "test.addArrays")
	public final Computers.Arity1<double[], double[]> mulStrings1 = (input, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input[i];
		}
	};

	// ARITY 2
	@OpField(names = "test.addArrays")
	public final Computers.Arity2<double[], double[], double[]> mulStrings2 = (input1, input2, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
		}
	};

	// ARITY 3
	@OpField(names = "test.addArrays")
	public final Computers.Arity3<double[], double[], double[], double[]> mulStrings3 = (input1, input2, input3, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
		}
	};

	// ARITY 4
	@OpField(names = "test.addArrays")
	public final Computers.Arity4<double[], double[], double[], double[], double[]> mulStrings4 = (input1, input2, input3, input4, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
		}
	};

	// ARITY 5
	@OpField(names = "test.addArrays")
	public final Computers.Arity5<double[], double[], double[], double[], double[], double[]> mulStrings5 = (input1, input2, input3, input4, input5, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
		}
	};

}
