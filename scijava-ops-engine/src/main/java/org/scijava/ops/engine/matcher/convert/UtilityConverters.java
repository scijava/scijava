
package org.scijava.ops.engine.matcher.convert;

import org.scijava.function.Computers;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.spi.OpMethod;
import org.scijava.priority.Priority;

import java.util.function.Function;

/**
 * Ops useful in Op conversion.
 *
 * @author Gabriel Selzer
 */
public class UtilityConverters implements OpCollection {

	@OpHints(hints = { //
		BaseOpHints.Conversion.FORBIDDEN, //
		BaseOpHints.Adaptation.FORBIDDEN, //
		BaseOpHints.DependencyMatching.FORBIDDEN //
	})
	@OpMethod( //
		names = "engine.convert", //
		type = Function.class, //
		priority = Priority.LAST //
	)
	public static <T, U, V> V combo( //
		@OpDependency(name = "engine.convert") Function<T, U> c1, //
		@OpDependency(name = "engine.convert") Function<U, V> c2, //
		T in //
	) {
		return c2.apply(c1.apply(in));
	}

	@OpHints(hints = { //
		BaseOpHints.Conversion.FORBIDDEN, //
		BaseOpHints.Adaptation.FORBIDDEN, //
		BaseOpHints.DependencyMatching.FORBIDDEN //
	})
	@OpMethod( //
		names = "engine.copy", //
		type = Computers.Arity1.class, //
		priority = Priority.LAST //
	)
	public static <T, U, V> void comboCopier( //
		@OpDependency(name = "engine.convert") Function<T, U> c1, //
		@OpDependency(name = "engine.copy") Computers.Arity1<U, V> c2, //
		T in, //
		V out //
	) {
		c2.compute(c1.apply(in), out);
	}

}
