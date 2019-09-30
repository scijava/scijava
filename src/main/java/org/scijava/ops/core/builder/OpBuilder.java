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

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers.Arity0;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Inplaces;
import org.scijava.ops.function.Producer;
import org.scijava.ops.types.Nil;
import org.scijava.ops.types.TypeService;
import org.scijava.util.Types;

/**
 * Convenience class for looking up and/or executing ops using a builder
 * pattern.
 * <p>
 * TODO: Examples
 * </p>
 *
 * @author Curtis Rueden
 */
public class OpBuilder {

	private final OpService ops;
	private final String opName;

	public OpBuilder(final OpService ops, final String opName) {
		this.ops = ops;
		this.opName = opName;
	}

	/** Specifies the op accepts no inputs&mdash;i.e., a nullary op. */
	public Arity0_OU input() {
		return new Arity0_OU();
	}

	/** Specifies 1 input by value. */
	public <I> Arity1_IV_OU<I> input(I in) {
		return new Arity1_IV_OU<>(in);
	}

	/** Specifies 1 input by raw type. */
	public <I> Arity1_IT_OU<I> inType(Class<I> inType) {
		return inType(Nil.of(inType));
	}

	/** Specifies 1 input by generic type. */
	public <I> Arity1_IT_OU<I> inType(Nil<I> inType) {
		return new Arity1_IT_OU<>(inType);
	}

	/** Specifies 2 inputs by value. */
	public <I1, I2> Arity2_IV_OU<I1, I2> input(I1 in1, I2 in2) {
		return new Arity2_IV_OU<>(in1, in2);
	}

	/** Specifies 2 inputs by raw type. */
	public <I1, I2> Arity2_IT_OU<I1, I2> inType(Class<I1> in1Type, Class<I2> in2Type) {
		return inType(Nil.of(in1Type), Nil.of(in2Type));
	}

	/** Specifies 2 inputs by generic type. */
	public <I1, I2> Arity2_IT_OU<I1, I2> inType(Nil<I1> in1Type, Nil<I2> in2Type) {
		return new Arity2_IT_OU<>(in1Type, in2Type);
		}

	// -- Helper methods --

	@SuppressWarnings({"unchecked"})
	private <T> Nil<T> type(final WeakReference<T> obj) {
		return (Nil<T>) Nil.of(ops.context().service(TypeService.class).reify(obj.get()));
	}

	// -- Helper classes --

	/**
	 * Builder with arity 0, output unspecified.
	 *
	 * @author Curtis Rueden
	 */
	public final class Arity0_OU {

		public <O> Arity0_OV<O> output(final O out) {
			return new Arity0_OV<>(out);
		}

		public <O> Arity0_OT<O> outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		public <O> Arity0_OT<O> outType(final Nil<O> outType) {
			return new Arity0_OT<>(outType);
		}

		public Producer<?> producer() {
			final Nil<Producer<Object>> specialType = new Nil<Producer<Object>>() {
				@Override
				public Type getType() {
					return Types.parameterize(Producer.class, new Type[] {Object.class});
				}
			};
			return ops.findOp(opName, specialType, new Nil<?>[0], Nil.of(Object.class));
		}

		public Object create() {
			return producer().create();
		}
	}

	/**
	 * Builder with arity 0, output type given.
	 *
	 * @author Curtis Rueden
	 * @param <O> The type of the output.
	 */
	public final class Arity0_OT<O> {

		private final Nil<O> outType;

		public Arity0_OT(final Nil<O> outType) {
			this.outType = outType;
		}

		public Producer<O> producer() {
			final Nil<Producer<O>> specialType = new Nil<Producer<O>>() {
				@Override
				public Type getType() {
					return Types.parameterize(Producer.class, new Type[] {outType.getType()});
				}
			};
			return ops.findOp(opName, specialType, new Nil<?>[0], outType);
		}

		public Computers.Arity0<O> computer() {
			return Computers.match(ops, opName, outType);
		}
		
		public Inplaces.Arity1<O> inplace() {
			return Inplaces.match(ops, opName, outType);
		}

