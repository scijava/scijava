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

/*
* This is autogenerated source code -- DO NOT EDIT. Instead, edit the
* corresponding template in templates/ and rerun bin/generate.groovy.
*/

package org.scijava.ops.adapt.functional;

import org.scijava.function.Computers;
import org.scijava.ops.api.OpField;
import org.scijava.ops.api.OpField;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpCollection;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class ComputerToFunctionAdaptTestOps {
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity1<double[], double[]> toFunc1 = (in, out) -> {
		for(int i = 0; i < in.length; i++) {
			out[i] = 0.0;
			out[i] += in[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity2<double[], double[], double[]> toFunc2 = (in1, in2, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity3<double[], double[], double[], double[]> toFunc3 = (in1, in2, in3, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity4<double[], double[], double[], double[], double[]> toFunc4 = (in1, in2, in3, in4, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity5<double[], double[], double[], double[], double[], double[]> toFunc5 = (in1, in2, in3, in4, in5, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity6<double[], double[], double[], double[], double[], double[], double[]> toFunc6 = (in1, in2, in3, in4, in5, in6, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity7<double[], double[], double[], double[], double[], double[], double[], double[]> toFunc7 = (in1, in2, in3, in4, in5, in6, in7, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc8 = (in1, in2, in3, in4, in5, in6, in7, in8, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc9 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
			out[i] += in9[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
			out[i] += in9[i];
			out[i] += in10[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc11 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
			out[i] += in9[i];
			out[i] += in10[i];
			out[i] += in11[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc12 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
			out[i] += in9[i];
			out[i] += in10[i];
			out[i] += in11[i];
			out[i] += in12[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc13 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
			out[i] += in9[i];
			out[i] += in10[i];
			out[i] += in11[i];
			out[i] += in12[i];
			out[i] += in13[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc14 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
			out[i] += in9[i];
			out[i] += in10[i];
			out[i] += in11[i];
			out[i] += in12[i];
			out[i] += in13[i];
			out[i] += in14[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc15 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
			out[i] += in9[i];
			out[i] += in10[i];
			out[i] += in11[i];
			out[i] += in12[i];
			out[i] += in13[i];
			out[i] += in14[i];
			out[i] += in15[i];
		}
	};
	
	@OpField(names = "test.CtF")
	public static final Computers.Arity16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> toFunc16 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16, out) -> {
		for(int i = 0; i < in1.length; i++) {
			out[i] = 0.0;
			out[i] += in1[i];
			out[i] += in2[i];
			out[i] += in3[i];
			out[i] += in4[i];
			out[i] += in5[i];
			out[i] += in6[i];
			out[i] += in7[i];
			out[i] += in8[i];
			out[i] += in9[i];
			out[i] += in10[i];
			out[i] += in11[i];
			out[i] += in12[i];
			out[i] += in13[i];
			out[i] += in14[i];
			out[i] += in15[i];
			out[i] += in16[i];
		}
	};
}
