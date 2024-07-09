/*-
 * #%L
 * Interoperability with legacy SciJava libraries.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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

package org.scijava.legacy.module.process;

import org.scijava.Priority;
import org.scijava.legacy.service.OpEnvironmentService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * A {@link PreprocessorPlugin} used to inject an {@link OpEnvironment}, created
 * by the {@link OpEnvironmentService}, into {@link Module}s.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.HIGH)
public class OpEnvironmentPreprocessorPlugin extends
	AbstractPreprocessorPlugin
{

	@Parameter
	private OpEnvironmentService opEnvironmentService;

	@Override
	public void process(Module module) {
		for (final var input : module.getInfo().inputs()) {
			if (!input.isAutoFill()) continue;
			if (module.isInputResolved(input.getName())) continue;
			final var type = input.getType();
			if (OpEnvironment.class.equals(type)) {
				@SuppressWarnings("unchecked")
				final var envInput =
					(ModuleItem<OpEnvironment>) input;
				envInput.setValue(module, opEnvironmentService.env());
				module.resolveInput(input.getName());
			}
		}
	}
}