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
	
	// ARITY 6
	@OpField(names = "test.addDoubles")
	public final Functions.Arity6<Double, Double, Double, Double, Double, Double, Double> addDoubles6 = (input1, input2, input3, input4, input5, input6) -> input1 + input2 + input3 + input4 + input5 + input6;
	
	// ARITY 7
	@OpField(names = "test.addDoubles")
	public final Functions.Arity7<Double, Double, Double, Double, Double, Double, Double, Double> addDoubles7 = (input1, input2, input3, input4, input5, input6, input7) -> input1 + input2 + input3 + input4 + input5 + input6 + input7;
	
	// ARITY 8
	@OpField(names = "test.addDoubles")
	public final Functions.Arity8<Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles8 = (input1, input2, input3, input4, input5, input6, input7, input8) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8;
	
	// ARITY 9
	@OpField(names = "test.addDoubles")
	public final Functions.Arity9<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles9 = (input1, input2, input3, input4, input5, input6, input7, input8, input9) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8 + input9;
	
	// ARITY 10
	@OpField(names = "test.addDoubles")
	public final Functions.Arity10<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles10 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8 + input9 + input10;
	
	// ARITY 11
	@OpField(names = "test.addDoubles")
	public final Functions.Arity11<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles11 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8 + input9 + input10 + input11;
	
	// ARITY 12
	@OpField(names = "test.addDoubles")
	public final Functions.Arity12<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles12 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8 + input9 + input10 + input11 + input12;
	
	// ARITY 13
	@OpField(names = "test.addDoubles")
	public final Functions.Arity13<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles13 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8 + input9 + input10 + input11 + input12 + input13;
	
	// ARITY 14
	@OpField(names = "test.addDoubles")
	public final Functions.Arity14<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles14 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8 + input9 + input10 + input11 + input12 + input13 + input14;
	
	// ARITY 15
	@OpField(names = "test.addDoubles")
	public final Functions.Arity15<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles15 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8 + input9 + input10 + input11 + input12 + input13 + input14 + input15;
	
	// ARITY 16
	@OpField(names = "test.addDoubles")
	public final Functions.Arity16<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> addDoubles16 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16) -> input1 + input2 + input3 + input4 + input5 + input6 + input7 + input8 + input9 + input10 + input11 + input12 + input13 + input14 + input15 + input16;
	

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
		
	// ARITY 6
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity6_1<double[], double[], double[], double[], double[], double[]> powDoubles6_1 = (io, in2, in3, in4, in5, in6) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
			}
		};
		
	// ARITY 6
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity6_2<double[], double[], double[], double[], double[], double[]> powDoubles6_2 = (in1, io, in3, in4, in5, in6) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
			}
		};
		
	// ARITY 6
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity6_3<double[], double[], double[], double[], double[], double[]> powDoubles6_3 = (in1, in2, io, in4, in5, in6) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
			}
		};
		
	// ARITY 6
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity6_4<double[], double[], double[], double[], double[], double[]> powDoubles6_4 = (in1, in2, in3, io, in5, in6) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
			}
		};
		
	// ARITY 6
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity6_5<double[], double[], double[], double[], double[], double[]> powDoubles6_5 = (in1, in2, in3, in4, io, in6) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
			}
		};
		
	// ARITY 6
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity6_6<double[], double[], double[], double[], double[], double[]> powDoubles6_6 = (in1, in2, in3, in4, in5, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
			}
		};
		
	// ARITY 7
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity7_1<double[], double[], double[], double[], double[], double[], double[]> powDoubles7_1 = (io, in2, in3, in4, in5, in6, in7) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
			}
		};
		
	// ARITY 7
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity7_2<double[], double[], double[], double[], double[], double[], double[]> powDoubles7_2 = (in1, io, in3, in4, in5, in6, in7) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
			}
		};
		
	// ARITY 7
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity7_3<double[], double[], double[], double[], double[], double[], double[]> powDoubles7_3 = (in1, in2, io, in4, in5, in6, in7) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
			}
		};
		
	// ARITY 7
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity7_4<double[], double[], double[], double[], double[], double[], double[]> powDoubles7_4 = (in1, in2, in3, io, in5, in6, in7) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
			}
		};
		
	// ARITY 7
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity7_5<double[], double[], double[], double[], double[], double[], double[]> powDoubles7_5 = (in1, in2, in3, in4, io, in6, in7) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
			}
		};
		
	// ARITY 7
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity7_6<double[], double[], double[], double[], double[], double[], double[]> powDoubles7_6 = (in1, in2, in3, in4, in5, io, in7) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
			}
		};
		
	// ARITY 7
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity7_7<double[], double[], double[], double[], double[], double[], double[]> powDoubles7_7 = (in1, in2, in3, in4, in5, in6, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
			}
		};
		
	// ARITY 8
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity8_1<double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles8_1 = (io, in2, in3, in4, in5, in6, in7, in8) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
			}
		};
		
	// ARITY 8
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity8_2<double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles8_2 = (in1, io, in3, in4, in5, in6, in7, in8) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
			}
		};
		
	// ARITY 8
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity8_3<double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles8_3 = (in1, in2, io, in4, in5, in6, in7, in8) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
			}
		};
		
	// ARITY 8
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity8_4<double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles8_4 = (in1, in2, in3, io, in5, in6, in7, in8) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
			}
		};
		
	// ARITY 8
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity8_5<double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles8_5 = (in1, in2, in3, in4, io, in6, in7, in8) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
			}
		};
		
	// ARITY 8
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity8_6<double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles8_6 = (in1, in2, in3, in4, in5, io, in7, in8) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
			}
		};
		
	// ARITY 8
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity8_7<double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles8_7 = (in1, in2, in3, in4, in5, in6, io, in8) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
			}
		};
		
	// ARITY 8
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity8_8<double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles8_8 = (in1, in2, in3, in4, in5, in6, in7, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_1<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_1 = (io, in2, in3, in4, in5, in6, in7, in8, in9) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_2<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_2 = (in1, io, in3, in4, in5, in6, in7, in8, in9) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_3<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_3 = (in1, in2, io, in4, in5, in6, in7, in8, in9) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_4<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_4 = (in1, in2, in3, io, in5, in6, in7, in8, in9) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_5<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_5 = (in1, in2, in3, in4, io, in6, in7, in8, in9) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_6<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_6 = (in1, in2, in3, in4, in5, io, in7, in8, in9) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_7<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_7 = (in1, in2, in3, in4, in5, in6, io, in8, in9) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_8 = (in1, in2, in3, in4, in5, in6, in7, io, in9) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 9
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity9_9<double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles9_9 = (in1, in2, in3, in4, in5, in6, in7, in8, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_1 = (io, in2, in3, in4, in5, in6, in7, in8, in9, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_2 = (in1, io, in3, in4, in5, in6, in7, in8, in9, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_3 = (in1, in2, io, in4, in5, in6, in7, in8, in9, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_4 = (in1, in2, in3, io, in5, in6, in7, in8, in9, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_5 = (in1, in2, in3, in4, io, in6, in7, in8, in9, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_6 = (in1, in2, in3, in4, in5, io, in7, in8, in9, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_7 = (in1, in2, in3, in4, in5, in6, io, in8, in9, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_8 = (in1, in2, in3, in4, in5, in6, in7, io, in9, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_9 = (in1, in2, in3, in4, in5, in6, in7, in8, io, in10) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 10
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity10_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles10_10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_1 = (io, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_2 = (in1, io, in3, in4, in5, in6, in7, in8, in9, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_3 = (in1, in2, io, in4, in5, in6, in7, in8, in9, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_4 = (in1, in2, in3, io, in5, in6, in7, in8, in9, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_5 = (in1, in2, in3, in4, io, in6, in7, in8, in9, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_6 = (in1, in2, in3, in4, in5, io, in7, in8, in9, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_7 = (in1, in2, in3, in4, in5, in6, io, in8, in9, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_8 = (in1, in2, in3, in4, in5, in6, in7, io, in9, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_9 = (in1, in2, in3, in4, in5, in6, in7, in8, io, in10, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, io, in11) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 11
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity11_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles11_11 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_1 = (io, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_2 = (in1, io, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_3 = (in1, in2, io, in4, in5, in6, in7, in8, in9, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_4 = (in1, in2, in3, io, in5, in6, in7, in8, in9, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_5 = (in1, in2, in3, in4, io, in6, in7, in8, in9, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_6 = (in1, in2, in3, in4, in5, io, in7, in8, in9, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_7 = (in1, in2, in3, in4, in5, in6, io, in8, in9, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_8 = (in1, in2, in3, in4, in5, in6, in7, io, in9, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_9 = (in1, in2, in3, in4, in5, in6, in7, in8, io, in10, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, io, in11, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_11 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, io, in12) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 12
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity12_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles12_12 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_1 = (io, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_2 = (in1, io, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_3 = (in1, in2, io, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_4 = (in1, in2, in3, io, in5, in6, in7, in8, in9, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_5 = (in1, in2, in3, in4, io, in6, in7, in8, in9, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_6 = (in1, in2, in3, in4, in5, io, in7, in8, in9, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_7 = (in1, in2, in3, in4, in5, in6, io, in8, in9, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_8 = (in1, in2, in3, in4, in5, in6, in7, io, in9, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_9 = (in1, in2, in3, in4, in5, in6, in7, in8, io, in10, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, io, in11, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_11 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, io, in12, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_12 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, io, in13) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 13
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity13_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles13_13 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_1 = (io, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_2 = (in1, io, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_3 = (in1, in2, io, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_4 = (in1, in2, in3, io, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_5 = (in1, in2, in3, in4, io, in6, in7, in8, in9, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_6 = (in1, in2, in3, in4, in5, io, in7, in8, in9, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_7 = (in1, in2, in3, in4, in5, in6, io, in8, in9, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_8 = (in1, in2, in3, in4, in5, in6, in7, io, in9, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_9 = (in1, in2, in3, in4, in5, in6, in7, in8, io, in10, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, io, in11, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_11 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, io, in12, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_12 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, io, in13, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_13 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, io, in14) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 14
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity14_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles14_14 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_1 = (io, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_2 = (in1, io, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_3 = (in1, in2, io, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_4 = (in1, in2, in3, io, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_5 = (in1, in2, in3, in4, io, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_6 = (in1, in2, in3, in4, in5, io, in7, in8, in9, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_7 = (in1, in2, in3, in4, in5, in6, io, in8, in9, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_8 = (in1, in2, in3, in4, in5, in6, in7, io, in9, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_9 = (in1, in2, in3, in4, in5, in6, in7, in8, io, in10, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, io, in11, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_11 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, io, in12, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_12 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, io, in13, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_13 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, io, in14, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_14 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, io, in15) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in15[i];
			}
		};
		
	// ARITY 15
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity15_15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles15_15 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_1 = (io, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_2 = (in1, io, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_3 = (in1, in2, io, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_4 = (in1, in2, in3, io, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_5 = (in1, in2, in3, in4, io, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_6 = (in1, in2, in3, in4, in5, io, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_7 = (in1, in2, in3, in4, in5, in6, io, in8, in9, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_8 = (in1, in2, in3, in4, in5, in6, in7, io, in9, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_9 = (in1, in2, in3, in4, in5, in6, in7, in8, io, in10, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, io, in11, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_11 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, io, in12, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_12 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, io, in13, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_13 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, io, in14, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_14 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, io, in15, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in15[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_15 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, io, in16) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in16[i];
			}
		};
		
	// ARITY 16
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity16_16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> powDoubles16_16 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, io) -> {
			for(int i = 0; i < io.length; i++) {
				io[i] *= in1[i];
				io[i] *= in2[i];
				io[i] *= in3[i];
				io[i] *= in4[i];
				io[i] *= in5[i];
				io[i] *= in6[i];
				io[i] *= in7[i];
				io[i] *= in8[i];
				io[i] *= in9[i];
				io[i] *= in10[i];
				io[i] *= in11[i];
				io[i] *= in12[i];
				io[i] *= in13[i];
				io[i] *= in14[i];
				io[i] *= in15[i];
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

	// ARITY 6
	@OpField(names = "test.addArrays")
	public final Computers.Arity6<double[], double[], double[], double[], double[], double[], double[]> mulStrings6 = (input1, input2, input3, input4, input5, input6, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
		}
	};

	// ARITY 7
	@OpField(names = "test.addArrays")
	public final Computers.Arity7<double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings7 = (input1, input2, input3, input4, input5, input6, input7, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
		}
	};

	// ARITY 8
	@OpField(names = "test.addArrays")
	public final Computers.Arity8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings8 = (input1, input2, input3, input4, input5, input6, input7, input8, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
		}
	};

	// ARITY 9
	@OpField(names = "test.addArrays")
	public final Computers.Arity9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings9 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
			output[i] += input9[i];
		}
	};

	// ARITY 10
	@OpField(names = "test.addArrays")
	public final Computers.Arity10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings10 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
			output[i] += input9[i];
			output[i] += input10[i];
		}
	};

	// ARITY 11
	@OpField(names = "test.addArrays")
	public final Computers.Arity11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings11 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
			output[i] += input9[i];
			output[i] += input10[i];
			output[i] += input11[i];
		}
	};

	// ARITY 12
	@OpField(names = "test.addArrays")
	public final Computers.Arity12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings12 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
			output[i] += input9[i];
			output[i] += input10[i];
			output[i] += input11[i];
			output[i] += input12[i];
		}
	};

	// ARITY 13
	@OpField(names = "test.addArrays")
	public final Computers.Arity13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings13 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
			output[i] += input9[i];
			output[i] += input10[i];
			output[i] += input11[i];
			output[i] += input12[i];
			output[i] += input13[i];
		}
	};

	// ARITY 14
	@OpField(names = "test.addArrays")
	public final Computers.Arity14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings14 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
			output[i] += input9[i];
			output[i] += input10[i];
			output[i] += input11[i];
			output[i] += input12[i];
			output[i] += input13[i];
			output[i] += input14[i];
		}
	};

	// ARITY 15
	@OpField(names = "test.addArrays")
	public final Computers.Arity15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings15 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
			output[i] += input9[i];
			output[i] += input10[i];
			output[i] += input11[i];
			output[i] += input12[i];
			output[i] += input13[i];
			output[i] += input14[i];
			output[i] += input15[i];
		}
	};

	// ARITY 16
	@OpField(names = "test.addArrays")
	public final Computers.Arity16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> mulStrings16 = (input1, input2, input3, input4, input5, input6, input7, input8, input9, input10, input11, input12, input13, input14, input15, input16, output) -> {
		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
			output[i] += input1[i];
			output[i] += input2[i];
			output[i] += input3[i];
			output[i] += input4[i];
			output[i] += input5[i];
			output[i] += input6[i];
			output[i] += input7[i];
			output[i] += input8[i];
			output[i] += input9[i];
			output[i] += input10[i];
			output[i] += input11[i];
			output[i] += input12[i];
			output[i] += input13[i];
			output[i] += input14[i];
			output[i] += input15[i];
			output[i] += input16[i];
		}
	};

}