		public O create() {
			return producer().create();
		}
		
	}

	/**
	 * Builder with arity 0, output value given.
	 *
	 * @author Curtis Rueden
	 * @param <O> The type of the output.
	 */
	public final class Arity0_OV<O> {

		private final WeakReference<O> out;

		public Arity0_OV(final O out) {
			this.out = new WeakReference<>(out);
		}

		public Arity0<O> computer() {
			return Computers.match(ops, opName, type(out));
		}
		
		public void compute() {
			computer().compute(out.get());
		}

		public Inplaces.Arity1<O> inplace(){
			return Inplaces.match(ops, opName, type(out));
		}
		
		public void mutate() {
			inplace().mutate(out.get());
		}
	}

	/**
	 * Builder with arity 1, input type given, output type given.
	 *
	 * @author Curtis Rueden
	 * @param <I> The type of the input.
	 * @param <O> The type of the output.
	 */
	public final class Arity1_IT_OT<I, O> {

		private final Nil<I> inType;
		private final Nil<O> outType;

		public Arity1_IT_OT(final Nil<I> inType, final Nil<O> outType) {
			this.inType = inType;
			this.outType = outType;
		}

		public Function<I, O> function() {
			return Functions.match(ops, opName, inType, outType);
		}

		public Computers.Arity1<I, O> computer() {
			return Computers.match(ops, opName, inType, outType);
		}
		
		public Inplaces.Arity2<I, O> inplace1(){
			return Inplaces.match1(ops, opName, inType, outType);
		}

		public Inplaces.Arity2<I, O> inplace2(){
			return Inplaces.match2(ops, opName, inType, outType);
		}
		
	}

	/**
	 * Builder with arity 1, input type given, output unspecified.
	 *
	 * @author Curtis Rueden
	 * @param <I> The type of the input.
	 */
	public final class Arity1_IT_OU<I> {

		private final Nil<I> inType;

		public Arity1_IT_OU(final Nil<I> inType) {
			this.inType = inType;
		}

		public <O> Arity1_IT_OT<I, O> outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		public <O> Arity1_IT_OT<I, O> outType(final Nil<O> outType) {
			return new Arity1_IT_OT<>(inType, outType);
		}

		public Function<I, ?> function() {
			return Functions.match(ops, opName, inType, Nil.of(Object.class));
		}
	}

	/**
	 * Builder with arity 1, input value given, output type given.
	 *
	 * @author Curtis Rueden
	 * @param <I> The type of the input.
	 * @param <O> The type of the output.
	 */
	public final class Arity1_IV_OT<I, O> {
		
		private final WeakReference<I> in;
		private final Nil<O> outType;

		public Arity1_IV_OT(final I in, final Nil<O> outType) {
			this.in = new WeakReference<>(in);
			this.outType = outType;
		}

		public Function<I, O> function() {
			return Functions.match(ops, opName, type(in), outType);
		}
		
		public Computers.Arity1<I, O> computer() {
			return Computers.match(ops, opName, type(in), outType);
		}
		
		public Inplaces.Arity2<I, O> inplace1(){
			return Inplaces.match1(ops, opName, type(in), outType);
		}

		public Inplaces.Arity2<I, O> inplace2(){
			return Inplaces.match2(ops, opName, type(in), outType);
		}

		public O apply() {
			return function().apply(in.get());
		}
	}

	/**
	 * Builder with arity 1, input value given, output unspecified.
	 *
	 * @author Curtis Rueden
	 * @param <I> The type of the input.
	 */
	public final class Arity1_IV_OU<I> {

		private final WeakReference<I> in;

		public Arity1_IV_OU(final I in) {
			this.in = new WeakReference<>(in);
		}

		public <O> Arity1_IV_OV<I, O> output(final O out) {
			return new Arity1_IV_OV<>(in.get(), out);
		}

		public <O> Arity1_IV_OT<I, O> outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		public <O> Arity1_IV_OT<I, O> outType(final Nil<O> outType) {
			return new Arity1_IV_OT<>(in.get(), outType);
		}

