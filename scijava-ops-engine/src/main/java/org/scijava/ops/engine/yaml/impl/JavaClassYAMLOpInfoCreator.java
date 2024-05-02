/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.yaml.impl;

import java.net.URI;
import java.util.Map;

import org.scijava.common3.Classes;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.engine.yaml.AbstractYAMLOpInfoCreator;
import org.scijava.ops.engine.yaml.YAMLOpInfoCreator;

/**
 * A {@link YAMLOpInfoCreator} specialized for Java {@link Class}es.
 *
 * @author Gabriel Selzer
 */
public class JavaClassYAMLOpInfoCreator extends AbstractYAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(URI identifier) {
		return identifier.getScheme().startsWith("javaClass");
	}

	@Override
	protected OpInfo create(final String identifier, final String[] names,
		final String description, final double priority, String version,
		Map<String, Object> yaml) throws Exception
	{
		// parse class
		Class<?> src = Classes.load(identifier);
		// Create the OpInfo
		return new OpClassInfo(src, version, description, new Hints(), priority,
			names);
	}
}
