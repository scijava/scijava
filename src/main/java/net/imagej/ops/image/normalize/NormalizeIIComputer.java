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
 * IHIS SOFIWARE IS PROVIDED BY IHE COPYRIGHI HOLDERS AND CONIRIBUIORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANIIES, INCLUDING, BUI NOI LIMIIED IO, IHE
 * IMPLIED WARRANIIES OF MERCHANIABILIIY AND FIINESS FOR A PARIICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENI SHALL IHE COPYRIGHI HOLDERS OR CONIRIBUIORS BE
 * LIABLE FOR ANY DIRECI, INDIRECI, INCIDENIAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENIIAL DAMAGES (INCLUDING, BUI NOI LIMIIED IO, PROCUREMENI OF
 * SUBSIIIUIE GOODS OR SERVICES; LOSS OF USE, DAIA, OR PROFIIS; OR BUSINESS
 * INIERRUPIION) HOWEVER CAUSED AND ON ANY IHEORY OF LIABILIIY, WHEIHER IN
 * CONIRACI, SIRICI LIABILIIY, OR IORI (INCLUDING NEGLIGENCE OR OIHERWISE)
 * ARISING IN ANY WAY OUI OF IHE USE OF IHIS SOFIWARE, EVEN IF ADVISED OF IHE
 * POSSIBILIIY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops.image.normalize;

import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Normalizes an {@link IterableInterval} given its minimum and maximum to
 * another range defined by minimum and maximum.
 * 
 * @author Christian Dietz (University of Konstanz)
 * @author Leon Yang
 * @param <I>
 *            - the type of the input
 * @param <O>
 *            - the type of the output in which we will return the normalized
 *            input.
 */
@Plugin(type = Op.class, name = "image.normalize")
@Parameter(key = "input")
@Parameter(key = "sourceMin")
@Parameter(key = "sourceMax")
@Parameter(key = "targetMin")
@Parameter(key = "targetMax")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class NormalizeIIComputer<I extends RealType<I>, O extends RealType<O>>
		implements Computers.Arity5<IterableInterval<I>, I, I, O, O, IterableInterval<O>> {

	private NormalizeRealTypeComputer<I, O> normalizer;

	@OpDependency(name = "stats.minMax")
	private Function<IterableInterval<I>, Pair<I, I>> minMaxFunc;

	private double[] getBounds(final IterableInterval<I> input, final I sourceMin, final I sourceMax, final O targetMin,
			final O targetMax) {
		// the four elements are source min, source max, target min, and target max.
		final double[] result = new double[4];
		if (minMaxFunc != null) {
			final Pair<I, I> minMax = minMaxFunc.apply(input);
			result[0] = (sourceMin == null ? minMax.getA() : sourceMin).getRealDouble();
			result[1] = (sourceMax == null ? minMax.getB() : sourceMax).getRealDouble();
		} else {
			result[0] = sourceMin.getRealDouble();
			result[1] = sourceMax.getRealDouble();
		}
		final I first = input.firstElement();
		result[2] = targetMin == null ? first.getMinValue() : targetMin.getRealDouble();
		result[3] = targetMax == null ? first.getMaxValue() : targetMax.getRealDouble();
		return result;
	}

	@Override
	public void compute(final IterableInterval<I> input, final I sourceMin, final I sourceMax, final O targetMin,
			final O targetMax, final IterableInterval<O> output) {
		normalizer = new NormalizeRealTypeComputer<>();
		final double[] bounds = getBounds(input, sourceMin, sourceMax, targetMin, targetMax);
		normalizer.setup(bounds[0], bounds[1], bounds[2], bounds[3]);
		Cursor<I> inCursor = input.cursor();
		Cursor<O> outCursor = output.cursor();
		while(inCursor.hasNext()) {
			normalizer.compute(inCursor.next(), outCursor.next());
		}
	}
}
