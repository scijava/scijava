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

package org.scijava.ops.engine.impl;

import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpInfoGenerator;
import org.scijava.ops.engine.matcher.impl.DefaultOpClassInfo;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpHints;

import java.util.Collections;
import java.util.List;

public class OpClassOpInfoGenerator implements OpInfoGenerator {

	private Hints formHints(OpHints h) {
		if (h == null) return new Hints();
		return new Hints(h.hints());
	}

	protected List<OpInfo> processClass(Class<?> c) {
		OpClass p = c.getAnnotation(OpClass.class);
		if (p == null) return Collections.emptyList();
		return Collections.singletonList(new DefaultOpClassInfo( //
			c, //
			Versions.of(c), //
			p.description(), //
			formHints(c.getAnnotation(OpHints.class)), //
			p.priority(), //
			Infos.parseNames(p.names()) //
		));
	}

	@Override
	public boolean canGenerateFrom(Object o) {
		boolean isOp = o instanceof Op;
		boolean isOpClass = o.getClass().isAnnotationPresent(OpClass.class);
		return isOp && isOpClass;
	}

	@Override
	public List<OpInfo> generateInfosFrom(Object o) {
		return processClass(o.getClass());
	}
}
