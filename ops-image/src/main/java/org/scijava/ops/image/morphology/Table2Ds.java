/*-
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

package org.scijava.ops.image.morphology;

import java.util.function.Function;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.morphology.table2d.Branchpoints;
import net.imglib2.algorithm.morphology.table2d.Bridge;
import net.imglib2.algorithm.morphology.table2d.Clean;
import net.imglib2.algorithm.morphology.table2d.Endpoints;
import net.imglib2.algorithm.morphology.table2d.Fill;
import net.imglib2.algorithm.morphology.table2d.Hbreak;
import net.imglib2.algorithm.morphology.table2d.Life;
import net.imglib2.algorithm.morphology.table2d.Majority;
import net.imglib2.algorithm.morphology.table2d.Remove;
import net.imglib2.algorithm.morphology.table2d.Spur;
import net.imglib2.algorithm.morphology.table2d.Thicken;
import net.imglib2.algorithm.morphology.table2d.Thin;
import net.imglib2.algorithm.morphology.table2d.Vbreak;
import net.imglib2.img.Img;
import net.imglib2.type.BooleanType;

import org.scijava.function.Computers;

/**
 * Wraps all of <a href=
 * "https://github.com/imglib/imglib2-algorithm/tree/master/src/main/java/net/imglib2/algorithm/morphology/table2d">imglib2-algorithm's
 * table2D</a> algorithms
 *
 * @author Gabriel Selzer
 * @param <B> - any Type extending {@link BooleanType}
 */
public class Table2Ds<B extends BooleanType<B>> {

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.branchpoints'
	 */
	public final Function<Img<B>, Img<B>> branchPointsFunc =
		Branchpoints::branchpoints;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.branchpoints'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> branchPointsComputer =
		Branchpoints::branchpoints;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.bridge'
	 */
	public final Function<Img<B>, Img<B>> bridgeFunc = Bridge::bridge;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.bridge'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> bridgeComputer =
		Bridge::bridge;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.clean'
	 */
	public final Function<Img<B>, Img<B>> cleanFunc = Clean::clean;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.clean'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> cleanComputer =
		Clean::clean;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.endpoints'
	 */
	public final Function<Img<B>, Img<B>> endpointsFunc = Endpoints::endpoints;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.endpoints'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> endpointsComputer =
		Endpoints::endpoints;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.fill'
	 */
	public final Function<Img<B>, Img<B>> fillFunc = Fill::fill;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.fill'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> fillComputer =
		Fill::fill;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.hbreak'
	 */
	public final Function<Img<B>, Img<B>> hbreakFunc = Hbreak::hbreak;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.hbreak'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> hbreakComputer =
		Hbreak::hbreak;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.life'
	 */
	public final Function<Img<B>, Img<B>> lifeFunc = Life::life;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.life'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> lifeComputer =
		Life::life;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.majority'
	 */
	public final Function<Img<B>, Img<B>> majorityFunc = Majority::majority;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.majority'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> majorityComputer =
		Majority::majority;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.remove'
	 */
	public final Function<Img<B>, Img<B>> removeFunc = Remove::remove;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.remove'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> removeComputer =
		Remove::remove;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.spur'
	 */
	public final Function<Img<B>, Img<B>> spurFunc = Spur::spur;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.spur'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> spurComputer =
		Spur::spur;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.thicken'
	 */
	public final Function<Img<B>, Img<B>> thickenFunc = Thicken::thicken;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.thicken'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> thickenComputer =
		Thicken::thicken;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.thin'
	 */
	public final Function<Img<B>, Img<B>> thinFunc = Thin::thin;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.thin'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> thinComputer =
		Thin::thin;

	/**
	 * @input input
	 * @output result
	 * @implNote op names='morphology.vbreak'
	 */
	public final Function<Img<B>, Img<B>> vbreakFunc = Vbreak::vbreak;

	/**
	 * @input input
	 * @container result
	 * @implNote op names='morphology.vbreak'
	 */
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> vbreakComputer =
		Vbreak::vbreak;

}
