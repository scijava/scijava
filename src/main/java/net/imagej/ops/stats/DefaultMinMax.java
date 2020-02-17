/*
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

package net.imagej.ops.stats;

import java.util.NoSuchElementException;
import java.util.function.Function;

import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;

import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * {@link Op} to calculate the {@code stats.minMax}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I>
 *            input type
 */
@Plugin(type = Op.class, name = "stats.minMax")
@Parameter(key = "iterableInput")
@Parameter(key = "minMax", itemIO = ItemIO.OUTPUT)
public class DefaultMinMax<I extends RealType<I>> implements Function<Iterable<I>, Pair<I, I>> {

	@Override
	public Pair<I, I> apply(final Iterable<I> input) {

		// set minVal to the largest possible value and maxVal to the smallest possible.
		I minVal, maxVal;
		try {
			minVal = input.iterator().next().createVariable();
			minVal.setReal(minVal.getMaxValue());
			maxVal = minVal.createVariable();
			maxVal.setReal(maxVal.getMinValue());
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Cannot determine minimum/maximum of an empty Iterator!");
		}

		for (final I in : input) {
			if (in.compareTo(minVal) < 0)
				minVal.set(in);
			if (in.compareTo(maxVal) > 0)
				maxVal.set(in);
		}
		return new ValuePair<>(minVal, maxVal);
	}

}