		public Function<I, ?> function() {
			return Functions.match(ops, opName, type(in), Nil.of(Object.class));
		}

		public Object apply() {
			return function().apply(in.get());
		}
	}

	/**
	 * Builder with arity 1, input value given, output value given.
	 *
	 * @author Curtis Rueden
	 * @param <I> The type of the input.
	 */
	public final class Arity1_IV_OV<I, O> {

		private final WeakReference<I> in;
		private final WeakReference<O> out;

		public Arity1_IV_OV(final I in, final O out) {
			this.in = new WeakReference<>(in);
			this.out = new WeakReference<>(out);
		}

		public Computers.Arity1<I, O> computer() {
			return Computers.match(ops, opName, type(in), type(out));
		}
		
		public Inplaces.Arity2<I, O> inplace1(){
			return Inplaces.match1(ops, opName, type(in), type(out));
		}

		public Inplaces.Arity2<I, O> inplace2(){
			return Inplaces.match2(ops, opName, type(in), type(out));
		}

		public void compute() {
			computer().compute(in.get(), out.get());
		}
		
		public void mutate1() {
			inplace1().mutate(in.get(), out.get());
		}

		public void mutate2() {
			inplace2().mutate(in.get(), out.get());
		}
	}

	/**
	 * Builder with arity 2, input types given, output type given.
	 *
	 * @author Curtis Rueden
	 * @param <I1> The type of input 1.
	 * @param <I2> The type of input 2.
	 * @param <O> The type of the output.
	 */
	public final class Arity2_IT_OT<I1, I2, O> {
		
		private final Nil<I1> in1Type;
		private final Nil<I2> in2Type;
		private final Nil<O> outType;

		public Arity2_IT_OT(final Nil<I1> in1Type, final Nil<I2> in2Type, final Nil<O> outType) {
			this.in1Type = in1Type;
			this.in2Type = in2Type;
			this.outType = outType;
		}

		public BiFunction<I1, I2, O> function() {
			return Functions.match(ops, opName, in1Type, in2Type, outType);
		}

		public Computers.Arity2<I1, I2, O> computer() {
			return Computers.match(ops, opName, in1Type, in2Type, outType);
		}
		
		public Inplaces.Arity3_1<I1, I2, O> inplace1() {
			return Inplaces.match1(ops, opName, in1Type, in2Type, outType);
		}

		public Inplaces.Arity3_2<I1, I2, O> inplace2() {
			return Inplaces.match2(ops, opName, in1Type, in2Type, outType);
		}

		public Inplaces.Arity3_3<I1, I2, O> inplace3() {
			return Inplaces.match3(ops, opName, in1Type, in2Type, outType);
		}
	}

	/**
	 * Builder with arity 2, input types given, output unspecified.
	 *
	 * @author Curtis Rueden
	 * @param <I1> The type of input 1.
	 * @param <I2> The type of input 2.
	 */
	public final class Arity2_IT_OU<I1, I2> {

		private final Nil<I1> in1Type;
		private final Nil<I2> in2Type;

		public Arity2_IT_OU(final Nil<I1> in1Type, final Nil<I2> in2Type) {
			this.in1Type = in1Type;
			this.in2Type = in2Type;
		}

		public <O> Arity2_IT_OT<I1, I2, O> outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		public <O> Arity2_IT_OT<I1, I2, O> outType(final Nil<O> outType) {
			return new Arity2_IT_OT<>(in1Type, in2Type, outType);
		}

		public BiFunction<I1, I2, ?> function() {
			return Functions.match(ops, opName, in1Type, in2Type, Nil.of(Object.class));
		}
	}

	/**
	 * Builder with arity 2, input values given, output type given.
	 *
	 * @author Curtis Rueden
	 * @param <I1> The type of input 1.
	 * @param <I2> The type of input 2.
	 * @param <O> The type of the output.
	 */
	public final class Arity2_IV_OT<I1, I2, O> {
		
