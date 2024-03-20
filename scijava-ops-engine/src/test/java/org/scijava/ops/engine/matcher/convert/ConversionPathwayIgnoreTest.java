
package org.scijava.ops.engine.matcher.convert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

import java.util.function.Function;

/**
 * A set of tests that ensure that {@code engine.copy} Ops are not called in Op
 * conversion when they are not necessary
 *
 * @author Gabriel Selzer
 */
public class ConversionPathwayIgnoreTest extends AbstractTestEnvironment
	implements OpCollection
{

	/** Helper Field to keep track of whether a copy Op is called */
	private static boolean COPIED = false;

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new IdentityCollection<>());
		ops.register(new UtilityConverters());
		ops.register(new ConversionPathwayIgnoreTest());
	}

	@BeforeEach
	public void reset() {
		COPIED = false;
	}

	@OpField(names = "test.a")
	public final Computers.Arity1<FocusedA, FocusedA> opA = (a1, o) -> o.set(a1
		.get() + 1);

	@Test
	public void testPathwaysNotIgnored() {
		FocusedA in = new FocusedA(0);
		FocusedB out = new FocusedB(0);
		ops.op("test.a").arity1().input(in).output(out).compute();
		Assertions.assertEquals(1, out.get());
		Assertions.assertTrue(COPIED);
	}

	@Test
	public void testPathwaysIgnored() {
		FocusedB in = new FocusedB(0);
		FocusedA out = new FocusedA(0);
		ops.op("test.a").arity1().input(in).output(out).compute();
		Assertions.assertEquals(1, out.get());
		Assertions.assertFalse(COPIED);
	}

	// -- engine meta-ops -- //

	@OpField(names = "engine.convert")
	public final Function<FocusedA, Simple> fromAConverter = //
		(a) -> new Simple(a.get());

	@OpField(names = "engine.convert")
	public final Function<FocusedB, Simple> fromBConverter = //
		(b) -> new Simple(b.get());

	@OpField(names = "engine.convert")
	public final Function<Simple, FocusedA> toAConverter = //
		(s) -> new FocusedA(s.get());

	@OpField(names = "engine.convert")
	public final Function<Simple, FocusedB> toBConverter = //
		(s) -> new FocusedB(s.get());

	@OpField(names = "engine.copy")
	public final Computers.Arity1<FocusedA, FocusedA> aCopier = (a1, a2) -> {
		COPIED = true;
		a2.set(a1.get());
	};

	@OpField(names = "engine.copy")
	public final Computers.Arity1<FocusedB, FocusedB> bCopier = (b1, b2) -> {
		COPIED = true;
		b2.set(b1.get());
	};

	@OpField(names = "engine.copy")
	public final Computers.Arity1<Simple, Simple> sCopier = (s1, s2) -> {
		COPIED = true;
		s2.set(s1.get());
	};

	// -- Helper classes -- //

	public static abstract class Base {

		int x;

		public Base(int x) {
			this.set(x);
		}

		public int get() {
			return x;
		}

		public void set(int x) {
			this.x = x;
		}

	}

	public static class FocusedA extends Base {

		public FocusedA(int x) {
			super(x);
		}
	}

	public static class FocusedB extends Base {

		public FocusedB(int x) {
			super(x);
		}
	}

	public static class Simple extends Base {

		public Simple(int x) {
			super(x);
		}
	}
}
