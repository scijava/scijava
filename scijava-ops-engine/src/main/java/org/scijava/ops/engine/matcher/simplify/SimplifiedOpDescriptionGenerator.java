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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.OpDescriptionGenerator;
import org.scijava.ops.engine.matcher.reduce.ReducedOpInfo;
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
		Optional<String> nsString = getNamespaceString(env, name);
		if (nsString.isPresent()) return nsString.get();
		var infos = SimplificationMatchingRoutine.getInfos(env, name);
		return buildOpString(infos, req, Infos::describeOneLine);
	}

	@Override
	public String verboseDescriptions(OpEnvironment env, OpRequest req) {
		String name = req.getName();
		Optional<String> nsString = getNamespaceString(env, name);
		if (nsString.isPresent()) return nsString.get();
		var infos = env.infos(name);
		return buildOpString(infos, req, Infos::describeMultiLine);
	}

	private String buildOpString(Collection<OpInfo> infos, OpRequest req,
			Function<OpInfo, String> descriptionFunction)
	{
		var filtered = filterInfos(infos, req);
		String opString = filtered.stream() //
				.map(descriptionFunction) //
				.map(s -> s.replaceAll("\n", "\n\t")) //
				.distinct() //
				.collect(Collectors.joining("\n\t- "));
		if (opString.isEmpty()) return "No Ops found matching this request.";
		var key = "Key: *=container, ^=mutable";
		return req.getName() + ":\n\t- " + opString + "\n" + key;
	}

	private Optional<String> getNamespaceString(OpEnvironment env, String name) {
		if (name == null) {
			return Optional.of(allNamespaces(env));
		}
		if (env.infos(name).isEmpty()) {
			return Optional.of(allNamespaces(env, name));
		}
		return Optional.empty();
	}

	private String allNamespaces(final OpEnvironment env) {
		List<String> namespaces = namespaceStream(env) //
				.collect(Collectors.toList());
		return "Namespaces:\n\t> " + String.join("\n\t> ", namespaces);
	}

	private String allNamespaces(final OpEnvironment env, final String name) {
		List<String> namespaces = namespaceStream(env) //
				// Filter by the predicate name
				.filter(n -> n.startsWith(name)) //
				.collect(Collectors.toList());
		return "Names:\n\t> " + String.join("\n\t> ", namespaces);
	}

	private Stream<String> namespaceStream(OpEnvironment env) {
		return env.infos().stream() //
				// Get all names from each Op
				.flatMap(info -> info.names().stream()) //
				// Deduplicate & sort
				.distinct() //
				.sorted() //
				// Filter out the engine namespace
				.filter(ns -> !ns.startsWith("engine"));
	}

	private List<OpInfo> filterInfos(Iterable<? extends OpInfo> infos, OpRequest req) {
		List<OpInfo> filtered = new ArrayList<>();
		for (var info: infos) {
			if (info instanceof ReducedOpInfo) {
				continue;
			}

			var numPureInputs = info.inputs().stream() //
					.filter(m -> !m.isOutput()) //
					.count();

			if (req.getArgs() == null || req.getArgs().length == numPureInputs) {
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
