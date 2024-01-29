/*-
 * #%L
 * SciJava Discovery: Discovery mechanisms used by the SciJava Framework.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

package org.scijava.discovery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link Discoverer} implementation that can be set up by the user. Useful
 * when a small number of implementation are already exposed.
 *
 * @author Gabriel Selzer
 */
public class ManualDiscoverer implements Discoverer {

	/**
	 * The implementations
	 */
	Set<Object> set;

	public ManualDiscoverer() {
		set = new HashSet<>();
	}

	public void register(Object[]... objects) {
		for (Object[] arr : objects) {
			for (Object o : arr) {
				set.add(o);
			}
		}
	}

	public void register(Object... objects) {
		for (Object o : objects) {
			set.add(o);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> discover(Class<T> c) {
		return set.parallelStream() //
			.filter(o -> c.isAssignableFrom(o.getClass())) //
			.map(o -> (T) o) //
			.collect(Collectors.toList());
	}

}
