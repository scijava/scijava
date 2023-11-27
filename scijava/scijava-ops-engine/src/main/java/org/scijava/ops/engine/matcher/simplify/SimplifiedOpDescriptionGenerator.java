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

package org.scijava.ops.engine.matcher.simplify;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.OpDescriptionGenerator;
import org.scijava.ops.engine.util.Infos;
import org.scijava.priority.Priority;

/**
 * An {@link OpDescriptionGenerator} implementation which makes use of
 * {@link SimplifiedOpInfo}s to provide "simple" descriptions.
 * 
 * @author Gabriel Selzer
 */
public class SimplifiedOpDescriptionGenerator implements
	OpDescriptionGenerator
{

	@Override
	public String simpleDescriptions(OpEnvironment env, OpRequest req) {
		String name = req.getName();
		if (name == null) {
			return allNamespaces(env);
		}
		if (env.infos(name).isEmpty()) {
			return allNamespaces(env, name);
		}
		var infos = SimplificationMatchingRoutine.getSimpleInfos(env, req.getName());
		var filtered = filterInfos(infos, req);
		String opString = filtered.stream() //
				.map(Infos::describe) //
				.map(s -> s.replaceAll("\n", "\n\t")) //
				.distinct() //
				.collect(Collectors.joining("\n\t> "));
		if (opString.isEmpty()) return "No Ops found matching this request.";
		return "Ops:\n\t> " + opString;
	}

	@Override
	public String verboseDescriptions(OpEnvironment env, OpRequest req) {
		String name = req.getName();
		if (name == null) {
			return allNamespaces(env);
		}
		if (env.infos(name).isEmpty()) {
			return allNamespaces(env, name);
		}
		var infos = env.infos(req.getName());
		var filtered = filterInfos(infos, req);
		String opString = filtered.stream() //
				.map(Infos::describeVerbose) //
				.map(s -> s.replaceAll("\n", "\n\t")) //
				.distinct() //
				.collect(Collectors.joining("\n\t> "));
		if (opString.isEmpty()) return "No Ops found matching this request.";
		return "Ops:\n\t> " + opString;
	}

	private String allNamespaces(final OpEnvironment env) {
		List<String> namespaces = env.infos().stream() //
				// Get all names from each Op
				.flatMap(info -> info.names().stream()) //
				// Map each name to its namespace
				.map(name -> name.contains(".") ?  name.substring(0, name.indexOf(".")) : name) //
				// Deduplicate, sort & collect
				.distinct() //
				.sorted() //
				.collect(Collectors.toList());
		return "Namespaces:\n\t> " + String.join("\n\t> ", namespaces);
	}

	private String allNamespaces(final OpEnvironment env, final String name) {
		List<String> namespaces = env.infos().stream() //
				// Get all names from each Op
				.flatMap(info -> info.names().stream()) //
				// Deduplicate, sort & collect
				.distinct() //
				.filter(n -> n.contains(name)) //
				.sorted() //
				.collect(Collectors.toList());
		return "Namespaces:\n\t> " + String.join("\n\t> ", namespaces);

	}

	private List<OpInfo> filterInfos(Iterable<? extends OpInfo> infos, OpRequest req) {
		List<OpInfo> filtered = new ArrayList<>();
		for (var info: infos) {
			if (req.getArgs() == null || req.getArgs().length == info.inputTypes().size()) {
				filtered.add(info);
			}
		}
		return filtered;
	}

	@Override
	public double getPriority() {
		return Priority.VERY_HIGH;
	}
}
