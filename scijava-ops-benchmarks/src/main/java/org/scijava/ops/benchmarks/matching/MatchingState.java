
package org.scijava.ops.benchmarks.matching;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.real.DoubleType;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.scijava.ops.api.OpEnvironment;

/**
 * {@link State} used in SciJava Ops benchmarks
 *
 * @author Gabriel Selzer
 */
@State(Scope.Benchmark)
public class MatchingState {

	public OpEnvironment env;
	public Img<DoubleType> in;
	public Img<ByteType> simpleIn;

	@Setup(Level.Invocation)
	public void setUpInvocation() {
		env = OpEnvironment.build();
		in = ArrayImgs.doubles(1000, 1000);
		simpleIn = ArrayImgs.bytes(1000, 1000);
	}
}
