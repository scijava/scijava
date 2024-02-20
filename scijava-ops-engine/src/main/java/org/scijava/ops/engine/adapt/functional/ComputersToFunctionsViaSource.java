/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava developers.
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
import org.scijava.ops.engine.BaseOpHints.Adaptation;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.priority.Priority;

/**
 * Collection of adaptation Ops to convert {@link Computers} into
 * {@link Functions} with the use of a {@link Producer} that creates the output
 * using the first input as a model.
 *
 * @author Gabriel Selzer
 */
public class ComputersToFunctionsViaSource {

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer0ToFunction0ViaSource<O> implements
		Function<Computers.Arity0<O>, Producer<O>>, Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Producer<O> apply(Computers.Arity0<O> computer) {
			return () -> {
				O out = creator.create();
				computer.compute(out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer1ToFunction1ViaSource<I, O> implements
		Function<Computers.Arity1<I, O>, Function<I, O>>, Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Function<I, O> apply(Computers.Arity1<I, O> computer) {
			return (in) -> {
				O out = creator.create();
				computer.compute(in, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer2ToFunction2ViaSource<I1, I2, O> implements
		Function<Computers.Arity2<I1, I2, O>, BiFunction<I1, I2, O>>, Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public BiFunction<I1, I2, O> apply(Computers.Arity2<I1, I2, O> computer) {
			return (in1, in2) -> {
				O out = creator.create();
				computer.compute(in1, in2, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer3ToFunction3ViaSource<I1, I2, I3, O> implements
		Function<Computers.Arity3<I1, I2, I3, O>, Functions.Arity3<I1, I2, I3, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity3<I1, I2, I3, O> apply(
			Computers.Arity3<I1, I2, I3, O> computer)
		{
			return (in1, in2, in3) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer4ToFunction4ViaSource<I1, I2, I3, I4, O>
		implements
		Function<Computers.Arity4<I1, I2, I3, I4, O>, Functions.Arity4<I1, I2, I3, I4, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity4<I1, I2, I3, I4, O> apply(
			Computers.Arity4<I1, I2, I3, I4, O> computer)
		{
			return (in1, in2, in3, in4) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer5ToFunction5ViaSource<I1, I2, I3, I4, I5, O>
		implements
		Function<Computers.Arity5<I1, I2, I3, I4, I5, O>, Functions.Arity5<I1, I2, I3, I4, I5, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity5<I1, I2, I3, I4, I5, O> apply(
			Computers.Arity5<I1, I2, I3, I4, I5, O> computer)
		{
			return (in1, in2, in3, in4, in5) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer6ToFunction6ViaSource<I1, I2, I3, I4, I5, I6, O>
		implements
		Function<Computers.Arity6<I1, I2, I3, I4, I5, I6, O>, Functions.Arity6<I1, I2, I3, I4, I5, I6, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity6<I1, I2, I3, I4, I5, I6, O> apply(
			Computers.Arity6<I1, I2, I3, I4, I5, I6, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer7ToFunction7ViaSource<I1, I2, I3, I4, I5, I6, I7, O>
		implements
		Function<Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O>, Functions.Arity7<I1, I2, I3, I4, I5, I6, I7, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity7<I1, I2, I3, I4, I5, I6, I7, O> apply(
			Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer8ToFunction8ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, O>
		implements
		Function<Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>, Functions.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O> apply(
			Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer9ToFunction9ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>
		implements
		Function<Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>, Functions.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> apply(
			Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer10ToFunction10ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>
		implements
		Function<Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>, Functions.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> apply(
			Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10,
					out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer11ToFunction11ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>
		implements
		Function<Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>, Functions.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public Functions.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>
			apply(
				Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10,
					in11, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer12ToFunction12ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>
		implements
		Function<Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>, Functions.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public
			Functions.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>
			apply(
				Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11,
				in12) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10,
					in11, in12, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer13ToFunction13ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>
		implements
		Function<Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>, Functions.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public
			Functions.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>
			apply(
				Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12,
				in13) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10,
					in11, in12, in13, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer14ToFunction14ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>
		implements
		Function<Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>, Functions.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public
			Functions.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>
			apply(
				Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12,
				in13, in14) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10,
					in11, in12, in13, in14, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer15ToFunction15ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>
		implements
		Function<Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>, Functions.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public
			Functions.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>
			apply(
				Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12,
				in13, in14, in15) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10,
					in11, in12, in13, in14, in15, out);
				return out;
			};
		}

	}

	@OpClass(names = "engine.adapt", priority = Priority.LOW)
	public static class Computer16ToFunction16ViaSource<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>
		implements
		Function<Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>, Functions.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>>,
		Op
	{

		@OpDependency(name = "engine.create", hints = { Adaptation.FORBIDDEN })
		Producer<O> creator;

		/**
		 * @param computer the computer to adapt
		 * @return a Function adaptation of computer
		 */
		@Override
		public
			Functions.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>
			apply(
				Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> computer)
		{
			return (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12,
				in13, in14, in15, in16) -> {
				O out = creator.create();
				computer.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10,
					in11, in12, in13, in14, in15, in16, out);
				return out;
			};
		}

	}

}
