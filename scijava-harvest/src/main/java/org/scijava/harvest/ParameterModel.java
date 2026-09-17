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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.scijava.execute.Behavior;
import org.scijava.execute.ExecutableInstance;
import org.scijava.execute.ParameterMember;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;

/**
 * What a parameter dialog talks to: current values, the shape they imply, the
 * problems with them.
 * <p>
 * Setting a value here does everything that follows from it - runs the
 * parameter's callback, notices whatever else the callback changed, and
 * rebuilds the tree, since the shape of a dialog depends on its values. A user
 * interface sets values and re-renders; it needs to know nothing about
 * callbacks, groups or generated parameters.
 * </p>
 *
 * @author Curtis Rueden
 */
public class ParameterModel {

	private final ExecutableInstance instance;
	private final ObservableStruct values;
	private final List<Consumer<ParameterTree>> listeners = new ArrayList<>();
	private ParameterTree tree;

	public ParameterModel(final ExecutableInstance instance) {
		this.instance = instance;
		this.values = new ObservableStruct(instance.parameters());
		this.tree = ParameterTree.of(instance.parameters(), instance);
	}

	/** Gets the shape the dialog should currently have. */
	public ParameterTree tree() {
		return tree;
	}

	/** Gets the current values. */
	public ObservableStruct values() {
		return values;
	}

	/** Gets a parameter's value. */
	public Object get(final String key) {
		return values.get(key);
	}

	/** Registers a listener, notified whenever the dialog's shape changes. */
	public void onTreeChanged(final Consumer<ParameterTree> listener) {
		listeners.add(listener);
	}

	/**
	 * Sets a parameter, then does everything that follows: runs its callback,
	 * notices what else changed, and rebuilds the tree.
	 *
	 * @param key the parameter to set
	 * @param value its new value
	 * @return every parameter that changed, the callback's doing included
	 */
	public List<ObservableStruct.Change> set(final String key,
		final Object value)
	{
		final List<ObservableStruct.Change> changes = new ArrayList<>(values.set(key,
			value));

		// NB: a callback may change other parameters, whose own callbacks then
		// have to run - but a pair that set each other would never stop, so each
		// parameter's callback runs at most once per set().
		final Set<String> called = new HashSet<>();
		List<ObservableStruct.Change> pending = new ArrayList<>(changes);
		pending.add(0, null); // NB: placeholder for the parameter set directly
		pending.set(0, changeFor(key));
		while (!pending.isEmpty()) {
			final List<ObservableStruct.Change> next = new ArrayList<>();
			for (final ObservableStruct.Change change : pending) {
				if (change == null) continue;
				if (!called.add(change.key())) continue;
				runCallback(change.key()).ifPresent(more -> {
					next.addAll(more);
					changes.addAll(more);
				});
			}
			pending = next;
		}

		rebuild();
		return changes;
	}

	/**
	 * Gets what is wrong with the current values, by parameter name.
	 * <p>
	 * A validator reports a problem by returning a message; anything else means
	 * the value is fine. NB: SciJava Common let a validator either throw or
	 * return a message, so every caller had to handle both. One way is enough,
	 * and a thrown exception is then a bug in the validator rather than a second
	 * protocol.
	 * </p>
	 *
	 * @return the problems, in parameter order; empty if the values are usable
	 */
	public Map<String, String> problems() {
		final Map<String, String> problems = new LinkedHashMap<>();
		for (final MemberInstance<?> member : tree.members()) {
			final String key = member.member().key();
			validator(member).ifPresent(behavior -> {
				final Object result = behavior.invoke(member.get());
				if (result instanceof String && !((String) result).isEmpty()) {
					problems.put(key, (String) result);
				}
			});
		}
		return problems;
	}

	/** Gets whether the current values are usable. */
	public boolean isValid() {
		return problems().isEmpty();
	}

	// -- Helper methods --

	private void rebuild() {
		final ParameterTree rebuilt = ParameterTree.of(instance.parameters(),
			instance);
		final boolean changed = !rebuilt.toString().equals(tree.toString());
		tree = rebuilt;
		if (changed) {
			for (final Consumer<ParameterTree> listener : listeners)
				listener.accept(tree);
		}
	}

	private ObservableStruct.Change changeFor(final String key) {
		final MemberInstance<?> member = instance.parameters().member(key);
		return member == null ? null //
			: new ObservableStruct.Change(member, null, values.get(key));
	}

	/** Runs a parameter's callback, if it has one, reporting what moved. */
	private Optional<List<ObservableStruct.Change>> runCallback(
		final String key)
	{
		final MemberInstance<?> member = instance.parameters().member(key);
		if (member == null) return Optional.empty();
		final Optional<Behavior> callback = behavior(member,
			ParameterMember.CALLBACK);
		if (callback.isEmpty()) return Optional.empty();
		callback.get().invoke();
		// NB: the callback may have set anything at all, so the only way to know
		// is to look at everything.
		return Optional.of(values.refresh());
	}

	private Optional<Behavior> validator(final MemberInstance<?> member) {
		return behavior(member, ParameterMember.VALIDATOR);
	}

	private Optional<Behavior> behavior(final MemberInstance<?> member,
		final String attr)
	{
		final Member<?> m = member.member();
		if (!(m instanceof ParameterMember)) return Optional.empty();
		return ((ParameterMember<?>) m).attr(attr).flatMap(instance::behavior);
	}
}
