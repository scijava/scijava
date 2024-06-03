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

import java.util.Collections;
import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Producer;
import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

/**
 * Tests the functionality of {@link InfoTree} instantiation.
 *
 * @author Gabriel Selzer
 */
public class InfoTreeTest extends AbstractTestEnvironment implements
	OpCollection
{

	private static InfoTree simpleTree;

	private static InfoTree complexTree;

	@BeforeAll
	public static void additionalSetup() {
		// add needed ops
		ops.register(new InfoTreeTest());
		ops.register(new ComplexOp());

		// get simple InfoTree
		OpInfo info = singularInfoOfName("test.infoTree");
		simpleTree = new InfoTree(info);

		// get dependent InfoTree
		OpInfo complexInfo = singularInfoOfName("test.infoTreeBase");
		complexTree = new InfoTree( //
			complexInfo, //
			Collections.singletonList(simpleTree) //
		);
	}

	public static final String S = "this Op is cool";

	@OpField(names = "test.infoTree")
	public final Producer<String> foo = () -> S;

	@Test
	public void testInfoTreeInstantiation() {
		Nil<Producer<String>> nil = new Nil<>() {};
		Producer<String> op = ops.opFromInfoTree(simpleTree, nil);
		Assertions.assertEquals(S, op.create());

		// Find dependency
		nil = new Nil<>() {};
		op = ops.opFromInfoTree(complexTree, nil);
		Assertions.assertEquals(S, op.create());
	}

	@Test
	public void testInfoTreeToString() {
		Assertions.assertEquals( //
			simpleTree.info().implementationName(), //
			simpleTree.toString() //
		);

		StringBuilder sb = new StringBuilder();
		sb.append(complexTree.info().implementationName());
		sb.append(InfoTree.DEPENDENCY_DELIM);
		sb.append(complexTree.dependencies().get(0).info().implementationName());
		Assertions.assertEquals( //
			sb.toString(), //
			complexTree.toString() //
		);
	}

	private static OpInfo singularInfoOfName(String name) {
		Iterator<OpInfo> infos = ops.infos(name).iterator();
		Assertions.assertTrue(infos.hasNext());
		OpInfo info = infos.next();
		Assertions.assertFalse(infos.hasNext());
		return info;
	}

}

@OpClass(names = "test.infoTreeBase")
class ComplexOp implements Producer<String>, Op {

	@OpDependency(name = "test.infoTree")
	private Producer<String> op;

	@Override
	public String create() {
		return op.create();
	}

}
