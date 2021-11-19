package org.scijava.ops.matcher;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * Tests op matcher functionality relating to {@link Types#greatestCommonSuperType}.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class MatchingWithGCSTTest extends AbstractTestEnvironment {

	interface Thing {

	}

	interface Stuff {

	}

	static class XThing extends Base implements Thing {

	}

	static class Base {

	}

	static class NThing extends Base implements Thing, Stuff {

	}

	static class QThing extends YThing implements Stuff {

	}

	static class YThing implements Thing {

	}

	@OpField(names = "test.listTypeReification")
	public static final Function<List<? extends Thing>, List<Double>> fooOP = (in) -> {
		List<Double> returnList = new ArrayList<>();
		returnList.add(0.);
		return returnList;
	};

	@Test
	public void OpMatchingIntegrationTest() {
		List<Thing> things = new ArrayList<>();
		things.add(new NThing());
		things.add(new XThing());
		things.add(new YThing());
		List<Double> actual = fooOP.apply(things);
		// N.B. The type reifier reifies this list to a List<Thing>
		List<Double> expected = ops.op("test.listTypeReification").input(things)
				.outType(new Nil<List<Double>>() {}).apply();
		assertEquals(actual, expected);
	}
}
