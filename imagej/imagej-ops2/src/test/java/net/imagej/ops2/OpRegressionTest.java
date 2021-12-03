package net.imagej.ops2;

import org.junit.jupiter.api.Test;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpRegressionTest extends AbstractOpTest {

	@Test
	public void opDiscoveryRegressionIT() {
		long expected = 1314;
		long actual = StreamSupport.stream(ops.infos().spliterator(), false).count();
		assertEquals(expected, actual);
	}

}
