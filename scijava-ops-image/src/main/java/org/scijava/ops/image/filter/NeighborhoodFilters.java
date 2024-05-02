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

package org.scijava.ops.image.filter;

import net.imglib2.algorithm.neighborhood.Neighborhood;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

public final class NeighborhoodFilters {

	private NeighborhoodFilters() {
		// Prevent instantiation of static utility class
	}

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
	 * @implNote op name='filter.max', type='Computer'
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
	 * @implNote op name='filter.mean',type='Computer'
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
	 * @implNote op name='filter.median', type='Computer'
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
	 * @implNote op name='filter.min', type='Computer'
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
	 * @implNote op name='filter.variance', type='Computer'
	 */
	public static <T, V> void defaultVariance(@OpDependency(
		name = "stats.variance") Computers.Arity1<Iterable<T>, V> op,
		Neighborhood<T> neighborhood, V output)
	{
		op.compute(neighborhood, output);
	}
}
