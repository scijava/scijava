/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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

package org.scijava.ops;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Producer;
import org.scijava.ops.impl.DefaultOpEnvironment;
import org.scijava.ops.matcher.OpRef;
import org.scijava.plugin.Plugin;

// TODO: assert multiple Ops in the cache when OpDependencies present.
@Plugin(type = OpCollection.class)
public class OpCachingTest extends AbstractTestEnvironment {

	/**
	 * Basic Op used to test cache functionality. NB we use an {@link OpField}
	 * here because we KNOW that we will only create ONE instance of this Op under
	 * the hood.
	 */
	@OpField(names = "test.basicOp")
	public final Producer<String> basicOp = () -> "This Op should be cached";

	@Test
	public void cacheOp() throws NoSuchFieldException, SecurityException,
		IllegalArgumentException, IllegalAccessException
	{
		// put the Op in the cache
		OpEnvironment opEnv = ops.env();
		if (!(opEnv instanceof DefaultOpEnvironment)) fail(
			"OpCachingTest expects a DefaultOpEnvironment (since it is testing the caching behavior of that class).");
		DefaultOpEnvironment defOpEnv = (DefaultOpEnvironment) opEnv;
		Producer<String> op = defOpEnv.op("test.basicOp").input().outType(
			String.class).producer();

		// use reflection to grab a hold of the opCache
		Field cacheField = defOpEnv.getClass().getDeclaredField("opCache");
		cacheField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<OpRef, Object> opCache = (Map<OpRef, Object>) cacheField.get(defOpEnv);

		// assert there is exactly one Op in the cache
		Assertions.assertEquals(opCache.size(), 1, 0);
		Assertions.assertEquals(op, opCache.values().iterator().next(),
			"Object in cache was not the same Object that was returned!");

		// assert that the same call to the matcher returns our Object
		OpRef cachedRef = opCache.keySet().iterator().next();
		String newString = "This Op invaded the cache!";
		Producer<String> newProducer = () -> newString;
		opCache.replace(cachedRef, newProducer);

		Producer<String> invadedOp = defOpEnv.op("test.basicOp").input().outType(
			String.class).producer();
		Assertions.assertEquals(invadedOp, newProducer,
			"Op returned did not match the Op inserted into the cache!");

	}

}
