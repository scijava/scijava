/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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
package net.imagej.ops2.topology.eulerCharacteristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Octant} convenience class
 *
 * @author Richard Domander (Royal Veterinary College, London)
 */
public class OctantTest {
    @Test
    public void testIsNeighborhoodEmpty() throws Exception {
        final Img<BitType> img = ArrayImgs.bits(2, 2, 2);
        Octant<BitType> octant = new Octant<>(img);

        octant.setNeighborhood(1, 1, 1);

				assertTrue(octant.isNeighborhoodEmpty(),
					"Neighborhood should be empty");

				img.forEach(BitType::setOne);
				octant.setNeighborhood(1, 1, 1);

				assertFalse(octant.isNeighborhoodEmpty(),
					"Neighborhood should not be empty");
	    }

    @Test
    public void testSetNeighborhood() throws Exception {
        final Img<BitType> img = ArrayImgs.bits(3, 3, 3);
        Octant<BitType> octant = new Octant<>(img);

        final RandomAccess<BitType> access = img.randomAccess();
        for (int z = 0; z < 2; z++) {
            access.setPosition(z, 2);
            for (int y = 0; y < 2; y++) {
                access.setPosition(y, 1);
                for (int x = 0; x < 2; x++) {
                    access.setPosition(x, 0);
                    access.get().setOne();
                }
            }
        }

				octant.setNeighborhood(1, 1, 1);
				assertEquals(8, octant.getNeighborCount(),
					"All neighbours should be foreground");

				octant.setNeighborhood(2, 2, 2);
				assertEquals(1, octant.getNeighborCount(),
					"Wrong number of foreground neighbors");
	    }
}
