/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.ops.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.ops.OpEnvironment;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpMatcher;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.OpRef;

/**
 * Default implementation of {@link OpTransformationMatcher}. This
 * implementation will chain two transformations at maximum.
 *
 * @author David Kolb
 */
public class DefaultOpTransformationMatcher implements OpTransformationMatcher {

	private final OpMatcher matcher;

	private final int maxTransformations = 2;
	
	public DefaultOpTransformationMatcher(final OpMatcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public List<OpTransformation> getTransformationsTo(final Iterable<OpTransformer> transformers, OpRef toRef, int currentChainLength) {
		List<OpTransformation> transforms = new ArrayList<>();
		
		for (OpTransformer ot: transformers) {
			final Collection<OpRef> fromRefs = ot.getRefsTransformingTo(toRef);
			if (fromRefs != null) {
				for (OpRef fromRef : fromRefs) {
					if (fromRef != null) {
						transforms.add(new OpTransformation(fromRef, toRef, ot, currentChainLength + 1));
					}
				}
			}
		}
		return transforms;
	}

	@Override
	public OpTransformationCandidate findTransformation(OpEnvironment opEnv, final Iterable<OpTransformer> transformers, OpRef ref) {
		LinkedList<OpTransformation> tsQueue = new LinkedList<>();
		tsQueue.addAll(getTransformationsTo(transformers, ref, 0));
		while (!tsQueue.isEmpty()) {
			OpTransformation t = tsQueue.pop();
			OpRef fromRef = t.getSource();
			try {
				OpCandidate match = matcher.findSingleMatch(opEnv, fromRef);
				return new OpTransformationCandidate(match, t);
			} catch (OpMatchingException e) {
				// if we have done fewer than the maximum allowed number of transformations from
				// ref, find more transformations from t
				if (t.getChainLength() < maxTransformations)
					// we need to make sure chain all of these transformations to t, thus we map the
					// results of getTransformationsTo
					tsQueue.addAll(getTransformationsTo(transformers, fromRef, t.getChainLength())//
							.stream().map(next -> next.chain(t))//
							.collect(Collectors.toList()));
			}
		}
		return null;
	}
	
}
