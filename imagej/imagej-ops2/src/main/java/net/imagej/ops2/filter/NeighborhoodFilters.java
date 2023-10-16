/*-
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

package net.imagej.ops2.filter;

import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

public class NeighborhoodFilters {

	/**
	 * Computes the maximum over a {@link Neighborhood}, and stores it in the
	 * passed output container
	 *
	 * @param op the Op able to compute the maximum
	 * @param neighborhood the {@link Neighborhood} to compute over
	 * @param output the preallocated output container
	 * @param <T> the {@link java.lang.reflect.Type} of the elements of
	 *          {@code neighborhood}
	 * @param <V> the {@link java.lang.reflect.Type} of the output container
	 * @implNote op name='filter.max',type='org.scijava.function.Computers$Arity1'
	 */
	public static <T, V> void defaultMax(@OpDependency(
		name = "stats.max") Computers.Arity1<Iterable<T>, V> op,
		Neighborhood<T> neighborhood, V output)
	{
		op.compute(neighborhood, output);
	}

	/**
	 * Computes the mean over a {@link Neighborhood}, and stores it in the passed
	 * output container
	 * 
	 * @param op the Op able to compute the mean
	 * @param neighborhood the {@link Neighborhood} to compute over
	 * @param output the preallocated output container
	 * @param <T> the {@link java.lang.reflect.Type} of the elements of
	 *          {@code neighborhood}
	 * @param <V> the {@link java.lang.reflect.Type} of the output container
	 * @implNote op
	 *           name='filter.mean',type='org.scijava.function.Computers$Arity1'
	 */
	public static <T, V> void defaultMean(@OpDependency(
		name = "stats.mean") Computers.Arity1<Iterable<T>, V> op,
		Neighborhood<T> neighborhood, V output)
	{
		op.compute(neighborhood, output);
	}

	/**
	 * Computes the median over a {@link Neighborhood}, and stores it in the
	 * passed output container
	 *
	 * @param op the Op able to compute the median
	 * @param neighborhood the {@link Neighborhood} to compute over
	 * @param output the preallocated output container
	 * @param <T> the {@link java.lang.reflect.Type} of the elements of
	 *          {@code neighborhood}
	 * @param <V> the {@link java.lang.reflect.Type} of the output container
	 * @implNote op
	 *           name='filter.median',type='org.scijava.function.Computers$Arity1'
	 */
	public static <T, V> void defaultMedian(@OpDependency(
		name = "stats.median") Computers.Arity1<Iterable<T>, V> op,
		Neighborhood<T> neighborhood, V output)
	{
		op.compute(neighborhood, output);
	}

	/**
	 * Computes the minimum over a {@link Neighborhood}, and stores it in the
	 * passed output container
	 *
	 * @param op the Op able to compute the minimum
	 * @param neighborhood the {@link Neighborhood} to compute over
	 * @param output the preallocated output container
	 * @param <T> the {@link java.lang.reflect.Type} of the elements of
	 *          {@code neighborhood}
	 * @param <V> the {@link java.lang.reflect.Type} of the output container
	 * @implNote op name='filter.min',type='org.scijava.function.Computers$Arity1'
	 */
	public static <T, V> void defaultMinimum(@OpDependency(
		name = "stats.min") Computers.Arity1<Iterable<T>, V> op,
		Neighborhood<T> neighborhood, V output)
	{
		op.compute(neighborhood, output);
	}

	/**
	 * Computes the variance over a {@link Neighborhood}, and stores it in the
	 * passed output container
	 *
	 * @param op the Op able to compute the variance
	 * @param neighborhood the {@link Neighborhood} to compute over
	 * @param output the preallocated output container
	 * @param <T> the {@link java.lang.reflect.Type} of the elements of
	 *          {@code neighborhood}
	 * @param <V> the {@link java.lang.reflect.Type} of the output container
	 * @implNote op
	 *           name='filter.variance',type='org.scijava.function.Computers$Arity1'
	 */
	public static <T, V> void defaultVariance(@OpDependency(
		name = "stats.variance") Computers.Arity1<Iterable<T>, V> op,
		Neighborhood<T> neighborhood, V output)
	{
		op.compute(neighborhood, output);
	}
}
