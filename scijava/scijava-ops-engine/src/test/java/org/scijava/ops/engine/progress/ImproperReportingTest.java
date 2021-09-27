
package org.scijava.ops.engine.progress;

import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.function.Producer;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.plugin.Plugin;

/**
 * Tests that improper progress reporting results in failure
 *
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class ImproperReportingTest extends AbstractTestEnvironment {

	/**
	 * An Op that tries to update its progress without defining what that progress
	 * means
	 */
	@OpField(names = "test.updateWithoutSetMax")
	public final Function<Integer, int[]> arrayCreator = (size) -> {
		int[] arr = new int[size];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = 1;
			Progress.update();
		}
		return arr;
	};

	/**
	 * An Op that defines fewer stages than it completes
	 */
	@OpField(names = "test.defineTooFewStages")
	public final Producer<Integer> tooFewStageOp = () -> {
		Progress.defineTotalProgress(2);
		int totalStages = 3;
		for (int i = 0; i < totalStages; i++) {
			Progress.setStageMax(1);
			Progress.update();
		}
		return totalStages;
	};

	/**
	 * An Op that defines more stages than it completes
	 */
	@OpField(names = "test.defineTooManyStages")
	public final Producer<Integer> tooManyStageOp = () -> {
		Progress.defineTotalProgress(3);
		int totalStages = 2;
		for (int i = 0; i < totalStages; i++) {
			Progress.setStageMax(1);
			Progress.update();
		}
		return totalStages;
	};

	@OpMethod(names = "test.defineTooFewSubTasks", type = Producer.class)
	public static Integer tooFewSubTaskOp(@OpDependency(
		name = "test.progressReporter") Function<Integer, Integer> op)
	{
		Progress.defineTotalProgress(0, 2);
		return IntStream.range(0, 3) //
				.map(i -> op.apply(4)) //
				.sum();
	}

	/**
	 * An Op that defines more subtasks than 
	 * @param op
	 * @return
	 */
	@OpMethod(names = "test.defineTooManySubTasks", type = Producer.class)
	public static Integer tooManySubTaskOp(@OpDependency(
		name = "test.progressReporter") Function<Integer, Integer> op)
	{
		Progress.defineTotalProgress(0, 3);
		return IntStream.range(0, 2) //
				.map(i -> op.apply(4)) //
				.sum();
	}

	/**
	 * Tests that Ops who update progress without defining total progress result
	 * in a thrown error.
	 */
	@Test
	public void testUpdateWithoutSetMax() {
		Function<Integer, int[]> op = ops.op("test.updateWithoutSetMax").inType(
			Integer.class).outType(int[].class).function();
		Assert.assertThrows(IllegalStateException.class, () -> op.apply(3));
	}

	/**
	 * Tests that Ops who updated progress past the defined maximum result in a
	 * thrown error.
	 */
	@Test
	public void testDefineTooFewStages() {
		Producer<Integer> op = ops.op("test.defineTooFewStages").input().outType(
			Integer.class).producer();
		Assert.assertThrows(IllegalStateException.class, () -> op.create());
	}

	/**
	 * Tests that Ops who did not complete as many stages as they said they would
	 * results in a thrown error.
	 */
	@Test
	public void testDefineTooManyStages() {
		Producer<Integer> op = ops.op("test.defineTooManyStages").input().outType(
			Integer.class).producer();
		Assert.assertThrows(IllegalStateException.class, () -> op.create());
	}
	
	/**
	 * Tests that Ops who updated progress past the defined maximum result in a
	 * thrown error.
	 */
	@Test
	public void testDefineTooFewSubTasks() {
		Producer<Integer> op = ops.op("test.defineTooFewSubTasks").input().outType(
			Integer.class).producer();
		Assert.assertThrows(IllegalStateException.class, () -> op.create());
	}

	/**
	 * Tests that Ops who did not complete as many stages as they said they would
	 * results in a thrown error.
	 */
	@Test
	public void testDefineTooManySubTasks() {
		Producer<Integer> op = ops.op("test.defineTooManySubTasks").input().outType(
			Integer.class).producer();
		Assert.assertThrows(IllegalStateException.class, () -> op.create());
	}

}
