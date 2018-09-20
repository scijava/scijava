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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.ops.core.Op;
import org.scijava.ops.matcher.MatchingResult;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.matcher.OpTypeMatchingService;
import org.scijava.ops.util.Inject;
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
	private OpTypeMatchingService matcher;

	@Parameter
	private LogService log;

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

	public <T> StructInstance<T> findOpInstance(final Class<? extends Op> opClass, final Nil<T> specialType,
			final Nil<?>[] inTypes, final Nil<?>[] outTypes, final Object... secondaryArgs) {
		// FIXME - multiple output types? We will need to generalize this.
		final OpRef ref = OpRef.fromTypes(merge(opClass, toTypes(specialType)), toTypes(outTypes), toTypes(inTypes));

		// Find single match which matches the specified types
		@SuppressWarnings("unchecked")
		final StructInstance<T> op = (StructInstance<T>) findTypeMatch(ref).createOp();

		// Inject the secondary args if there are any
		if (Inject.Structs.isInjectable(op)) {
			if (secondaryArgs.length != 0) {
				Inject.Structs.inputs(op, secondaryArgs);
			} else {
				log.warn(
						"Specified Op has secondary args however no secondary args are given. Op execution may lead to errors.");
			}
		} else if (secondaryArgs.length > 0) {
			log.warn(
					"Specified Op has no secondary args however secondary args are given. The specified args will not be injected.");
		}
		return op;
	}

	public <T> T findOp(final Class<? extends Op> opClass, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?>[] outTypes, final Object... secondaryArgs) {
		return findOpInstance(opClass, specialType, inTypes, outTypes, secondaryArgs).object();
	}

	public <T> T findOp(final Class<? extends Op> opClass, final Nil<T> specialType, final Nil<?>[] inTypes,
			final Nil<?> outType, final Object... secondaryArgs) {
		return findOpInstance(opClass, specialType, inTypes, new Nil[] { outType }, secondaryArgs).object();
	}

	public MatchingResult findTypeMatches(final OpRef ref) {
		return matcher.findMatch(this, ref);
	}

	public OpCandidate findTypeMatch(final OpRef ref) {
		return findTypeMatches(ref).singleMatch();
	}

	private Type[] merge(Type in1, Type... ins) {
		Type[] merged = new Type[ins.length + 1];
		merged[0] = in1;
		for (int i = 0; i < ins.length; i++) {
			merged[i + 1] = ins[i];
		}
		return merged;
	}

	private Type[] toTypes(Nil<?>... nils) {
		return Arrays.stream(nils).filter(n -> n != null).map(n -> n.getType()).toArray(Type[]::new);
	}
}
