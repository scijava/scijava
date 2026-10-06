/*-
 * #%L
 * Language Server Protocol bridge for SciJava code intelligence.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.code.lsp;

import java.util.HashMap;
import java.util.Map;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default implementation of {@link LspService}.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = Service.class)
public class DefaultLspService extends AbstractService implements LspService {

	/** How long a server may go unused before it stops, in milliseconds. */
	private static final long IDLE = Long.getLong("scijava.lsp.idle", 10 * 60 *
		1000);

	/**
	 * How many servers of a language may run at once (one per environment
	 * scripts run in).
	 */
	private static final int MAX = Integer.getInteger("scijava.lsp.max", 4);

	@Parameter
	private PluginService pluginService;

	@Parameter
	private LogService log;

	private final Map<String, LspServers> servers = new HashMap<>();

	@Override
	public synchronized LspServers servers(final String languageId) {
		if (servers.containsKey(languageId)) return servers.get(languageId);
		LspServers s = null;
		for (final PluginInfo<LspServerPlugin> info : pluginService
			.getPluginsOfType(LspServerPlugin.class))
		{
			final LspServerPlugin plugin = pluginService.createInstance(info);
			if (plugin != null && plugin.supports(languageId)) {
				s = new LspServers(plugin.spec(), log, IDLE, MAX);
				break;
			}
		}
		servers.put(languageId, s);
		return s;
	}

	@Override
	public synchronized void dispose() {
		for (final LspServers s : servers.values()) {
			if (s != null) s.shutdown();
		}
		servers.clear();
	}
}
