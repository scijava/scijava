/* #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.filter.derivative;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.RealComposite;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Test for partial derivative op.
 *
 * @author Eike Heinz, University of Konstanz
 */

public class PartialDerivativeFilterTest extends AbstractOpTest {

	@Test
	public void test() {
		Img<FloatType> img = TestImgGeneration.floatArray(false, new long[] { 20,
			20 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 8 && counterX < 12 || counterY > 8 && counterY < 12) {
				cursorImg.next().setOne();
			}
			else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 20 == 0) {
				counterY++;
			}
			if (counterX == 20) {
				counterX = 0;
			}
			if (counterY == 20) {
				counterY = 0;
			}
		}

		RandomAccessibleInterval<FloatType> out = ops.op("filter.partialDerivative")
			.input(img, 0).outType(new Nil<RandomAccessibleInterval<FloatType>>()
			{}).apply();

		FloatType type = Util.getTypeFromInterval(out).createVariable();
		type.set(4.0f);
		RandomAccess<FloatType> outRA = out.randomAccess();
		for (int i = 0; i < 8; i++) {
			outRA.setPosition(new int[] { 9, i });
			assertEquals(type, outRA.get());

		}
		outRA.setPosition(new int[] { 9, 8 });
		type.set(3.0f);
		assertEquals(type, outRA.get());
		outRA.setPosition(new int[] { 9, 10 });
		type.set(0.0f);
		assertEquals(type, outRA.get());
		outRA.setPosition(new int[] { 9, 11 });
		type.set(1.0f);
		assertEquals(type, outRA.get());
		outRA.setPosition(new int[] { 9, 12 });
		type.set(3.0f);
		assertEquals(type, outRA.get());
		type.set(4.0f);
		for (int i = 13; i < 20; i++) {
			outRA.setPosition(new int[] { 9, i });
			assertEquals(type, outRA.get());

		}

		type.set(-4.0f);
		for (int i = 0; i < 8; i++) {
			outRA.setPosition(new int[] { 12, i });
			assertEquals(type, outRA.get());

		}
		outRA.setPosition(new int[] { 12, 8 });
		type.set(-3.0f);
		assertEquals(type, outRA.get());
		outRA.setPosition(new int[] { 12, 10 });
		type.set(0.0f);
		assertEquals(type, outRA.get());
		outRA.setPosition(new int[] { 12, 11 });
		type.set(-1.0f);
		assertEquals(type, outRA.get());
		outRA.setPosition(new int[] { 12, 12 });
		type.set(-3.0f);
		assertEquals(type, outRA.get());
		type.set(-4.0f);
		for (int i = 13; i < 20; i++) {
			outRA.setPosition(new int[] { 12, i });
			assertEquals(type, outRA.get());

		}
	}

	@Test
	public void testAllDerivatives() {
		Img<FloatType> img = TestImgGeneration.floatArray(false, new long[] { 20,
			20, 3 });

		Cursor<FloatType> cursorImg = img.cursor();
		int counterX = 0;
		int counterY = 0;
		while (cursorImg.hasNext()) {
			if (counterX > 8 && counterX < 12 || counterY > 8 && counterY < 12) {
				cursorImg.next().setOne();
			}
			else {
				cursorImg.next().setZero();
			}
			counterX++;
			if (counterX % 20 == 0) {
				counterY++;
			}
			if (counterX == 20) {
				counterX = 0;
			}
			if (counterY == 20) {
				counterY = 0;
			}
		}

		CompositeIntervalView<FloatType, RealComposite<FloatType>> out = ops.op(
			"filter.partialDerivative").input(img).outType(
				new Nil<CompositeIntervalView<FloatType, RealComposite<FloatType>>>()
				{}).apply();

		CompositeView<FloatType, RealComposite<FloatType>>.CompositeRandomAccess outRA =
			out.randomAccess();

		FloatType type = Util.getTypeFromInterval(img).createVariable();

		// position 9,8 in all dimensions

		outRA.setPosition(new int[] { 9, 8, 0 });
		RealComposite<FloatType> outvalue = outRA.get();
		Float[] correctValues = new Float[] { 12.0f, 4.0f, 0.0f };
		int i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		outRA.setPosition(new int[] { 9, 8, 1 });
		outvalue = outRA.get();
		correctValues = new Float[] { 12.0f, 4.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		outRA.setPosition(new int[] { 9, 8, 2 });
		outvalue = outRA.get();
		correctValues = new Float[] { 12.0f, 4.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		// position 9,9 in all dimensions

		outRA.setPosition(new int[] { 9, 9, 0 });
		outvalue = outRA.get();
		correctValues = new Float[] { 4.0f, 4.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		outRA.setPosition(new int[] { 9, 9, 1 });
		outvalue = outRA.get();
		correctValues = new Float[] { 4.0f, 4.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		outRA.setPosition(new int[] { 9, 9, 2 });
		outvalue = outRA.get();
		correctValues = new Float[] { 4.0f, 4.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		// position 9,10 in all dimensions

		outRA.setPosition(new int[] { 9, 10, 0 });
		outvalue = outRA.get();
		correctValues = new Float[] { 0.0f, 0.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		outRA.setPosition(new int[] { 9, 10, 1 });
		outvalue = outRA.get();
		correctValues = new Float[] { 0.0f, 0.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		outRA.setPosition(new int[] { 9, 10, 2 });
		outvalue = outRA.get();
		correctValues = new Float[] { 0.0f, 0.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		// position 9,11 in all dimensions

		outRA.setPosition(new int[] { 9, 11, 0 });
		outvalue = outRA.get();
		correctValues = new Float[] { 4.0f, -4.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		outRA.setPosition(new int[] { 9, 11, 1 });
		outvalue = outRA.get();
		correctValues = new Float[] { 4.0f, -4.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}

		outRA.setPosition(new int[] { 9, 11, 2 });
		outvalue = outRA.get();
		correctValues = new Float[] { 4.0f, -4.0f, 0.0f };
		i = 0;
		for (FloatType value : outvalue) {
			type.set(correctValues[i]);
			assertEquals(type, value);
			i++;
		}
	}
}
