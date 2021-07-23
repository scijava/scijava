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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.scijava.ops.engine.OpCandidate;
import org.scijava.ops.engine.OpInfo;
import org.scijava.ops.engine.OpUtils;

/**
 * Base class for help operations.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractHelp {

	protected String help;

	protected void help(final List<OpCandidate> candidates) {
		final ArrayList<OpInfo> infos = new ArrayList<>();
		for (final OpCandidate candidate : candidates) {
			infos.add(candidate.opInfo());
		}
		help(infos);
	}

	protected void help(final Iterable<? extends OpInfo> infos) {
		Iterator<? extends OpInfo> itr = infos.iterator();
		if (!itr.hasNext()){
			help = "No such operation.";
			return;
		}

		final StringBuilder sb = new StringBuilder("Available operations:");
		for (final OpInfo info : infos) {
			sb.append("\n\t" + OpUtils.opString(info));
		}

		// TODO: ops cannot (yet) have descriptions.
//		if (infos.size() == 1) {
//			final OpInfo info = infos.iterator().next();
//			final String description = info.cInfo().getDescription();
//			if (description != null && !description.isEmpty()) {
//				sb.append("\n\n" + description);
//			}
//		}

		help = sb.toString();
	}

}
