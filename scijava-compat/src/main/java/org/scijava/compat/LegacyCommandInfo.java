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

package org.scijava.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.command3.CommandInfo;
import org.scijava.command3.MenuTree;
import org.scijava.execute.Behavior;
import org.scijava.execute.Executable;
import org.scijava.execute.ExecutableInstance;
import org.scijava.module.Module;
import org.scijava.module.MethodCallException;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * A SciJava Common module, seen as a SciJava3 command.
 * <p>
 * This is the whole of the legacy bridge's promise: an application built on
 * SciJava3 can show, harvest and run everything SciJava Common already knows
 * about - every ImageJ2 command, every Fiji plugin that was ported to the
 * module system - beside its own commands, in its own menus and dialogs, with
 * no change to either side. The dependency goes one way and lives here;
 * SciJava Common knows nothing about SciJava3.
 * </p>
 * <p>
 * NB: nothing is loaded or constructed to build one of these. A
 * {@code ModuleInfo} already describes its parameters without instantiating
 * anything, which is the property that makes a menu of ten thousand legacy
 * commands cost nothing - the same property the SciJava3 annotation index has.
 * </p>
 *
 * @author Curtis Rueden
 */
public class LegacyCommandInfo implements CommandInfo {

	private final ModuleInfo info;
	private final org.scijava.Context context;

	/**
	 * @param info the module to present as a command
	 * @param context the legacy context, which the module is injected with -
	 *          exactly as SciJava Common's own {@code ModuleService} does, and
	 *          without which a module that uses a legacy service (a
	 *          {@code DynamicCommand}, for one) fails the moment it runs
	 */
	public LegacyCommandInfo(final ModuleInfo info,
		final org.scijava.Context context)
	{
		this.info = info;
		this.context = context;
	}

	/** Gets the module description behind this command. */
	public ModuleInfo info() {
		return info;
	}

	// -- CommandInfo methods --

	@Override
	public Optional<String> menuPath() {
		final MenuPath path = info.getMenuPath();
		if (path == null || path.isEmpty()) return Optional.empty();
		// NB: not getMenuString(), which renders "A > B > C" with spaces around
		// the separator. SciJava3 splits on ">" and would carry the spaces into
		// the labels, so the path is rebuilt from the entries themselves.
		return Optional.of(path.stream().map(MenuEntry::getName) //
			.collect(java.util.stream.Collectors.joining(MenuTree.SEPARATOR)));
	}

	@Override
	public String label() {
		// NB: SciJava Common's own answer to this question, which prefers the
		// label, then the menu leaf, then the name.
		return info.getTitle();
	}

	@Override
	public Optional<String> accelerator() {
		return leaf().map(MenuEntry::getAccelerator).map(
			LegacyCommandInfo::accelerator).filter(s -> !s.isEmpty());
	}

	@Override
	public Optional<String> iconPath() {
		return Optional.ofNullable(info.getIconPath()).filter(s -> !s.isEmpty());
	}

	@Override
	public double weight() {
		return leaf().map(MenuEntry::getWeight).orElse(Double.POSITIVE_INFINITY);
	}

	@Override
	public boolean isVisible() {
		return info.isVisible();
	}

	// -- Executable methods --

	@Override
	public String name() {
		return info.getDelegateClassName();
	}

	@Override
	public Struct struct() {
		return structOf(info);
	}

	/**
	 * Describes the parameters of a module description.
	 * <p>
	 * NB: service and context parameters are left out. SciJava Common's
	 * {@code @Parameter} means both "this is an input" and "inject this
	 * service", and its harvester tells them apart by having preprocessors fill
	 * the services first. SciJava3 split the two annotations precisely so that
	 * this question never arises, so the bridge answers it here: what the
	 * legacy context injects is not something to ask the user for.
	 * </p>
	 */
	private static Struct structOf(final ModuleInfo info) {
		final List<Member<?>> members = new ArrayList<>();
		for (final ModuleItem<?> item : info.inputs()) {
			if (isInjected(item)) continue;
			members.add(new LegacyMember<>(item));
		}
		for (final ModuleItem<?> item : info.outputs()) {
			// NB: a both-directions item appears in each list; it is one parameter.
			if (item.isInput() || isInjected(item)) continue;
			members.add(new LegacyMember<>(item));
		}
		return () -> members;
	}

	/** Gets whether the legacy context fills this parameter itself. */
	private static boolean isInjected(final ModuleItem<?> item) {
		final Class<?> type = item.getType();
		return org.scijava.service.Service.class.isAssignableFrom(type) || //
			org.scijava.Context.class.isAssignableFrom(type);
	}

	@Override
	public ExecutableInstance create() {
		final Module module;
		try {
			module = info.createModule();
		}
		catch (final ModuleException exc) {
			throw new IllegalStateException("Cannot create legacy command: " +
				name(), exc);
		}
		// NB: what SciJava Common's ModuleService.createModule does, and for the
		// same reason: a legacy command may hold @Parameter services, and a
		// DynamicCommand needs its own context to build its parameters at all.
		context.inject(module);
		org.scijava.Priority.inject(module, info.getPriority());
		// NB: and what its InitPreprocessor does, before anything harvests. A
		// DynamicCommand adds its parameters here, so initializing later would
		// mean showing the user a dialog missing half its rows.
		try {
			module.initialize();
		}
		catch (final MethodCallException exc) {
			throw new IllegalStateException("Cannot initialize legacy command: " +
				name(), exc);
		}
		return new Instance(module);
	}