		private final WeakReference<I1> in1;
		private final WeakReference<I2> in2;
		private final Nil<O> outType;

		public Arity2_IV_OT(final I1 in1, final I2 in2, final Nil<O> outType) {
			this.in1 = new WeakReference<>(in1);
			this.in2 = new WeakReference<>(in2);
			this.outType = outType;
		}

		public BiFunction<I1, I2, O> function() {
			return Functions.match(ops, opName, type(in1), type(in2), outType);
		}
		
		public Computers.Arity2<I1, I2, O> computer() {
			return Computers.match(ops, opName, type(in1), type(in2), outType);
		}
		
		public Inplaces.Arity3_1<I1, I2, O> inplace1() {
			return Inplaces.match1(ops, opName, type(in1), type(in2), outType);
		}

		public Inplaces.Arity3_2<I1, I2, O> inplace2() {
			return Inplaces.match2(ops, opName, type(in1), type(in2), outType);
		}

		public Inplaces.Arity3_3<I1, I2, O> inplace3() {
			return Inplaces.match3(ops, opName, type(in1), type(in2), outType);
		}

		public O apply() {
			return function().apply(in1.get(), in2.get());
		}
	}

	/**
	 * Builder with arity 2, input values given, output unspecified.
	 *
	 * @author Curtis Rueden
	 * @param <I1> The type of input 1.
	 * @param <I2> The type of input 2.
	 */
	public final class Arity2_IV_OU<I1, I2> {

		private final WeakReference<I1> in1;
		private final WeakReference<I2> in2;

		public Arity2_IV_OU(final I1 in1, final I2 in2) {
			this.in1 = new WeakReference<>(in1);
			this.in2 = new WeakReference<>(in2);
		}

		public <O> Arity2_IV_OV<I1, I2, O> output(final O out) {
			return new Arity2_IV_OV<>(in1.get(), in2.get(), out);
		}

		public <O> Arity2_IV_OT<I1, I2, O> outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		public <O> Arity2_IV_OT<I1, I2, O> outType(final Nil<O> outType) {
			return new Arity2_IV_OT<>(in1.get(), in2.get(), outType);
		}

		public BiFunction<I1, I2, ?> function() {
			return Functions.match(ops, opName, type(in1), type(in2), Nil.of(Object.class));
		}

		public Object apply() {
			return function().apply(in1.get(), in2.get());
		}
	}

	/**
	 * Builder with arity 2, input values given, output value given.
	 *
	 * @author Curtis Rueden
	 * @param <I1> The type of input 1.
	 * @param <I2> The type of input 2.
	 * @param <O> The type of the output.
	 */
	public final class Arity2_IV_OV<I1, I2, O> {

		private final WeakReference<I1> in1;
		private final WeakReference<I2> in2;
		private final WeakReference<O> out;

		public Arity2_IV_OV(final I1 in1, final I2 in2, final O out) {
			this.in1 = new WeakReference<>(in1);
			this.in2 = new WeakReference<>(in2);
			this.out = new WeakReference<>(out);
		}

		public Computers.Arity2<I1, I2, O> computer() {
			return Computers.match(ops, opName, type(in1), type(in2), type(out));
		}
		
		public Inplaces.Arity3_1<I1, I2, O> inplace1() {
			return Inplaces.match1(ops, opName, type(in1), type(in2), type(out));
		}

		public Inplaces.Arity3_2<I1, I2, O> inplace2() {
			return Inplaces.match2(ops, opName, type(in1), type(in2), type(out));
		}

		public Inplaces.Arity3_3<I1, I2, O> inplace3() {
			return Inplaces.match3(ops, opName, type(in1), type(in2), type(out));
		}

		public void compute() {
			computer().compute(in1.get(), in2.get(), out.get());
		}

		public void mutate1() {
			inplace1().mutate(in1.get(), in2.get(), out.get());
		}

		public void mutate2() {
			inplace2().mutate(in1.get(), in2.get(), out.get());
		}

		public void mutate3() {
			inplace3().mutate(in1.get(), in2.get(), out.get());
		}
	}
}
