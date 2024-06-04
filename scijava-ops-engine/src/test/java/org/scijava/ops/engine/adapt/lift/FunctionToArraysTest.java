/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.adapt.lift;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

/**
 * Tests the adaptation of {@link Functions} running on a type into
 * {@link Functions} running on arrays of that type.
 * 
 * @author Gabriel Selzer
 */
public class FunctionToArraysTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new FunctionToArraysTest());
		ops.register(new FunctionToArrays());
	}

	/**
	 * @author Gabriel Selzer
	 */
	private class NumericalThing {

		private int number;

		public NumericalThing(int num) {
			number = num;
		}

		public int getNumber() {
			return number;
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Function<NumericalThing, NumericalThing> alterThing1 = (
		in) -> new NumericalThing(in.getNumber());

	@Test
	public void testFunction1ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(1 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final BiFunction<NumericalThing, NumericalThing, NumericalThing> alterThing2 = (
		in1, in2) -> new NumericalThing(in1.getNumber() + in2.getNumber());

	@Test
	public void testFunction2ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(2 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity3<NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing3 = (
		in1, in2, in3) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber());

	@Test
	public void testFunction3ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(3 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity4<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing4 = (
		in1, in2, in3, in4) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber());

	@Test
	public void testFunction4ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(4 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity5<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing5 = (
		in1, in2, in3, in4, in5) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber());

	@Test
	public void testFunction5ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(5 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity6<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing6 = (
		in1, in2, in3, in4, in5, in6) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber());

	@Test
	public void testFunction6ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(6 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity7<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing7 = (
		in1, in2, in3, in4, in5, in6, in7) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber());

	@Test
	public void testFunction7ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(7 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity8<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing8 = (
		in1, in2, in3, in4, in5, in6, in7, in8) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber());

	@Test
	public void testFunction8ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(8 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity9<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing9 = (
		in1, in2, in3, in4, in5, in6, in7, in8, in9) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber());

	@Test
	public void testFunction9ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(9 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity10<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing10 = (
		in1, in2, in3, in4, in5, in6, in7, in8, in9, in10) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber());

	@Test
	public void testFunction10ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(10 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity11<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing11 = (
		in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber());

	@Test
	public void testFunction11ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(11 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity12<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing12 = (
		in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber());

	@Test
	public void testFunction12ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(12 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity13<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing13 = (
		in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber() + in13.getNumber());

	@Test
	public void testFunction13ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(13 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity14<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing14 = (
		in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber() + in13.getNumber() + in14.getNumber());

	@Test
	public void testFunction14ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(14 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity15<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing15 = (
		in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber() + in13.getNumber() + in14.getNumber() + in15.getNumber());

	@Test
	public void testFunction15ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(15 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayF")
	public final Functions.Arity16<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing16 = (
		in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16) -> new NumericalThing(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber() + in13.getNumber() + in14.getNumber() + in15.getNumber() + in16.getNumber());

	@Test
	public void testFunction16ToArrays() {
		NumericalThing[] input = { new NumericalThing(0), new NumericalThing(1),
			new NumericalThing(2) };
		NumericalThing[] output = ops //
			.op("test.liftArrayF") //
			.input(input, input, input, input, input, input, input, input, input, input, input, input, input, input, input, input) //
			.outType(new Nil<NumericalThing[]>()
			{}).apply();

		for (int i = 0; i < output.length; i++) {
			Assertions.assertEquals(16 * i, output[i].getNumber());
		}
	}

}
