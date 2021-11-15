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

package org.scijava.ops.engine.adapt.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Producer;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpDependency;

/**
 * Collection of adaptation Ops to convert {@link Computers} into
 * {@link Functions} with the use of a {@link Computer} that copies the output
 * of the function into the preallocated argument.
 * 
 * @author Gabriel Selzer
 */
public class FunctionsToComputers {

	@OpClass(names = "adapt")
	public static class Function0ToComputer0<O> implements Function<Producer<O>, Computers.Arity0<O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity0<O> apply(Producer<O> function) {
			return (out) -> {
				O temp = function.create();
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function1ToComputer1<I, O> implements Function<Function<I, O>, Computers.Arity1<I, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity1<I, O> apply(Function<I, O> function) {
			return (in, out) -> {
				O temp = function.apply(in);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function2ToComputer2<I1, I2, O> implements Function<BiFunction<I1, I2, O>, Computers.Arity2<I1, I2, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity2<I1, I2, O> apply(BiFunction<I1, I2, O> function) {
			return (in1, in2, out) -> {
				O temp = function.apply(in1, in2);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function3ToComputer3<I1, I2, I3, O> implements Function<Functions.Arity3<I1, I2, I3, O>, Computers.Arity3<I1, I2, I3, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity3<I1, I2, I3, O> apply(Functions.Arity3<I1, I2, I3, O> function) {
			return (in1, in2, in3, out) -> {
				O temp = function.apply(in1, in2, in3);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function4ToComputer4<I1, I2, I3, I4, O> implements Function<Functions.Arity4<I1, I2, I3, I4, O>, Computers.Arity4<I1, I2, I3, I4, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity4<I1, I2, I3, I4, O> apply(Functions.Arity4<I1, I2, I3, I4, O> function) {
			return (in1, in2, in3, in4, out) -> {
				O temp = function.apply(in1, in2, in3, in4);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function5ToComputer5<I1, I2, I3, I4, I5, O> implements Function<Functions.Arity5<I1, I2, I3, I4, I5, O>, Computers.Arity5<I1, I2, I3, I4, I5, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity5<I1, I2, I3, I4, I5, O> apply(Functions.Arity5<I1, I2, I3, I4, I5, O> function) {
			return (in1, in2, in3, in4, in5, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function6ToComputer6<I1, I2, I3, I4, I5, I6, O> implements Function<Functions.Arity6<I1, I2, I3, I4, I5, I6, O>, Computers.Arity6<I1, I2, I3, I4, I5, I6, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity6<I1, I2, I3, I4, I5, I6, O> apply(Functions.Arity6<I1, I2, I3, I4, I5, I6, O> function) {
			return (in1, in2, in3, in4, in5, in6, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function7ToComputer7<I1, I2, I3, I4, I5, I6, I7, O> implements Function<Functions.Arity7<I1, I2, I3, I4, I5, I6, I7, O>, Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O> apply(Functions.Arity7<I1, I2, I3, I4, I5, I6, I7, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function8ToComputer8<I1, I2, I3, I4, I5, I6, I7, I8, O> implements Function<Functions.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>, Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O> apply(Functions.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function9ToComputer9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> implements Function<Functions.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>, Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> apply(Functions.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function10ToComputer10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> implements Function<Functions.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>, Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> apply(Functions.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function11ToComputer11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> implements Function<Functions.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>, Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> apply(Functions.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function12ToComputer12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> implements Function<Functions.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>, Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> apply(Functions.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function13ToComputer13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> implements Function<Functions.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>, Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> apply(Functions.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function14ToComputer14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> implements Function<Functions.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>, Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> apply(Functions.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function15ToComputer15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> implements Function<Functions.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>, Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> apply(Functions.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15);
				copyOp.compute(temp, out);
			};
		}

	}

	@OpClass(names = "adapt")
	public static class Function16ToComputer16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> implements Function<Functions.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>, Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>>, Op {

		@OpDependency(name = "copy", adaptable = false)
		Computers.Arity1<O, O> copyOp;

		/**
		 * @param function the function to adapt
		 * @return an adaptation of function
		 */
		@Override
		public Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> apply(Functions.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> function) {
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16, out) -> {
				O temp = function.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16);
				copyOp.compute(temp, out);
			};
		}

	}

}
