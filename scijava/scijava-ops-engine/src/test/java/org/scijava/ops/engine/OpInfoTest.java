
package org.scijava.ops.engine;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;

public class OpInfoTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new IntListAdder());
		ops.register(new DoubleListAdder());
	}

	public static abstract class AbstractListAdder<T> implements
		Computers.Arity1<T, List<T>>, Op
	{

		@Override
		public void compute(T in, @Container List<T> out) {
			out.add(in);
		}
	}

	@OpClass(names = "test.opinfo.listAdder")
	public static class IntListAdder extends AbstractListAdder<Integer> {}

	@OpClass(names = "test.opinfo.listAdder")
	public static class DoubleListAdder extends AbstractListAdder<Double> {}

	@Test
	public void testOpInfoDescriptions() {
		Iterator<OpInfo> infos = ops.infos("test.opinfo.listAdder").iterator();
		while (infos.hasNext()) {
			infos.next().toString();
		}
	}
}
