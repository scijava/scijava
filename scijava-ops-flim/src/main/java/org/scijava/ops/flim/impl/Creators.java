/*-
 * #%L
 * Fluorescence lifetime analysis in ImageJ.
 * %%
 * Copyright (C) 2017 - 2022 Board of Regents of the University of Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.flim.impl;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * FLIMLib fitters on a {@link net.imglib2.RandomAccessibleInterval} of
 * time-resolved FLIM data.
 *
 * @author Dasong Gao
 */
public class Creators {

	private Creators() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Convenience method to generate a square kernel for use in flim Ops
	 *
	 * @param size the width/height of the kernel
	 * @return a kernel for use in Flim
	 * @implNote op names="create.kernelFlim" type=Function
	 */
	public static Img<DoubleType> makeSquareKernel(int size) {
		Img<DoubleType> out = ArrayImgs.doubles(size, size, 1);
		Cursor<DoubleType> cursor = out.cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.get().set(1.0);
		}
		return out;
	}

}
