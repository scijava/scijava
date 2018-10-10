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
import java.util.List;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.OpRef;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service to find Op transformations. This default service will chain two transformations
 * at maximum.
 * 
 * @author David Kolb
 */
@Plugin(type = Service.class)
public final class DefaultOpTransformerService extends AbstractSingletonService<OpTransformer>
		implements OpTransformerService {

	//TODO why does this not work?
//	@Parameter
	private OpService opService;
	
	@Override
	public List<OpTransformation> getTansformationsTo(OpRef toRef) {
		init();
		List<OpTransformation> transforms = new ArrayList<>();
		
		for (OpTransformer ot: getInstances()) {
			OpRef fromRef = ot.getRefTransformingTo(toRef);
			if (fromRef != null) {
				transforms.add(new OpTransformation(fromRef, toRef, ot));
			}
		}
		return transforms;
	}
	
	@Override
	public OpTransformationCandidate findTransfromation(OpRef ref) {
		init();
		List<OpTransformation> ts = getTansformationsTo(ref);
		for (OpTransformation t : ts) {
			OpTransformationCandidate match = findTransfromation(opService, t, 0);
			if (match != null) {
				return match;
			}
		}
		return null;
	}
	
	private OpTransformationCandidate findTransfromation(OpService opService, OpTransformation candidate, int depth) {
		if (candidate == null || depth > 1) {
			return null;
		} else {
			OpRef fromRef = candidate.getSource();
			try {
				OpCandidate match = opService.findTypeMatch(fromRef);
				return new OpTransformationCandidate(match, candidate);
			} catch (OpMatchingException e) {
				List<OpTransformation> ts = getTansformationsTo(fromRef);
				for (OpTransformation t : ts) {
					OpTransformationCandidate cand = findTransfromation(opService, t.chain(candidate), depth + 1);
					if (cand != null) {
						return cand;
					}
				}
			}
		}
		return null;
	}
	
	private void init() {
		if (opService == null) {
			opService = context().getService(OpService.class);
		}
	}
}
