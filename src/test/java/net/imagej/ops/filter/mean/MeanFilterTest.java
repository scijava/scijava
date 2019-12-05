package net.imagej.ops.filter.mean;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.integer.ByteType;

import org.junit.Test;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.ops.types.Nil;

public class MeanFilterTest extends AbstractOpTest{
	
	@Test
	public void meanFilterTest() {
		
		Img<ByteType> img = op("create.img").input(new FinalInterval(5, 5), new ByteType()).outType(new Nil<Img<ByteType>>() {}).apply();
		RectangleShape shape = new RectangleShape(1, false);
		OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>> oobf = new OutOfBoundsBorderFactory<>();
		Img<ByteType> output = op("create.img").input(img).outType(new Nil<Img<ByteType>>() {}).apply();
		op("filter.mean").input(img, shape, oobf).output(output).compute();
		
	}

}
