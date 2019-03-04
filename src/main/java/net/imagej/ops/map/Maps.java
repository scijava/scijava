/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
 * %%
 * Redistribution and use in source and Bi forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in Bi form must reproduce the above copyright notice,
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

package net.imagej.ops.map;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.util.Intervals;

import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.ops.core.computer.Computer5;
import org.scijava.ops.core.computer.Computer6;
import org.scijava.ops.core.computer.NullaryComputer;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.ops.core.inplace.BiInplaceSecond;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.ops.core.inplace.Inplace3First;
import org.scijava.ops.core.inplace.Inplace3Second;
import org.scijava.ops.core.inplace.Inplace4Second;
import org.scijava.ops.core.inplace.Inplace5First;
import org.scijava.ops.core.inplace.Inplace6First;
import org.scijava.ops.core.inplace.Inplace6Second;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Utility class for {@link MapOp}s.
 *
 * @author Leon Yang
 */
@Plugin(type = OpCollection.class)
public class Maps<I, I1, I2, IO, O> {

	// -- Helpers for conforms() --

	public static <I, O> boolean compatible(final IterableInterval<I> a, final IterableInterval<O> b) {
		return a.iterationOrder().equals(b.iterationOrder());
	}

	public static <I, O> boolean compatible(final IterableInterval<I> a, final RandomAccessibleInterval<O> b) {
		return Intervals.contains(b, a);
	}

	public static <I, O> boolean compatible(final RandomAccessibleInterval<I> a, final IterableInterval<O> b) {
		return Intervals.contains(a, b);
	}

	public static <I1, I2, O> boolean compatible(final IterableInterval<I1> a, final IterableInterval<I2> b,
			final IterableInterval<O> c) {
		return a.iterationOrder().equals(b.iterationOrder()) && a.iterationOrder().equals(c.iterationOrder());
	}

	public static <I1, I2, O> boolean compatible(final IterableInterval<I1> a, final IterableInterval<I2> b,
			final RandomAccessibleInterval<O> c) {
		return a.iterationOrder().equals(b.iterationOrder()) && Intervals.contains(c, a);
	}

	public static <I1, I2, O> boolean compatible(final IterableInterval<I1> a, final RandomAccessibleInterval<I2> b,
			final IterableInterval<O> c) {
		return a.iterationOrder().equals(c.iterationOrder()) && Intervals.contains(b, a);
	}

	public static <I1, I2, O> boolean compatible(final RandomAccessibleInterval<I1> a, final IterableInterval<I2> b,
			final IterableInterval<O> c) {
		return b.iterationOrder().equals(c.iterationOrder()) && Intervals.contains(a, b);
	}

	public static <I1, I2, O> boolean compatible(final IterableInterval<I1> a, final RandomAccessibleInterval<I2> b,
			final RandomAccessibleInterval<O> c) {
		return Intervals.contains(b, a) && Intervals.contains(c, a);
	}

	public static <I1, I2, O> boolean compatible(final RandomAccessibleInterval<I1> a, final IterableInterval<I2> b,
			final RandomAccessibleInterval<O> c) {
		return Intervals.contains(a, b) && Intervals.contains(c, b);
	}

	public static <I1, I2, O> boolean compatible(final RandomAccessibleInterval<I1> a,
			final RandomAccessibleInterval<I2> b, final IterableInterval<O> c) {
		return Intervals.contains(a, c) && Intervals.contains(b, c);
	}

	// -- Nullary Maps --

	@OpField(names = "map")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer<NullaryComputer<O>, Iterable<O>> mapNullary = (op, mapped) -> {
		for (final O e : mapped)
			op.compute(e);
	};

