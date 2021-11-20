package net.imagej.ops2.morphology;

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
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

/**
 * Wraps all of <a href=
 * "https://github.com/imglib/imglib2-algorithm/tree/master/src/main/java/net/imglib2/algorithm/morphology/table2d">imglib2-algorithm's
 * table2D</a> algorithms
 *
 * @author Gabriel Selzer
 *
 * @param <B>
 *            - any Type extending {@link BooleanType}
 */
@Plugin(type = OpCollection.class)
public class Table2Ds<B extends BooleanType<B>> {

	@OpField(names = "morphology.branchpoints", params = "input, result")
	public final Function<Img<B>, Img<B>> branchPointsFunc = Branchpoints::branchpoints;

	@OpField(names = "morphology.branchpoints", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> branchPointsComputer = Branchpoints::branchpoints;

	@OpField(names = "morphology.bridge", params = "input, result")
	public final Function<Img<B>, Img<B>> bridgeFunc = Bridge::bridge;

	@OpField(names = "morphology.bridge", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> bridgeComputer = Bridge::bridge;

	@OpField(names = "morphology.clean", params = "input, result")
	public final Function<Img<B>, Img<B>> cleanFunc = Clean::clean;

	@OpField(names = "morphology.clean", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> cleanComputer = Clean::clean;

	@OpField(names = "morphology.endpoints", params = "input, result")
	public final Function<Img<B>, Img<B>> endpointsFunc = Endpoints::endpoints;

	@OpField(names = "morphology.endpoints", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> endpointsComputer = Endpoints::endpoints;

	@OpField(names = "morphology.fill", params = "input, result")
	public final Function<Img<B>, Img<B>> fillFunc = Fill::fill;

	@OpField(names = "morphology.fill", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> fillComputer = Fill::fill;

	@OpField(names = "morphology.hbreak", params = "input, result")
	public final Function<Img<B>, Img<B>> hbreakFunc = Hbreak::hbreak;

	@OpField(names = "morphology.hbreak", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> hbreakComputer = Hbreak::hbreak;

	@OpField(names = "morphology.life", params = "input, result")
	public final Function<Img<B>, Img<B>> lifeFunc = Life::life;

	@OpField(names = "morphology.life", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> lifeComputer = Life::life;

	@OpField(names = "morphology.majority", params = "input, result")
	public final Function<Img<B>, Img<B>> majorityFunc = Majority::majority;

	@OpField(names = "morphology.majority", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> majorityComputer = Majority::majority;

	@OpField(names = "morphology.remove", params = "input, result")
	public final Function<Img<B>, Img<B>> removeFunc = Remove::remove;

	@OpField(names = "morphology.remove", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> removeComputer = Remove::remove;

	@OpField(names = "morphology.spur", params = "input, result")
	public final Function<Img<B>, Img<B>> spurFunc = Spur::spur;

	@OpField(names = "morphology.spur", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> spurComputer = Spur::spur;

	@OpField(names = "morphology.thicken", params = "input, result")
	public final Function<Img<B>, Img<B>> thickenFunc = Thicken::thicken;

	@OpField(names = "morphology.thicken", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> thickenComputer = Thicken::thicken;

	@OpField(names = "morphology.thin", params = "input, result")
	public final Function<Img<B>, Img<B>> thinFunc = Thin::thin;

	@OpField(names = "morphology.thin", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> thinComputer = Thin::thin;

	@OpField(names = "morphology.vbreak", params = "input, result")
	public final Function<Img<B>, Img<B>> vbreakFunc = Vbreak::vbreak;

	@OpField(names = "morphology.vbreak", params = "input, result")
	public final Computers.Arity1<RandomAccessible<B>, IterableInterval<B>> vbreakComputer = Vbreak::vbreak;

}
