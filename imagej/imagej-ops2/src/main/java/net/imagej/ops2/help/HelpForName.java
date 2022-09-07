/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.help;

import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;

/**
 * Gets documentation for the given name.
 *
 * @author Mark Hiner
 * @implNote op names='help', priority='100.', description='Gets documentation
 *           for all Ops with the given name'
 */
public class HelpForName extends AbstractHelp implements
	BiFunction<String, OpEnvironment, String>
{

	/**
	 * Returns a {@link String} describing all Ops in {@code ops} of name
	 * {@code name}
	 *
	 * @param name the {@link String} against which Ops in {@code ops} should be
	 *          matched
	 * @param ops the {@link OpEnvironment} containing Ops
	 */
	@Override
	public String apply(String name, OpEnvironment ops) {
		final Iterable<OpInfo> allOps = ops.infos(name);
		help(allOps);
		return help;
	}

}