	@OpField(names = "map")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer4<NullaryComputer<O>, Long, Long, Long, IterableInterval<O>> mapNullaryParams = (op,
			startIndex, stepSize, numSteps, mapped) -> {
		if (numSteps <= 0)
			return;
		final Cursor<O> aCursor = mapped.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			aCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			op.compute(aCursor.get());
		}
	};

	// -- Unary Maps --

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final BiComputer<IterableInterval<I>, Computer<I, O>, IterableInterval<O>> mapIIToII = (in1,
			op, mapped) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<I> aCursor = in1.cursor();
		final Cursor<O> bCursor = mapped.cursor();
		while (aCursor.hasNext()) {
			op.compute(aCursor.next(), bCursor.next());
		}
	};

	@OpField(names = "map", priority = Priority.LOW)
	@Parameter(key = "in1")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final BiComputer<IterableInterval<I>, Computer<I, O>,RandomAccessibleInterval<O>> mapIIToRAI = (in1,
			op, mapped) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<I> aCursor = in1.localizingCursor();
		final RandomAccess<O> bAccess = mapped.randomAccess();
		while (aCursor.hasNext()) {
			aCursor.fwd();
			bAccess.setPosition(aCursor);
			op.compute(aCursor.get(), bAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	public final Inplace3Second<RandomAccessibleInterval<I>, IterableInterval<O>, Computer<I, O>> mapRAIToII = (in1,
			mapped, op) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final RandomAccess<I> aAccess = in1.randomAccess();
		final Cursor<O> bCursor = mapped.localizingCursor();
		while (bCursor.hasNext()) {
			bCursor.fwd();
			aAccess.setPosition(bCursor);
			op.compute(aAccess.get(), bCursor.get());
		}
	};

	// -- Bi Maps --

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	public final Inplace4Second<IterableInterval<I1>, IterableInterval<I2>, IterableInterval<O>, BiComputer<I1, I2, O>> mapIIAndIIToII = (
			in1, in2, mapped, op) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<I1> aCursor = in1.cursor();
		final Cursor<I2> bCursor = in2.cursor();
		final Cursor<O> cCursor = mapped.cursor();
		while (aCursor.hasNext()) {
			op.compute(aCursor.next(), bCursor.next(), cCursor.next());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer3<IterableInterval<I1>, IterableInterval<I2>, BiComputer<I1, I2, O>,RandomAccessibleInterval<O>> mapIIAndIIToRAI = (
			in1, in2, op, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<I1> aCursor = in1.localizingCursor();
		final Cursor<I2> bCursor = in2.cursor();
		final RandomAccess<O> cAccess = mapped.randomAccess();
		while (aCursor.hasNext()) {
			aCursor.fwd();
			cAccess.setPosition(aCursor);
			op.compute(aCursor.get(), bCursor.next(), cAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer3<IterableInterval<I1>, RandomAccessibleInterval<I2>, BiComputer<I1, I2, O>, IterableInterval<O>> mapIIAndRAIToII = (
			in1, in2, op, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<I1> aCursor = in1.localizingCursor();
		final RandomAccess<I2> bAccess = in2.randomAccess();
		final Cursor<O> cCursor = mapped.cursor();
		while (aCursor.hasNext()) {
			aCursor.fwd();
			bAccess.setPosition(aCursor);
			op.compute(aCursor.get(), bAccess.get(), cCursor.next());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer3<RandomAccessibleInterval<I1>, IterableInterval<I2>, BiComputer<I1, I2, O>, IterableInterval<O>> mapRAIAndIIToII = (
			in1, in2, op, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final RandomAccess<I1> aAccess = in1.randomAccess();
		final Cursor<I2> bCursor = in2.localizingCursor();
		final Cursor<O> cCursor = mapped.cursor();
		while (bCursor.hasNext()) {
			bCursor.fwd();
			aAccess.setPosition(bCursor);
			op.compute(aAccess.get(), bCursor.get(), cCursor.next());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer3<IterableInterval<I1>, RandomAccessibleInterval<I2>, BiComputer<I1, I2, O>, RandomAccessibleInterval<O>> mapIIAndRAIToRAI = (
			in1, in2, op, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<I1> aCursor = in1.localizingCursor();
		final RandomAccess<I2> bAccess = in2.randomAccess();
		final RandomAccess<O> cAccess = mapped.randomAccess();
		while (aCursor.hasNext()) {
			aCursor.fwd();
			bAccess.setPosition(aCursor);
			cAccess.setPosition(aCursor);
			op.compute(aCursor.get(), bAccess.get(), cAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer3<RandomAccessibleInterval<I1>, IterableInterval<I2>, BiComputer<I1, I2, O>, RandomAccessibleInterval<O>> mapRAIAndIIToRAI = (
			in1, in2, op, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final RandomAccess<I1> aAccess = in1.randomAccess();
		final Cursor<I2> bCursor = in2.localizingCursor();
		final RandomAccess<O> cAccess = mapped.randomAccess();
		while (bCursor.hasNext()) {
			bCursor.fwd();
			aAccess.setPosition(bCursor);
			cAccess.setPosition(bCursor);
			op.compute(aAccess.get(), bCursor.get(), cAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer3<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, BiComputer<I1, I2, O>, IterableInterval<O>> mapRAIAndRAIToII = (
			in1, in2, op, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final RandomAccess<I1> aAccess = in1.randomAccess();
		final RandomAccess<I2> bAccess = in2.randomAccess();
		final Cursor<O> cCursor = mapped.localizingCursor();
		while (cCursor.hasNext()) {
			cCursor.fwd();
			aAccess.setPosition(cCursor);
			bAccess.setPosition(cCursor);
			op.compute(aAccess.get(), bAccess.get(), cCursor.get());
		}
	};

	// -- Parallel Unary Maps --

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer5<IterableInterval<I>, Computer<I, O>, Long, Long, Long, IterableInterval<O>> mapIIToIIParams = (
			in1,op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<I> aCursor = in1.cursor();
		final Cursor<O> bCursor = mapped.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			final long m = ctr == 0 ? startIndex + 1 : stepSize;
			aCursor.jumpFwd(m);
			bCursor.jumpFwd(m);
			op.compute(aCursor.get(), bCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer5<IterableInterval<I>, Computer<I, O>, Long, Long, Long, RandomAccessibleInterval<O>> mapIIToRAIParams = (
			in1, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<I> aCursor = in1.localizingCursor();
		final RandomAccess<O> bAccess = mapped.randomAccess();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			aCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			bAccess.setPosition(aCursor);
			op.compute(aCursor.get(), bAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer5<RandomAccessibleInterval<I>, Computer<I, O>, Long, Long, Long, IterableInterval<O>> mapRAIToIIParams = (
			in1, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final RandomAccess<I> aAccess = in1.randomAccess();
		final Cursor<O> bCursor = mapped.localizingCursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			bCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			aAccess.setPosition(bCursor);
			op.compute(aAccess.get(), bCursor.get());
		}
	};

	// -- Parallel Bi Maps --

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer6<IterableInterval<I1>, IterableInterval<I2>, BiComputer<I1, I2, O>, Long, Long, Long, IterableInterval<O>> mapIIAndIIToIParams = (
			in1, in2, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<I1> aCursor = in1.cursor();
		final Cursor<I2> bCursor = in2.cursor();
		final Cursor<O> cCursor = mapped.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			final long m = ctr == 0 ? startIndex + 1 : stepSize;
			aCursor.jumpFwd(m);
			bCursor.jumpFwd(m);
			cCursor.jumpFwd(m);
			op.compute(aCursor.get(), bCursor.get(), cCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer6<IterableInterval<I1>, IterableInterval<I2>, BiComputer<I1, I2, O>, Long, Long, Long, RandomAccessibleInterval<O>> mapIIAndIIToRAIParams = (
			in1, in2, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<I1> aCursor = in1.localizingCursor();
		final Cursor<I2> bCursor = in2.cursor();
		final RandomAccess<O> cAccess = mapped.randomAccess();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			final long m = ctr == 0 ? startIndex + 1 : stepSize;
			aCursor.jumpFwd(m);
			bCursor.jumpFwd(m);
			cAccess.setPosition(aCursor);
			op.compute(aCursor.get(), bCursor.get(), cAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer6<IterableInterval<I1>, RandomAccessibleInterval<I2>, BiComputer<I1, I2, O>, Long, Long, Long, IterableInterval<O>> mapIIAndRAIToIIParams = (
			in1, in2, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<I1> aCursor = in1.localizingCursor();
		final RandomAccess<I2> bAccess = in2.randomAccess();
		final Cursor<O> cCursor = mapped.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			final long m = ctr == 0 ? startIndex + 1 : stepSize;
			aCursor.jumpFwd(m);
			cCursor.jumpFwd(m);
			bAccess.setPosition(aCursor);
			op.compute(aCursor.get(), bAccess.get(), cCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer6<RandomAccessibleInterval<I1>, IterableInterval<I2>, BiComputer<I1, I2, O>, Long, Long, Long, IterableInterval<O>> mapRAIAndIIToIIParams = (
			in1, in2, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final RandomAccess<I1> aAccess = in1.randomAccess();
		final Cursor<I2> bCursor = in2.localizingCursor();
		final Cursor<O> cCursor = mapped.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			final long m = ctr == 0 ? startIndex + 1 : stepSize;
			bCursor.jumpFwd(m);
			cCursor.jumpFwd(m);
			aAccess.setPosition(bCursor);
			op.compute(aAccess.get(), bCursor.get(), cCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer6<IterableInterval<I1>, RandomAccessibleInterval<I2>, BiComputer<I1, I2, O>, Long, Long, Long, RandomAccessibleInterval<O>> mapIIAndRAIToRAIParams = (
			in1, in2, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<I1> aCursor = in1.localizingCursor();
		final RandomAccess<I2> bAccess = in2.randomAccess();
		final RandomAccess<O> cAccess = mapped.randomAccess();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			aCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			bAccess.setPosition(aCursor);
			cAccess.setPosition(aCursor);
			op.compute(aCursor.get(), bAccess.get(), cAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer6<RandomAccessibleInterval<I1>, IterableInterval<I2>, BiComputer<I1, I2, O>, Long, Long, Long, RandomAccessibleInterval<O>> mapRAIAndIIToRAIParams = (
			in1, in2, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final RandomAccess<I1> aAccess = in1.randomAccess();
		final Cursor<I2> bCursor = in2.localizingCursor();
		final RandomAccess<O> cAccess = mapped.randomAccess();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			bCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			aAccess.setPosition(bCursor);
			cAccess.setPosition(bCursor);
			op.compute(aAccess.get(), bCursor.get(), cAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	public final Computer6<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, BiComputer<I1, I2, O>, Long, Long, Long, IterableInterval<O>> mapRAIAndRAIToIIParams = (
			in1, in2, op, startIndex, stepSize, numSteps, mapped) -> {
		if (!Maps.compatible(in1, in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final RandomAccess<I1> aAccess = in1.randomAccess();
		final RandomAccess<I2> bAccess = in2.randomAccess();
		final Cursor<O> cCursor = mapped.localizingCursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			cCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			aAccess.setPosition(cCursor);
			bAccess.setPosition(cCursor);
			op.compute(aAccess.get(), bAccess.get(), cCursor.get());
		}
	};

	// -- Unary Inplace Maps --

	@OpField(names = "map")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	public final BiInplaceFirst<Iterable<I>, Inplace<I>> mapInplace = (arg, op) -> {
		for (final I e : arg)
			op.mutate(e);
	};

	@OpField(names = "map")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	public final Inplace5First<IterableInterval<I>, Inplace<I>, Long, Long, Long> mapInplaceParams = (arg, op,
			startIndex, stepSize, numSteps) -> {
		if (numSteps <= 0)
			return;
		final Cursor<I> argCursor = arg.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			argCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			op.mutate(argCursor.get());
		}
	};

	// -- Bi Inplace Maps --

	@OpField(names = "map", priority = Priority.HIGH)
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "op")
	public final Inplace3First<IterableInterval<IO>, IterableInterval<I2>, BiInplaceFirst<IO, I2>> mapIIToIIInplaceFirst = (
			mapped, in2, op) -> {
		if (!Maps.compatible(in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<IO> mappedCursor = mapped.cursor();
		final Cursor<I2> inCursor = in2.cursor();
		while (inCursor.hasNext()) {
			op.mutate(mappedCursor.next(), inCursor.next());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	public final Inplace3Second<IterableInterval<I2>, IterableInterval<IO>, BiInplaceSecond<I2, IO>> mapIIToIIInplaceSecond = (
			in1, mapped, op) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<I2> inCursor = in1.cursor();
		final Cursor<IO> mappedCursor = mapped.cursor();
		while (inCursor.hasNext()) {
			op.mutate(inCursor.next(), mappedCursor.next());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "op")
	public final Inplace3First<IterableInterval<IO>, RandomAccessibleInterval<I2>, BiInplaceFirst<IO, I2>> mapIIToRAIInplaceFirst = (
			mapped, in2, op) -> {
		if (!Maps.compatible(in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<IO> mappedCursor = mapped.localizingCursor();
		final RandomAccess<I2> inAccess = in2.randomAccess();
		while (mappedCursor.hasNext()) {
			mappedCursor.fwd();
			inAccess.setPosition(mappedCursor);
			op.mutate(mappedCursor.get(), inAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	public final Inplace3Second<IterableInterval<I1>, RandomAccessibleInterval<IO>, BiInplaceSecond<I1, IO>> mapIIToRAIInplaceSecond = (
			in1, mapped, op) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final Cursor<I1> inCursor = in1.localizingCursor();
		final RandomAccess<IO> mappedAccess = mapped.randomAccess();
		while (inCursor.hasNext()) {
			inCursor.fwd();
			mappedAccess.setPosition(inCursor);
			op.mutate(inCursor.get(), mappedAccess.get());
		}
	};

	@OpField(names = "map", priority = Priority.LOW)
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "op")
	public final Inplace3First<RandomAccessibleInterval<IO>, IterableInterval<I2>, BiInplaceFirst<IO, I2>> mapRAIToIIInplaceFirst = (
			mapped, in2, op) -> {
		if (!Maps.compatible(in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final RandomAccess<IO> mappedAccess = mapped.randomAccess();
		final Cursor<I2> inCursor = in2.localizingCursor();
		while (inCursor.hasNext()) {
			inCursor.fwd();
			mappedAccess.setPosition(inCursor);
			op.mutate(mappedAccess.get(), inCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	public final Inplace3First<RandomAccessibleInterval<I1>, IterableInterval<IO>, BiInplaceSecond<I1, IO>> mapRAIToIIInplaceSecond = (
			in1, mapped, op) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		final RandomAccess<I1> inCursor = in1.randomAccess();
		final Cursor<IO> mappedCursor = mapped.localizingCursor();
		while (mappedCursor.hasNext()) {
			mappedCursor.fwd();
			inCursor.setPosition(mappedCursor);
			op.mutate(inCursor.get(), mappedCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	public final Inplace6First<IterableInterval<IO>, IterableInterval<I2>, BiInplaceFirst<IO, I2>, Long, Long, Long> mapIIToIIInplaceFirstParams = (
			mapped, in2, op, startIndex, stepSize, numSteps) -> {
		if (!Maps.compatible(in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<IO> mappedCursor = mapped.cursor();
		final Cursor<I2> inCursor = in2.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			final long m = ctr == 0 ? startIndex + 1 : stepSize;
			inCursor.jumpFwd(m);
			inCursor.jumpFwd(m);
			op.mutate(mappedCursor.get(), inCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	public final Inplace6Second<IterableInterval<I1>, IterableInterval<IO>, BiInplaceSecond<I1, IO>, Long, Long, Long> mapIIToIIInplaceSecondParams = (
			in1, mapped, op, startIndex, stepSize, numSteps) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<I1> inCursor = in1.cursor();
		final Cursor<IO> mappedCursor = mapped.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			final long m = ctr == 0 ? startIndex + 1 : stepSize;
			inCursor.jumpFwd(m);
			inCursor.jumpFwd(m);
			op.mutate(inCursor.get(), mappedCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	public final Inplace6First<IterableInterval<IO>, RandomAccessibleInterval<I2>, BiInplaceFirst<IO, I2>, Long, Long, Long> mapIIToRAIInplaceFirstParams = (
			mapped, in2, op, startIndex, stepSize, numSteps) -> {
		if (!Maps.compatible(in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<IO> mappedCursor = mapped.localizingCursor();
		final RandomAccess<I2> inAccess = in2.randomAccess();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			mappedCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			inAccess.setPosition(mappedCursor);
			op.mutate(mappedCursor.get(), inAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	public final Inplace6Second<IterableInterval<I1>, RandomAccessibleInterval<IO>, BiInplaceSecond<I1, IO>, Long, Long, Long> mapIIToRAIInplaceSecondParams = (
			in1, mapped, op, startIndex, stepSize, numSteps) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final Cursor<I1> inCursor = in1.localizingCursor();
		final RandomAccess<IO> mappedAccess = mapped.randomAccess();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			inCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			mappedAccess.setPosition(inCursor);
			op.mutate(inCursor.get(), mappedAccess.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "in1")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	public final Inplace6Second<RandomAccessibleInterval<I1>, IterableInterval<IO>, BiInplaceSecond<I1, IO>, Long, Long, Long> mapRAIToIIInplaceSecondParams = (
			in1, mapped, op, startIndex, stepSize, numSteps) -> {
		if (!Maps.compatible(in1, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final RandomAccess<I1> inAccess = in1.randomAccess();
		final Cursor<IO> mappedCursor = mapped.cursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			mappedCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			inAccess.setPosition(mappedCursor);
			op.mutate(inAccess.get(), mappedCursor.get());
		}
	};

	@OpField(names = "map")
	@Parameter(key = "mapped", type = ItemIO.BOTH)
	@Parameter(key = "in2")
	@Parameter(key = "op")
	@Parameter(key = "startIndex")
	@Parameter(key = "stepSize")
	@Parameter(key = "numSteps")
	public final Inplace6First<RandomAccessibleInterval<IO>, IterableInterval<I2>, BiInplaceFirst<IO, I2>, Long, Long, Long> mapRAIToIIInplaceFirstParams = (
			mapped, in2, op, startIndex, stepSize, numSteps) -> {
		if (!Maps.compatible(in2, mapped))
			throw new IllegalArgumentException("Input intervals must have matching iteration orders!");
		if (numSteps <= 0)
			return;
		final RandomAccess<IO> mappedAccess = mapped.randomAccess();
		final Cursor<I2> inCursor = in2.localizingCursor();

		for (long ctr = 0; ctr < numSteps; ctr++) {
			inCursor.jumpFwd(ctr == 0 ? startIndex + 1 : stepSize);
			mappedAccess.setPosition(inCursor);
			op.mutate(mappedAccess.get(), inCursor.get());
		}
	};
}
