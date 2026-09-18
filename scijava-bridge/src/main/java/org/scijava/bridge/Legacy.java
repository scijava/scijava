/*
 * #%L
 * Presenting SciJava Common's modules as SciJava3 commands.
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

package org.scijava.bridge;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.command3.CommandInfo;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;

/**
 * Everything SciJava Common knows how to run, as SciJava3 commands.
 * <p>
 * This is the bridge's front door, and it is one call:
 * </p>
 *
 * <pre>
 * final org.scijava.Context legacy = new org.scijava.Context();
 * final List&lt;CommandInfo&gt; commands = Legacy.commands(legacy);
 * </pre>
 * <p>
 * The result goes into an application's menus beside its own commands and its
 * scripts, which is what makes the migration incremental: a command is ported
 * when someone gets to it, and until then it runs through the bridge, in the
 * same menus and the same dialogs, and nobody has to notice.
 * </p>
 * <p>
 * NB: the caller owns the legacy context, because the caller knows which
 * services it wants and when to dispose of it. Two contexts exist while both
 * worlds do - one for each - which is the honest arrangement: they share no
 * services and neither can see the other's.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Legacy {

	private Legacy() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets everything the given SciJava Common context can run.
	 *
	 * @param context a SciJava Common context, which the caller owns
	 * @return its modules, as commands, in menu order
	 */
	public static List<CommandInfo> commands(final org.scijava.Context context) {
		final ModuleService modules = context.getService(ModuleService.class);
		if (modules == null) {
			throw new IllegalArgumentException(
				"The legacy context has no ModuleService");
		}
		return commands(modules.getModules(), context);
	}

	/**
	 * Gets the given modules as commands, in menu order.
	 *
	 * @param infos the modules to present
	 * @param context the legacy context they belong to
	 */
	public static List<CommandInfo> commands(
		final List<? extends ModuleInfo> infos, final org.scijava.Context context)
	{
		return infos.stream() //
			.map(info -> new LegacyCommandInfo(info, context)) //
			.sorted(Comparator.comparingDouble(CommandInfo::weight) //
				.thenComparing(CommandInfo::label)) //
			.collect(Collectors.toList());
	}
}
