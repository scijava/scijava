/*
 * #%L
 * Settings a user can read and edit, in one TOML file.
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

package org.scijava.settings;

import java.util.Optional;

import org.scijava.execute.ParameterMember;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.StructInstance;

/**
 * What a command remembers between runs, and what it does not.
 * <p>
 * The shared part of {@link LoadInputs} and {@link SaveInputs}: which table a
 * command's values live in, and which of its parameters are worth keeping.
 * </p>
 *
 * @author Curtis Rueden
 */
final class RememberedInputs {

	private RememberedInputs() {
		// prevent instantiation of utility class
	}

	/**
	 * Gets the table a command's values live in: its identifier.
	 * <p>
	 * NB: the class name for a command, the path for a script - what
	 * {@code Executable.name()} says, and nothing about which artifact it came
	 * in. A class moving between components would otherwise lose a user's
	 * settings, which is a refactor nobody should be punished for.
	 * </p>
	 */
	static String table(final org.scijava.execute.Executable executable) {
		return executable.name();
	}

	/** Gets the key one parameter is remembered under. */
	static String key(final Member<?> member) {
		return attr(member, ParameterMember.PERSIST_KEY).orElseGet(member::key);
	}

	/**
	 * Gets whether a parameter is worth remembering.
	 * <p>
	 * Inputs are, unless they say {@code persist = false}. Outputs are not, and
	 * neither is anything the caller supplied outright - a value given on a
	 * command line or by another command is that run's business, not the user's
	 * remembered choice.
	 * </p>
	 */
	static boolean remembers(final MemberInstance<?> instance) {
		final Member<?> member = instance.member();
		if (!member.isInput()) return false;
		return !attr(member, ParameterMember.PERSIST).map("false"::equals).orElse(false);
	}

	/** Gets every parameter of a run that is worth remembering. */
	static Iterable<MemberInstance<?>> remembered(
		final StructInstance<?> instance)
	{
		final java.util.List<MemberInstance<?>> members = new java.util.ArrayList<>();
		for (final MemberInstance<?> member : instance.members()) {
			if (remembers(member)) members.add(member);
		}
		return members;
	}

	private static Optional<String> attr(final Member<?> member,
		final String key)
	{
		if (!(member instanceof ParameterMember)) return Optional.empty();
		return ((ParameterMember<?>) member).attr(key);
	}
}
