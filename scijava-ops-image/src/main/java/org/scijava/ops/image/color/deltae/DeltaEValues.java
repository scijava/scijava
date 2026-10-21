/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.color.deltae;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Computes the colour difference between two CIELAB colours given as
 * {@code double[]} triplets. See {@link DeltaE} for the formulas.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @see <a href="https://doi.org/10.1002/col.20070">G. Sharma, W. Wu and E. N.
 *      Dalal, "The CIEDE2000 color-difference formula: Implementation notes,
 *      supplementary test data, and mathematical observations", Color Res.
 *      Appl. 30(1):21-30, 2005</a>
 * @implNote op names='color.deltaE'
 */
public class DeltaEValues implements
	Functions.Arity3<double[], double[], String, Double>
{

	/**
	 * Computes a colour difference.
	 *
	 * @param lab1 the first (reference) colour, as L*, a*, b*
	 * @param lab2 the second colour, as L*, a*, b*
	 * @param formula the colour difference formula: CIE76, CIE94,
	 *          CIE94textiles or CIEDE2000 (default: CIEDE2000)
	 * @return the colour difference
	 */
	@Override
	public Double apply(final double[] lab1, final double[] lab2,
		@Nullable final String formula)
	{
		if (lab1.length != 3 || lab2.length != 3) {
			throw new IllegalArgumentException(
				"Colours must have three components (L*, a*, b*)");
		}
		final var f = formula == null ? DeltaE.CIEDE2000 : DeltaE.byName(formula);
		return f.compute(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);
	}
}
