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

	// ARITY ZERO
	@OpField(names = "test.addDoubles")
	public final Producer<Double> addDoubles0 = () -> 0.;

	// ARITY 1
	@OpField(names = "test.addDoubles")
	public final Function<Double, Double> addDoubles1 = (in1) -> in1;

	// ARITY 2
	@OpField(names = "test.addDoubles")
	public final BiFunction<Double, Double, Double> addDoubles2 = (in1, in2) -> in1 + in2;

	// ARITY 3+ (CODE GENERATE THESE)
	@OpField(names = "test.addDoubles")
	public final Functions.Arity3<Double, Double, Double, Double> addDoubles3 = (in1, in2, in3) -> in1 + in2 + in3;

	/*
	 * -- INPLACES --
	 * 
	 * The general procedure for these Ops: Given a set of inputs in1, in2, ... ,
	 * io, ..., inN, the output will be io = in1 * in2 * ... * io * ... * inN.
	 * N.B. We do this in arrays since the doubles themselves are immutable. 
	 */

	// ARITY ONE
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity1<double[]> powDoubles1 = (io) -> {};

	// ARITY TWO
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity2_1<double[], double[]> powDoubles2_1 = (io,
			in2) -> {
				for(int i = 0; i < io.length; i++) {
					io[i] *= in2[i];
				}
			};

	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity2_2<double[], double[]> powDoubles2_2 = (in1,
			io) -> {
				for(int i = 0; i < io.length; i++) {
					io[i] *= in1[i];
				}
			};

	// ARITY THREES
	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity3_1<double[], double[], double[]> powDoubles3_1 = (io, in2,
			in3) -> {
				for(int i = 0; i < io.length; i++) {
					io[i] *= in2[i];
					io[i] *= in3[i];
				}
			};

	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity3_2<double[], double[], double[]> powDoubles3_2 = (in1, io,
			in3) -> {
				for(int i = 0; i < io.length; i++) {
					io[i] *= in1[i];
					io[i] *= in3[i];
				}
			};

	@OpField(names = "test.mulArrays")
	public final Inplaces.Arity3_3<double[], double[], double[]> powDoubles3_3 = (in1, in2,
			io) -> {
				for(int i = 0; i < io.length; i++) {
					io[i] *= in1[i];
					io[i] *= in2[i];
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
	public final Computers.Arity0<double[]> mulStrings0 = (out) -> {
		for (int i = 0; i < out.length; i++)
			out[i] = 0;
	};

	// ARITY 1
	@OpField(names = "test.addArrays")
	public final Computers.Arity1<double[], double[]> mulStrings1 = (in,
			out) -> {
				for(int i = 0; i < out.length; i++) {
					out[i] = in[i];
				}
			};

	// ARITY 2
	@OpField(names = "test.addArrays")
	public final Computers.Arity2<double[], double[], double[]> mulStrings2 = (in1, in2, 
			out) -> {
				for(int i = 0; i < out.length; i++) {
					out[i] = in1[i] + in2[i];
				}
			};

	// ARITY 3
	@OpField(names = "test.addArrays")
	public final Computers.Arity3<double[], double[], double[], double[]> mulStrings3 = (in1, in2, in3,
			out) -> {
				for(int i = 0; i < out.length; i++) {
					out[i] = in1[i] + in2[i] + in3[i];
				}
			};

	// TODO: delete

	@OpField(names = "test.castToInt")
	public final Computers.Arity1<double[], int[]> castToInt = (src, dest) -> {
		for (int i = 0; i < dest.length; i++)
			dest[i] = (int) src[i];
	};

	@OpField(names = "test.concat")
	public final Computers.Arity2<String[], Object[], String[]> concat = (s, o, out) -> {
		for (int i = 0; i < out.length; i++)
			out[i] = s[i] + o[i];
	};
}
