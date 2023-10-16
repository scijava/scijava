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

package org.scijava.ops.engine.struct;

import java.util.Collection;
import java.util.Map;

import org.scijava.struct.FunctionalMethodType;

public class MethodParamInfo {

	private final Map<FunctionalMethodType, String> fmtNames;
	private final Map<FunctionalMethodType, String> fmtDescriptions;
	private final Map<FunctionalMethodType, Boolean> fmtNullability;

	public MethodParamInfo(final Map<FunctionalMethodType, String> fmtNames,
		final Map<FunctionalMethodType, String> fmtDescriptions,
		final Map<FunctionalMethodType, Boolean> fmtNullability)
	{
		this.fmtNames = fmtNames;
		this.fmtDescriptions = fmtDescriptions;
		this.fmtNullability = fmtNullability;
	}

	public String name(FunctionalMethodType fmt) {
		return fmtNames.get(fmt);
	}

	public String description(FunctionalMethodType fmt) {
		return fmtDescriptions.get(fmt);
	}

	public boolean containsAll(Collection<FunctionalMethodType> fmts) {
		return fmtNames.keySet().containsAll(fmts) && fmtDescriptions.keySet()
			.containsAll(fmts);
	}

	public Map<FunctionalMethodType, String> getFmtNames() {
		return fmtNames;
	}

	public Map<FunctionalMethodType, String> getFmtDescriptions() {
		return fmtDescriptions;
	}

	public Map<FunctionalMethodType, Boolean> getFmtNullability() {
		return fmtNullability;
	}

	public Boolean optionality(FunctionalMethodType fmt) {
		return fmtNullability.getOrDefault(fmt, false);
	}
}
