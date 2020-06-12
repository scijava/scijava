/*-
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.linalg.rotate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import net.imagej.ops2.AbstractOpTest;

import org.joml.AxisAngle4d;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import org.scijava.ops.core.builder.OpBuilder;

/**
 * Tests for {@link Rotate3d}.
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class Rotate3dTest extends AbstractOpTest {

	private static final Quaterniondc IDENTITY = new Quaterniond(1, 0, 0, 0);

	@Test
	public void testAxisAngle() {
		final Vector3d xAxis = new Vector3d(1, 0, 0);
		final Vector3d in = new Vector3d(xAxis);
		final AxisAngle4d axisAngle = new AxisAngle4d(Math.PI / 2.0, 0, 0, 1);
		final Vector3d expected = xAxis.rotate(new Quaterniond(axisAngle));

		final Vector3d result = op("linalg.rotate").input(in, axisAngle).outType(Vector3d.class).apply();

		assertEquals(expected, result, "Rotation is incorrect");
	}

	@Test
	public void testCalculate() {
		final Vector3d xAxis = new Vector3d(1, 0, 0);
		final Vector3d in = new Vector3d(xAxis);

		final Vector3d result = op("linalg.rotate").input(in, IDENTITY).outType(Vector3d.class).apply();

		assertNotSame(in, result, "Op should create a new object for output");
		assertEquals(xAxis, result, "Rotation is incorrect");
	}

	// TODO: X -> Inplace transformers
//	@Test
//	public void testMutate() {
//		final Vector3d xAxis = new Vector3d(1, 0, 0);
//		final Vector3d in = new Vector3d(xAxis);
//		final Quaterniond q = new Quaterniond(new AxisAngle4d(Math.PI / 2.0, 0, 0,
//			1));
//		final Vector3d expected = xAxis.rotate(q);
//
//		final Vector3d result = ops.linalg().rotate1(in, q);
//
//		assertSame("Mutate should operate on the input object", in, result);
//		assertEquals("Rotation is incorrect", expected, result);
//	}
}
