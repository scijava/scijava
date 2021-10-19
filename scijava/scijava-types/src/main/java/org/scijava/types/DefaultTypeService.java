/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
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

package org.scijava.types;

import org.scijava.discovery.Discoverer;
import org.scijava.discovery.plugin.PluginBasedDiscoverer;
import org.scijava.log2.LogService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;

/**
 * Default {@link TypeService} implementation.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultTypeService extends
	AbstractSingletonService<TypeExtractor<?>> implements TypeService
{

	@Parameter
	private PluginService plugins;

	@Parameter
	private LogService logger;

	private TypeReifier reifier;

	private synchronized void generateReifier() {
		pluginService().getPluginsOfType(TypeExtractor.class);
		Discoverer d = new PluginBasedDiscoverer(plugins);
		if (reifier != null) return;
			reifier = new DefaultTypeReifier(logger, d);
	}

	@Override
	public TypeReifier reifier() {
		if (reifier == null) generateReifier();
		return reifier;
	}

	// -- TypeService methods --

	@Override
	public <T> TypeExtractor<T> getExtractor(final Class<T> c) {
		return reifier().getExtractor(c);
	}

}
