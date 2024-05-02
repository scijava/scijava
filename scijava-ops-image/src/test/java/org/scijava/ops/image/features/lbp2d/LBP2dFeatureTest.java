/*
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

package org.scijava.ops.image.features.lbp2d;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.scijava.ops.image.AbstractFeatureTest;
import net.imglib2.type.numeric.integer.LongType;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Test for {@Link Lbp2dFeature}
 *
 * @author Andreas Graumann (University of Konstanz)
 */
public class LBP2dFeatureTest extends AbstractFeatureTest {

	@Test
	public void testLbp2d() {
		final ArrayList<LongType> hist = ops.op("features.lbp2d").input(random, 1,
			4).outType(new Nil<ArrayList<LongType>>()
		{}).apply();

		// Test values proved by calculating small toy example by hand.
		assertEquals(5412.0, hist.get(0).getRealDouble(), 1e-3, "features.lbp2d");
		assertEquals(0.0, hist.get(1).getRealDouble(), 1e-3, "features.lbp2d");
		assertEquals(4251.0, hist.get(2).getRealDouble(), 1e-3, "features.lbp2d");
		assertEquals(337.0, hist.get(3).getRealDouble(), 1e-3, "features.lbp2d");

	}

}
