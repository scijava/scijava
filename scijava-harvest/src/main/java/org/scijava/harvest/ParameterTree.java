/*
 * #%L
 * The model behind a parameter dialog: groups, dependencies, validation.
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

package org.scijava.harvest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.scijava.execute.Behavior;
import org.scijava.execute.ExecutableInstance;
import org.scijava.execute.Executables;
import org.scijava.execute.ParameterMember;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * The shape of a parameter dialog at one moment: which parameters are shown,
 * how they are grouped, and in what order.
 * <p>
 * It is rebuilt whenever a value changes, because the shape itself depends on
 * the values: a chosen implementation brings its own parameters, an "advanced"
 * toggle reveals a group, a count decides how many labels appear. A user
 * interface renders a tree, and re-renders when handed a new one.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ParameterTree {

	private final List<ParameterNode> nodes;

	private ParameterTree(final List<ParameterNode> nodes) {
		this.nodes = nodes;
	}

	/** Gets the top-level nodes, in display order. */
	public List<ParameterNode> nodes() {
		return nodes;
	}

	/** Gets every parameter in the tree, groups flattened, in display order. */
	public List<MemberInstance<?>> members() {
		final List<MemberInstance<?>> members = new ArrayList<>();
		collect(nodes, members);
		return members;
	}

	/** Finds a node by its parameter's name. */
	public Optional<ParameterNode> find(final String key) {
		return find(nodes, key);
	}

	/**
	 * Builds the tree for the given parameters.
	 *
	 * @param instance the parameter values, which the shape may depend on
	 * @param executable the thing being run, which supplies named behaviors
	 * @return the tree as it should appear right now
	 */
	public static ParameterTree of(final StructInstance<?> instance,
		final ExecutableInstance executable)
	{
		return new Builder(instance, executable).build();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		print(nodes, sb, 0);
		return sb.toString();
	}

	// -- Helper methods --

	private static void collect(final List<ParameterNode> nodes,
		final List<MemberInstance<?>> members)
	{
		for (final ParameterNode node : nodes) {
			node.member().ifPresent(members::add);
			collect(node.children(), members);
		}
	}

	private static Optional<ParameterNode> find(final List<ParameterNode> nodes,
		final String key)
	{
		for (final ParameterNode node : nodes) {
			final Optional<MemberInstance<?>> member = node.member();
			if (member.isPresent() && member.get().member().key().equals(key)) {
				return Optional.of(node);
			}
			final Optional<ParameterNode> found = find(node.children(), key);
			if (found.isPresent()) return found;
		}
		return Optional.empty();
	}

	private static void print(final List<ParameterNode> nodes,
		final StringBuilder sb, final int depth)
	{
		for (final ParameterNode node : nodes) {
			sb.append("  ".repeat(depth)).append(node.isGroup() ? "[" + node.label() +
				"]" : node.label()).append('\n');
			print(node.children(), sb, depth + 1);
		}
	}

	/** Builds a tree from parameters, their metadata, and current values. */
	private static class Builder {

		private final StructInstance<?> instance;
		private final ExecutableInstance executable;
		private final Map<String, Group> groups = new LinkedHashMap<>();

		Builder(final StructInstance<?> instance,
			final ExecutableInstance executable)
		{
			this.instance = instance;
			this.executable = executable;
			for (final Group group : declaredGroups())
				groups.put(group.name(), group);
		}

		ParameterTree build() {
			final List<ParameterNode> nodes = new ArrayList<>();
			final Map<String, DefaultNode> groupNodes = new LinkedHashMap<>();

			for (final MemberInstance<?> member : instance.members()) {
				if (!member.member().isInput()) continue;
				if (!isVisible(member)) continue;

				final ParameterNode node = nodeFor(member);
				final Optional<String> group = attr(member, ParameterMember.GROUP);
				if (group.isEmpty()) {
					nodes.add(node);
				}
				else {
					// NB: a group appears where its first member would have.
					final DefaultNode groupNode = groupNodes.computeIfAbsent(group
						.get(), name -> {
							final DefaultNode created = groupNode(name);
							nodes.add(created);
							return created;
						});
					groupNode.children.add(node);
				}
				appendGeneratedGroups(nodes, member.member().key());
			}
			// NB: a group whose visibility says no disappears entirely, rather
			// than showing as an empty box.
			nodes.removeIf(node -> node.isGroup() && node.children().isEmpty());
			return new ParameterTree(nodes);
		}

		/** Builds the node for one parameter, expanding it if it is a struct. */
		private ParameterNode nodeFor(final MemberInstance<?> member) {
			final DefaultNode node = new DefaultNode(label(member), member);
			for (final MemberInstance<?> child : subMembers(member))
				node.children.add(nodeFor(child));
			return node;
		}

		/**
		 * Gets the parameters <em>inside</em> a parameter, if it has any.
		 * <p>
		 * NB: driven by the value's class rather than the declared type, which is
		 * what makes a chosen implementation work: the field says
		 * {@code Joke}, the value is a knock-knock joke, and it is the joke's own
		 * parameters that should appear. Expanding the declared type would find
		 * nothing, since an interface declares no fields.
		 * </p>
		 */
		private List<MemberInstance<?>> subMembers(
			final MemberInstance<?> member)
		{
			final Object value = member.isReadable() ? member.get() : null;
			if (value == null) return List.of();
			final Struct struct = Executables.struct(value.getClass());
			final List<Member<?>> members = struct.members();
			if (members.isEmpty()) return List.of();
			return struct.createInstance(value).members();
		}

		/** Adds any generated groups anchored after the given parameter. */
		private void appendGeneratedGroups(final List<ParameterNode> nodes,
			final String afterKey)
		{
			for (final Group group : groups.values()) {
				if (group.membersFrom().isEmpty()) continue;
				if (!afterKey.equals(group.after())) continue;
				if (!isVisible(group)) continue;
				final DefaultNode node = groupNode(group.name());
				for (final MemberInstance<?> member : generatedMembers(group))
					node.children.add(nodeFor(member));
				if (!node.children.isEmpty()) nodes.add(node);
			}
		}

		/** Asks the executable for a generated group's parameters. */
		private List<MemberInstance<?>> generatedMembers(final Group group) {
			final Optional<Behavior> behavior = executable.behavior(group
				.membersFrom());
			if (behavior.isEmpty()) return List.of();
			final Object result = behavior.get().invoke();
			if (result instanceof StructInstance) {
				return ((StructInstance<?>) result).members();
			}
			if (result instanceof Struct) {
				// NB: a struct with no object behind it cannot be read or written,
				// so a generator must hand back an instance.
				throw new IllegalStateException("Generator " + group.membersFrom() + //
					" returned a Struct; it must return a StructInstance, bound to" + //
					" something that holds the values");
			}
			if (result == null) return List.of();
			// A plain object: its own parameters are the group's.
			return Executables.struct(result.getClass()).createInstance(result)
				.members();
		}

		private DefaultNode groupNode(final String name) {
			final Group group = groups.get(name);
			final String label = group == null || group.label().isEmpty() ? name //
				: group.label();
			final DefaultNode node = new DefaultNode(label, null);
			if (group != null) {
				node.collapsible = group.collapsible();
				node.collapsed = group.collapsed();
			}
			return node;
		}

		private boolean isVisible(final MemberInstance<?> member) {
			final Optional<String> when = attr(member,
				ParameterMember.VISIBLE_WHEN);
			if (when.isEmpty()) {
				// A parameter in a group inherits that group's visibility.
				final Optional<String> group = attr(member, ParameterMember.GROUP);
				return group.isEmpty() || isVisible(groups.get(group.get()));
			}
			return ask(when.get());
		}

		private boolean isVisible(final Group group) {
			return group == null || group.visibleWhen().isEmpty() || ask(group
				.visibleWhen());
		}

		private boolean ask(final String behaviorName) {
			final Optional<Behavior> behavior = executable.behavior(behaviorName);
			if (behavior.isEmpty()) return true;
			return !Boolean.FALSE.equals(behavior.get().invoke());
		}

		private static Optional<String> attr(final MemberInstance<?> member,
			final String key)
		{
			final Member<?> m = member.member();
			return m instanceof ParameterMember ? ((ParameterMember<?>) m).attr(key)
				: Optional.empty();
		}

		private static String label(final MemberInstance<?> member) {
			final Member<?> m = member.member();
			return m instanceof ParameterMember ? ((ParameterMember<?>) m).label()
				: m.key();
		}

		private List<Group> declaredGroups() {
			final Object object = executable.parameters().object();
			if (object == null) return List.of();
			final Group[] declared = object.getClass().getAnnotationsByType(
				Group.class);
			return declared == null ? List.of() : List.of(declared);
		}
	}

	/** A node in the tree. */
	private static class DefaultNode implements ParameterNode {

		private final String label;
		private final MemberInstance<?> member;
		private final List<ParameterNode> children = new ArrayList<>();
		private boolean collapsible;
		private boolean collapsed;

		DefaultNode(final String label, final MemberInstance<?> member) {
			this.label = label;
			this.member = member;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public Optional<MemberInstance<?>> member() {
			return Optional.ofNullable(member);
		}

		@Override
		public List<ParameterNode> children() {
			return Collections.unmodifiableList(children);
		}

		@Override
		public boolean isCollapsible() {
			return collapsible;
		}

		@Override
		public boolean isCollapsed() {
			return collapsed;
		}

		@Override
		public String toString() {
			return isGroup() ? "[" + label + "]" : label;
		}
	}
}
