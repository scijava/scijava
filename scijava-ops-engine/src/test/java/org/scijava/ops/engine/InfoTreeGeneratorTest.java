/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
		var op = ops.op("test.infoTreeGeneration").inType(Double.class).outType(
			Double.class).function();
		String signature = Ops.signature(op);
		// Run treeFromSignature to make sure our generator doesn't run
		var infoTree = ops.treeFromSignature(signature);
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
		public double priority() {
			return Priority.LOW;
		}

	}
}
