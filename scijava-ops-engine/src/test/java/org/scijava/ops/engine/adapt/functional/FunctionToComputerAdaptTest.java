/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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
 
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.copy.CopyOpCollection;

public class FunctionToComputerAdaptTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new FunctionToComputerAdaptTestOps());
		ops.register(new CopyOpCollection());
		ops.register(objsFromNoArgConstructors(FunctionsToComputers.class.getDeclaredClasses()));
	}

	@Test
	public void testFunction1ToComputer1() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {2, 4}, output, 0);
	}

	@Test
	public void testFunction2ToComputer2() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {4, 8}, output, 0);
	}

	@Test
	public void testFunction3ToComputer3() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {6, 12}, output, 0);
	}

	@Test
	public void testFunction4ToComputer4() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {8, 16}, output, 0);
	}

	@Test
	public void testFunction5ToComputer5() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {10, 20}, output, 0);
	}

	@Test
	public void testFunction6ToComputer6() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {12, 24}, output, 0);
	}

	@Test
	public void testFunction7ToComputer7() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {14, 28}, output, 0);
	}

	@Test
	public void testFunction8ToComputer8() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {16, 32}, output, 0);
	}

	@Test
	public void testFunction9ToComputer9() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {18, 36}, output, 0);
	}

	@Test
	public void testFunction10ToComputer10() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {20, 40}, output, 0);
	}

	@Test
	public void testFunction11ToComputer11() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {22, 44}, output, 0);
	}

	@Test
	public void testFunction12ToComputer12() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {24, 48}, output, 0);
	}

	@Test
	public void testFunction13ToComputer13() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {26, 52}, output, 0);
	}

	@Test
	public void testFunction14ToComputer14() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {28, 56}, output, 0);
	}

	@Test
	public void testFunction15ToComputer15() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {30, 60}, output, 0);
	}

	@Test
	public void testFunction16ToComputer16() {
		double[] in = { 2, 4 };
		double[] output = { 0, 0 };
		ops.op("test.FtC").input(in, in, in, in, in, in, in, in, in, in, in, in, in, in, in, in).output(output).compute();
		Assertions.assertArrayEquals(new double[] {32, 64}, output, 0);
	}
}

