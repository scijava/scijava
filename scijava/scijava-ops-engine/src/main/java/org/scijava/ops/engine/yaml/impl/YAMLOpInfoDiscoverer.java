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

package org.scijava.ops.engine.yaml.impl;

import static org.scijava.ops.engine.yaml.YAMLUtils.subMap;
import static org.scijava.ops.engine.yaml.YAMLUtils.value;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.scijava.common3.Classes;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.yaml.YAMLOpInfoCreator;
import org.yaml.snakeyaml.Yaml;

/**
 * A {@link Discoverer} implementation that can discover {@link OpInfo}s from
 * YAML.
 *
 * @author Gabriel Selzer
 */
public class YAMLOpInfoDiscoverer implements Discoverer {

	private final Yaml yaml = new Yaml();

	private final List<YAMLOpInfoCreator> creators = Discoverer.using(
		ServiceLoader::load).discover(YAMLOpInfoCreator.class);

	@SuppressWarnings("unchecked")
	@Override
	public <U> List<U> discover(Class<U> c) {
		// We only discover OpInfos
		if (!c.equals(OpInfo.class)) return Collections.emptyList();
		// Load all YAML files
		Enumeration<URL> opFiles = getOpYAML();
		// Parse each YAML file
		List<OpInfo> opInfos = new ArrayList<>();
		Collections.list(opFiles).stream().distinct().forEach(opFile -> {
			try {
				parse(opInfos, opFile);
			}
			catch (IOException e) {
				throw new IllegalArgumentException( //
					"Could not read Op YAML file " + opFile.toString() + ": ", //
					e);
			}
		});
		return (List<U>) opInfos;
	}

	/**
	 * Convenience method to hide IOException
	 * 
	 * @return an {@link Enumeration} of YAML files.
	 */
	private Enumeration<URL> getOpYAML() {
		try {
			return Classes.classLoader().getResources("op.yaml");
		}
		catch (IOException e) {
			throw new RuntimeException("Could not load Op YAML files!", e);
		}
	}

	private void parse(List<OpInfo> infos, final URL url)
			throws IOException
	{
		List<Map<String, Object>> yamlData = yaml.load(url.openStream());

		for (Map<String, Object> op : yamlData)
		{
			Map<String, Object> opData = subMap(op, "op");
			String identifier = value(opData, "source");
			try {
				URI uri = new URI(identifier);
				Optional<YAMLOpInfoCreator> c = creators.stream() //
					.filter(f -> f.canCreateFrom(uri)) //
					.findFirst();
				if (c.isPresent()) infos.add(c.get().create(uri, opData));
			}
			catch (Exception e) {
				// TODO: Use SciJava Log2's Logger to notify the user.
				// See https://github.com/scijava/scijava/issues/106 for discussion
				// and progress
				throw new IllegalArgumentException(e);
			}
		}
	}

}
