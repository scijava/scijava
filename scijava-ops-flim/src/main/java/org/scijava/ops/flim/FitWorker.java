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

package org.scijava.ops.flim;

import net.imglib2.type.numeric.RealType;

import java.util.List;

public interface FitWorker<I extends RealType<I>> {

	/**
	 * The handler interface for fit events.
	 *
	 * @param <I> The parameter type
	 */
	public interface FitEventHandler<I extends RealType<I>> {

		/**
		 * The handler called by {@link FitWorker}s upon completion of all fits.
		 *
		 * @param params the params
		 * @param results the results
		 */
		default void onComplete(FitParams<I> params, FitResults results) {}

		/**
		 * The handler called by {@link FitWorker}s upon completion of a single fit.
		 *
		 * @param pos the x, y coordinate of the trans being fitted
		 * @param params the params (volatile) of the completed fit
		 * @param results the results (volatile) from the completed fit
		 */
		default void onSingleComplete(int[] pos, FitParams<I> params,
			FitResults results)
		{}
	}

	/**
	 * How many parameters should there be in {@code results.param}? E.g. 3 for
	 * one-component {@link LMAFitWorker} and 5 for {@link PhasorFitWorker}.
	 *
	 * @return The number of output parameters in the parameter array.
	 */
	int nParamOut();

	/**
	 * How many bins will be fitted?
	 *
	 * @return {@code fitEnd - fitStart}
	 */
	int nDataOut();

	;

	/**
	 * Fit all coordinates listed and handles fit events.
	 *
	 * @param pos the coordinates of trans to fit
	 * @param handler the fit event handler
	 */
	void fitBatch(List<int[]> pos, FitEventHandler<I> handler);
}
