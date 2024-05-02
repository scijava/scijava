/*-
 * #%L
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

package org.scijava.ops.image.linalg.rotate;

import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.scijava.function.Computers;

/**
 * Rotates the vector by the quaternion.
 *
 * @author Richard Domander (Royal Veterinary College, London)
 * @author Gabriel Selzer
 */
public class Rotations {

	/**
	 * @input inVector
	 * @input quaternion
	 * @container vDot
	 * @implNote op names='linalg.rotate'
	 */
	public final Computers.Arity2<Vector3d, Quaterniondc, Vector3d> rotate3d = (v,
		q, vDot) -> {
		vDot.set(v);
		vDot.rotate(q);
	};

	/**
	 * @input inVector
	 * @input axisAngle
	 * @container vDot
	 * @implNote op names='linalg.rotate'
	 */
	public final Computers.Arity2<Vector3d, AxisAngle4d, Vector3d> rotate3dAxisAngle =
		(v, aa, vDot) -> rotate3d.compute(v, new Quaterniond(aa), vDot);

	/**
	 * @input inVector
	 * @input quaternion
	 * @container vDot
	 * @implNote op names='linalg.rotate'
	 */
	public final Computers.Arity2<Vector3f, Quaternionfc, Vector3f> rotate3f = (v,
		q, vDot) -> {
		vDot.set(v);
		vDot.rotate(q);
	};

	/**
	 * @input inVector
	 * @input axisAngle
	 * @container vDot
	 * @implNote op names='linalg.rotate'
	 */
	public final Computers.Arity2<Vector3f, AxisAngle4f, Vector3f> rotate3fAxisAngle =
		(v, aa, vDot) -> rotate3f.compute(v, new Quaternionf(aa), vDot);

}
