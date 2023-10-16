/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;

public abstract class AbstractTestEnvironment {

	protected static OpEnvironment ops;

	@BeforeAll
	public static void setUp() {
		ops = barebonesEnvironment();
	}

	@AfterAll
	public static void tearDown() {
		ops = null;
	}

	protected static <T> Optional<T> objFromNoArgConstructor(Class<T> c) {
		try {
			return Optional.of(c.getDeclaredConstructor().newInstance());
		} catch (Throwable t) {
			return Optional.empty();
		}
	}

	protected static Object[] objsFromNoArgConstructors(Class<?>[] arr) {
		return Arrays.stream(arr) //
				.map(AbstractTestEnvironment::objFromNoArgConstructor) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.toArray();
	}

	protected static OpEnvironment barebonesEnvironment() {
		Discoverer serviceLoading = Discoverer.using(ServiceLoader::load).except( //
			Op.class, //
			OpInfo.class, //
			OpCollection.class //
		);
		return OpEnvironment.getEnvironment(serviceLoading);
	}

	protected static boolean arrayEquals(double[] arr1, Double... arr2) {
		return Arrays.deepEquals(Arrays.stream(arr1).boxed().toArray(Double[]::new), arr2);
	}

	protected static <T> void assertIterationsEqual(final Iterable<T> expected, final Iterable<T> actual) {
		final Iterator<T> e = expected.iterator();
		final Iterator<T> a = actual.iterator();
		while (e.hasNext()) {
			assertTrue(a.hasNext(), "Fewer elements than expected");
			assertEquals(e.next(), a.next());
		}
		assertFalse(a.hasNext(), "More elements than expected");
	}

}
