
package org.scijava.ops.engine;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.Ops;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.priority.Priority;

public class InfoTreeGeneratorTest extends AbstractTestEnvironment implements
	OpCollection
{

	@OpField(names = "test.infoTreeGeneration")
	public final Function<Double, Double> foo = in -> in + 1;

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new DummyInfoTreeGenerator(), new InfoTreeGeneratorTest());
	}

	@Test
	public void testMultipleValidInfoTreeGenerators() {
		// Obtain a signature that can be reified by the DefaultInfoTreeGenerator
		// and by our dummy generator
		var op = ops.unary("test.infoTreeGeneration").inType(Double.class).outType(
			Double.class).function();
		String signature = Ops.signature(op);
		// Run treeFromID to make sure our generator doesn't run
		var infoTree = ops.treeFromID(signature);
		// Assert non-null output
		Assertions.assertNotNull(infoTree);
	}

	static class DummyInfoTreeGenerator implements InfoTreeGenerator {

		@Override
		public InfoTree generate(OpEnvironment env, String signature,
			Map<String, OpInfo> idMap, Collection<InfoTreeGenerator> generators)
		{
			return null;
		}

		@Override
		public boolean canGenerate(String signature) {
			return true;
		}

		@Override
		public double getPriority() {
			return Priority.LOW;
		}

	}
}
