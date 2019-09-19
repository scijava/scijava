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

import java.util.Collection;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;
import org.scijava.plugin.SingletonPlugin;

/**
 * Interface describing a transformer that is able to convert between different
 * types of Ops.
 *
 * @author David Kolb
 * @author Marcel Wiedenmann
 */
public interface OpTransformer extends SingletonPlugin {

	/**
	 * Transforms the given {@code src} Op into the target Op specified by the
	 * given {@code targetRef}. If the transformation depends on other Ops, the
	 * given {@link OpService} is used to retrieve them.
	 *
	 * @param opService The {@link OpService} to use for retrieving further Ops.
	 * @param src The source Op to transform.
	 * @param targetRef The {@link OpRef} that describes the Op into which to
	 *          transform {@code src}.
	 * @return The transformed Op.
	 * @throws OpTransformationException If any exception occurs during
	 *           transformation.
	 */
	Object transform(OpService opService, Object src, OpRef targetRef) throws OpTransformationException;

	
	default OpRef substituteAnyInTargetRef(OpRef srcRef, OpRef targetRef) {
		return targetRef;
	};
	
	/**
	 * Returns a collection that contains the {@link OpRef}s of all Ops which can
	 * be transformed into the Op described by the given {@code targetRef} using
	 * this transformer.
	 * <P>
	 * This method effectively applies the inverse of the
	 * {@link #transform(OpService, Object, OpRef) transformation} of this
	 * transformer to the given {@code targetRef}. It can be used to inquire about
	 * Ops that can be transformed into the specified target using this
	 * transformer.
	 *
	 * @param targetRef The target {@link OpRef}.
	 * @return The possible source {@link OpRef}s.
	 */
	Collection<OpRef> getRefsTransformingTo(OpRef targetRef);
}
