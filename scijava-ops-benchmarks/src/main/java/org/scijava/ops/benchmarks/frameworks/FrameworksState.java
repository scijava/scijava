
package org.scijava.ops.benchmarks.frameworks;

import net.imagej.ops.OpService;
import org.openjdk.jmh.annotations.*;
import org.scijava.Context;
import org.scijava.annotations.Index;
import org.scijava.annotations.IndexItem;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Plugin;

/**
 * {@link State} used in SciJava Ops framework benchmarks
 *
 * @author Gabriel Selzer
 */
@State(Scope.Benchmark)
public class FrameworksState {

	public OpEnvironment env;
	public OpService ops;
	public byte[] in;

	private Context ctx;

	@Setup(Level.Invocation)
	public void setUpInvocation() {
		// Set up ImageJ Ops
		ctx = new Context(OpService.class);
		ops = ctx.getService(OpService.class);
		// Set up SciJava Ops
		env = OpEnvironment.build();
		// Create input data
		in = createRawData(4000, 4000);
	}

	@TearDown(Level.Invocation)
	public void tearDownInvocation() {
		ctx.dispose();
	}

	private byte[] createRawData(final int w, final int h) {
		final int size = w * h;
		final int max = w + h;
		final byte[] data = new byte[size];
		int index = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				data[index++] = (byte) (255 * (x + y) / max);
			}
		}
		return data;
	}
}
