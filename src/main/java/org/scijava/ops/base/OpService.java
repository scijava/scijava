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
package org.scijava.ops.base;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.ops.Op;
import org.scijava.param.ValidityException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;
import org.scijava.struct.StructInstance;
import org.scijava.types.Nil;

/**
 * Service that manages ops.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class OpService extends AbstractService implements SciJavaService, OpEnvironment {

	@Parameter
	private PluginService pluginService;

	@Parameter
	private OpMatchingService matcher;

	@Parameter
	private LogService log;

	public StructInstance<?> op(OpRef ref) {
		final OpCandidate match = matcher.findMatch(this, ref);
		return match.createOp();
	}
	
	@Override
	public Collection<OpInfo> infos() {
		// TODO: Consider maintaining an efficient OpInfo data structure.
		final ArrayList<OpInfo> infos = new ArrayList<>();
		for (final PluginInfo<Op> info : pluginService.getPluginsOfType(Op.class)) {
			try {
				final Class<? extends Op> opClass = info.loadClass();
				infos.add(new OpInfo(opClass));
			}
			catch (InstantiableException | ValidityException exc) {
				// TODO: Stop sucking at exception handling.
				log.error(exc);
			}
		}
		return infos;
	}
	
	public <T> StructInstance<T> findStructInstance(final Nil<T> opType, final List<Type> opAdditionalTypes, final Type[] inTypes,
			final Type[] outTypes) {
		// FIXME - createTypes does not support multiple additional types,
		// or multiple output types. We will need to generalize this.
		final OpRef ref = OpRef.fromTypes(opType.getType(), //
				opAdditionalTypes.get(0), outTypes[0], (Object[]) inTypes);
		@SuppressWarnings("unchecked")
		final StructInstance<T> op = (StructInstance<T>) op(ref);
		return op;
	}
	
	public <T> T findOp(final Nil<T> opType, final List<Type> opAdditionalTypes, final Type[] inTypes,
			final Type[] outTypes) {
		return findStructInstance(opType, opAdditionalTypes, inTypes, outTypes).object();
	}
}
