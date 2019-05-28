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

public class MeanFilterTest extends AbstractOpTest{
	
	@Test
	public void meanFilterTest() {
		
		Img<ByteType> img = (Img<ByteType>) ops().run("create.img", new FinalInterval(5, 5), new ByteType());
		RectangleShape shape = new RectangleShape(1, false);
		OutOfBoundsFactory<ByteType, RandomAccessibleInterval<ByteType>> oobf = new OutOfBoundsBorderFactory<>();
		Img<ByteType> output = (Img<ByteType>) ops().run("create.img", img);
		ops().run("filter.mean", img, shape, oobf, output);
		
	}

}
