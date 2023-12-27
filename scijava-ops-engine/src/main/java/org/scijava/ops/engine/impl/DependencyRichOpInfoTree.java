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

import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;

/**
 * An {@link InfoTree} specialization for instances when the dependencies are
 * already instantiated. With a normal {@link InfoTree}, we'd have to get the
 * {@link InfoTree}s of all dependencies, then reinstantiate them.
 *
 * @author Gabe Selzer
 */
public class DependencyRichOpInfoTree extends InfoTree {

	private final List<RichOp<?>> dependencies;

	public DependencyRichOpInfoTree(OpInfo info, List<RichOp<?>> dependencies) {
		super(info, treesFrom(dependencies));
		this.dependencies = dependencies;
	}

	@Override
	protected Object generateOp() {
		return info().createOpInstance(dependencies).object();
	}

	private static List<InfoTree> treesFrom(List<RichOp<?>> dependencies) {
		return dependencies.stream().map(RichOp::infoTree).collect(Collectors.toList());
	}

}