	@Override
	public String toString() {
		return name() + menuPath().map(p -> " [" + p + "]").orElse("");
	}

	// -- Helper methods --

	/**
	 * Translates a legacy accelerator into the notation SciJava3 uses.
	 * <p>
	 * NB: not {@code Accelerator.toString()}, which renders Swing's format
	 * ({@code "control L"}). Swing would parse that back, but JavaFX would not,
	 * and a shortcut that works in one toolkit and silently vanishes in another
	 * is exactly the sort of thing the bridge exists to prevent. Written as
	 * {@code ^L}, every binding understands it - and control or meta both
	 * become {@code ^}, which is what "the platform's menu shortcut" means.
	 * </p>
	 */
	private static String accelerator(final org.scijava.input.Accelerator acc) {
		if (acc == null || acc.getKeyCode() == null) return "";
		final org.scijava.input.InputModifiers mods = acc.getModifiers();
		final StringBuilder sb = new StringBuilder();
		if (mods != null) {
			if (mods.isCtrlDown() || mods.isMetaDown()) sb.append('^');
			if (mods.isAltDown()) sb.append('!');
			if (mods.isShiftDown()) sb.append('+');
		}
		return sb.append(acc.getKeyCode().name()).toString();
	}

	/**
	 * Gets what to tell the user about a failed validation.
	 * <p>
	 * NB: the innermost message. A validator that throws has its exception
	 * wrapped by SciJava Common in a {@code MethodCallException} whose own
	 * message is "Error executing method: ...", which tells a user nothing; the
	 * message the validator actually wrote is underneath. A validator that
	 * returns its message instead has no cause, and that message is the
	 * exception's own.
	 * </p>
	 */
	private static String problem(final MethodCallException exc) {
		Throwable deepest = exc;
		while (deepest.getCause() != null)
			deepest = deepest.getCause();
		final String message = deepest.getMessage();
		return message == null || message.isEmpty() ? exc.getMessage() : message;
	}

	private Optional<MenuEntry> leaf() {
		final MenuPath path = info.getMenuPath();
		return path == null || path.isEmpty() ? Optional.empty() //
			: Optional.ofNullable(path.getLeaf());
	}

	/** One run of a legacy command, backed by a SciJava Common module. */
	private class Instance implements ExecutableInstance {

		private final Module module;
		private final StructInstance<Module> parameters;

		Instance(final Module module) {
			this.module = module;
			// NB: the *module's* description, not this command's. They are the
			// same object for an ordinary command, and for a DynamicCommand the
			// module's carries the parameters it just built for itself. That
			// mutable-per-instance description is what SciJava3 declined to offer
			// as a way of writing commands -- and presenting one costs nothing.
			final Struct struct = structOf(module.getInfo());
			final List<MemberInstance<?>> members = new ArrayList<>();
			for (final Member<?> member : struct.members()) {
				members.add(((LegacyMember<?>) member).instance(module));
			}
			parameters = new StructInstance<>() {

				@Override
				public Struct struct() {
					return struct;
				}

				@Override
				public Module object() {
					return module;
				}

				@Override
				public List<MemberInstance<?>> members() {
					return members;
				}

				@Override
				public MemberInstance<?> member(final String key) {
					return members.stream() //
						.filter(m -> m.member().key().equals(key)) //
						.findFirst().orElse(null);
				}
			};
		}

		@Override
		public Executable executable() {
			return LegacyCommandInfo.this;
		}

		@Override
		public StructInstance<?> parameters() {
			return parameters;
		}

		@Override
		public void run() {
			// NB: initialization already happened, at create() time. The legacy
			// preprocessor chain is deliberately not involved: this is a SciJava3
			// run, and two chains harvesting the same inputs would be one too
			// many.
			module.run();
		}

		@Override
		public Optional<Behavior> behavior(final String name) {
			// NB: SciJava3 names a behavior and asks the executable what the name
			// means. For a legacy command it means a method on the module's
			// delegate object, which SciJava Common knows how to invoke - so a
			// legacy dialog behaves in a SciJava3 application exactly as it did
			// in an ImageJ2 one, without either side being changed.
			for (final ModuleItem<?> item : module.getInfo().inputs()) {
				if (name.equals(item.getCallback())) {
					return Optional.of(args -> {
						try {
							item.callback(module);
						}
						catch (final MethodCallException exc) {
							throw new IllegalStateException("Legacy callback '" + name +
								"' failed", exc);
						}
						return null;
					});
				}
				if (name.equals(item.getValidater())) {
					// NB: SciJava Common lets a validator either throw or return a
					// non-empty message, which is why every caller there has to
					// handle both. SciJava3 has one protocol - return a message, or
					// do not - and the two collapse into it here, which is the
					// whole cost of that simplification.
					//
					// (Released SciJava Common offers only validate(), which throws
					// a MethodCallException carrying the returned message. A
					// validateMessage() exists in its development line and would be
					// this same thing; using validate() works against both.)
					return Optional.of(args -> {
						try {
							item.validate(module);
							return null;
						}
						catch (final MethodCallException exc) {
							return problem(exc);
						}
					});
				}
			}
			return Optional.empty();
		}
	}
}
