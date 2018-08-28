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
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;
import org.scijava.struct.StructInstance;
import org.scijava.ops.types.Nil;

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
	private OpTypeMatchingService matcher;

	@Parameter
	private LogService log;

	public StructInstance<?> op(OpRef ref) {
		final MatchingResult match = matcher.findMatch(this, ref);
		return match.singleMatch().createOp();
	}
	
	@Override
	public Collection<OpInfo> infos() {
		// TODO: Consider maintaining an efficient OpInfo data structure.
		final ArrayList<OpInfo> infos = new ArrayList<>();
		for (final PluginInfo<Op> info : pluginService.getPluginsOfType(Op.class)) {
			try {
				final Class<? extends Op> opClass = info.loadClass();
				infos.add(new OpInfo(opClass));
			} catch (InstantiableException exc) {
				log.error("Can't load class from plugin info: " + info.toString(), exc);
			}
		}
		return infos;
	}

	public <T> StructInstance<T> findOpInstance(final Class<? extends Op> opClass, final Nil<T> specialType, final Type[] inTypes,
			final Type outType) {
		// FIXME - multiple output types? We will need to generalize this.
		final OpRef ref = OpRef.fromTypes(merge(opClass, specialType == null ? null : specialType.getType()), outType, inTypes);
		@SuppressWarnings("unchecked")
		final StructInstance<T> op = (StructInstance<T>) op(ref);
		return op;
	}
	
	public <O extends Op> StructInstance<O> findOpInstance(final Class<O> opClass, final Type[] inTypes, final Type outType) {
		return findOpInstance(opClass, null, inTypes, outType);
	}
	
	public <T> T findOp(final Class<? extends Op> opClass, final Nil<T> specialType, final Type[] inTypes,
			final Type outType) {
		return findOpInstance(opClass, specialType, inTypes, outType).object();
	}
	
	public <O extends Op> O findOp(final Class<O> opClass, final Type[] inTypes, final Type outType) {
		return findOpInstance(opClass, inTypes, outType).object();
	}
	
	private Type[] merge(Type in1, Type... ins) {
		Type[] merged = new Type[ins.length + 1];
		merged[0] = in1;
		for (int i = 0; i < ins.length; i++) {
			merged[i+1] = ins[i];
		}
		return merged;
	}
}
