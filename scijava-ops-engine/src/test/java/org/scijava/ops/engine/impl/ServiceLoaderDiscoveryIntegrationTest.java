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

package org.scijava.ops.engine.impl;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpInfoGenerator;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;

public class ServiceLoaderDiscoveryIntegrationTest {

	@Test
	public void opDiscoveryRegressionIT() {
		final Discoverer d = Discoverer.using(ServiceLoader::load);
		final List<Op> discoveries = d.discover(Op.class);
		Assertions.assertEquals(236, discoveries.size());

		@SuppressWarnings("unused")
		final OpInfoGenerator g = new OpClassOpInfoGenerator();
		final List<OpInfo> infos = discoveries.stream() //
			.flatMap(c -> g.generateInfosFrom(c).stream()) //
			.collect(Collectors.toList());
		Assertions.assertEquals(236, infos.size());
	}

	@Test
	public void opCollectionDiscoveryRegressionIT() {
		final Discoverer d = Discoverer.using(ServiceLoader::load);
		final List<OpCollection> discoveries = d.discover(OpCollection.class);
		Assertions.assertEquals(22, discoveries.size());
		@SuppressWarnings("unused")
		final OpInfoGenerator g = new OpCollectionInfoGenerator();
		final List<OpInfo> infos = discoveries.stream() //
			.flatMap(c -> g.generateInfosFrom(c).stream()) //
			.collect(Collectors.toList());
		Assertions.assertEquals(300, infos.size());
	}

}
