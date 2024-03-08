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

package org.scijava.ops.engine.impl;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.OpDescriptionGenerator;
import org.scijava.ops.engine.matcher.convert.ConvertedOpInfo;
import org.scijava.ops.engine.matcher.reduce.ReducedOpInfo;
import org.scijava.ops.engine.util.Infos;
import org.scijava.priority.Priority;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * An {@link OpDescriptionGenerator} implementation which makes use of
 * {@link ConvertedOpInfo}s to provide "simple" descriptions.
 *
 * @author Gabriel Selzer
 */
public class DefaultOpDescriptionGenerator implements OpDescriptionGenerator {

	@Override
	public double getPriority() {
		return Priority.VERY_HIGH;
	}

	@Override
	public String simpleDescriptions(OpEnvironment env, OpRequest req) {
		String name = req.getName();
		Optional<String> nsString = getNonOpString(env, name);
		if (nsString.isPresent()) return nsString.get();
		var infos = env.infos(name);
		Function<OpInfo, String> descriptor = (info) -> {
			final StringBuilder sb = new StringBuilder("(");
			// Step 2: Inputs
			var inputs = info.inputs().stream().map(member -> {
				var str = "";
				var description = describeType(env, Nil.of(member.getType()));
				switch (member.getIOType()) {
					case INPUT:
						str += description;
						break;
					case MUTABLE:
						str += "@MUTABLE " + description;
						break;
					case CONTAINER:
						str += "@CONTAINER " + description;
						break;
					default:
						throw new IllegalArgumentException("Invalid IO type: " + member
							.getIOType());
				}
				if (!member.isRequired()) {
					str += " = null";
				}
				return str;
			}).collect(Collectors.joining(", "));
			sb.append(inputs);
			sb.append(")");
			// Step 3: Output
			var output = info.output();
			sb.append(" -> ");
			switch (output.getIOType()) {
				case OUTPUT:
					var description = describeType(env, Nil.of(output.getType()));
					sb.append(description);
					break;
				case MUTABLE:
				case CONTAINER:
					sb.append("None");
					break;
				default:
					throw new IllegalArgumentException("Invalid IO type: " + output
						.getIOType());
			}
			return sb.toString();
		};
		return buildOpString(infos, req, descriptor);
	}

	private static <T> String describeType(OpEnvironment env, Nil<T> from) {
		Type specialType = Types.parameterize(Function.class, new Type[] { Types
			.parameterize(Nil.class, new Type[] { from.getType() }), String.class });
		@SuppressWarnings("unchecked")
		Nil<Function<Nil<T>, String>> specialTypeNil =
			(Nil<Function<Nil<T>, String>>) Nil.of(specialType);
		try {
			Type nilFromType = Types.parameterize(Nil.class, new Type[] { from
				.getType() });
			Hints h = new Hints( //
				BaseOpHints.Adaptation.FORBIDDEN, //
				BaseOpHints.Conversion.FORBIDDEN, //
				BaseOpHints.History.IGNORE //
			);
			Function<Nil<T>, String> op = env.op("engine.describe", specialTypeNil,
				new Nil[] { Nil.of(nilFromType) }, Nil.of(String.class), h);
			return op.apply(from);
		}
		catch (OpMatchingException e) {
			return from.getType().getTypeName();
		}
	}

	@Override
	public String verboseDescriptions(OpEnvironment env, OpRequest req) {
		String name = req.getName();
		Optional<String> nsString = getNonOpString(env, name);
		if (nsString.isPresent()) return nsString.get();
		var infos = env.infos(name);
		return buildOpString(infos, req, Infos::describeVerbose);
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
		return req.getName() + ":\n\t- " + opString;
	}

	/**
	 * Helper method to build strings when the queried name is not an op
	 *
	 * @param env The op to query, or null
	 * @param name The potential op name
	 * @return If the {@code name} is empty/{@code null}, return a namespaces
	 *         string. Otherwise, if it doesn't match a particular op name, it is
	 *         assumed to be a namespace request and we return all ops in that
	 *         namespace. Returns {@code Optional.empty()} if this is a legitimate
	 *         op request.
	 */
	private Optional<String> getNonOpString(OpEnvironment env, String name) {
		String prefix = null;
		Stream<String> nsStream = null;
		if (Strings.isNullOrEmpty(name)) {
			// Return all namespaces
			nsStream = publicOpStream(env);
			nsStream = nsStream.map(s -> s.substring(0, s.lastIndexOf('.')))
				.distinct();
			prefix = "Namespaces:\n\t> ";
		}
		else if (env.infos(name).isEmpty()) {
			// Return all ops in the namespace
			nsStream = publicOpStream(env).filter(n -> n.startsWith(name));
			prefix = "Names:\n\t> ";
		}
		if (nsStream == null) return Optional.empty();
		String suffix = nsStream.collect(Collectors.joining("\n\t> "));

		if (Strings.isNullOrEmpty(suffix)) return Optional.of(
			"Not a valid Op name or namespace:\n\t> " + name);

		return Optional.of(prefix + suffix);
	}

	/**
	 * Helper method for getting publicly visible ops
	 *
	 * @return A stream of strings for each Op info not in a protected namespace
	 *         (e.g. 'engine')
	 */
	private Stream<String> publicOpStream(OpEnvironment env) {
		return env.infos().stream() //
			// Get all names from each Op
			.flatMap(info -> info.names().stream()) //
			// Deduplicate & sort
			.distinct() //
			.sorted() //
			// Filter out the engine namespace
			.filter(ns -> !ns.startsWith("engine"));
	}

	private List<OpInfo> filterInfos(Iterable<? extends OpInfo> infos,
		OpRequest req)
	{
		List<OpInfo> filtered = new ArrayList<>();
		for (var info : infos) {
			if (info instanceof ReducedOpInfo) {
				continue;
			}
			if (info instanceof ConvertedOpInfo) {
				if (((ConvertedOpInfo) info).srcInfo() instanceof ReducedOpInfo)
					continue;
			}

			// Container types are matched as outputs in OpRequests
			var numPureInputs = info.inputs().stream() //
				.filter(m -> !ItemIO.CONTAINER.equals(m.getIOType())) //
				.count();

			if (req.getArgs() == null || req.getArgs().length == numPureInputs) {
				filtered.add(info);
			}

		}
		return filtered;
	}
}
