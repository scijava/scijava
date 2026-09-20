/*
 * #%L
 * An application container: services, discovered and wired.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.context;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/**
 * Reflective access to classes that have opened themselves to the container.
 * <p>
 * A plugin author writes one line:
 * </p>
 *
 * <pre>
 * opens com.example to org.scijava.context;
 * </pre>
 * <p>
 * and every SciJava module that legitimately needs in - the container
 * constructing a plugin, the execution layer reading its {@code @Parameter}
 * fields, later the harvester invoking a callback - borrows access from here
 * rather than each demanding an {@code opens} of its own.
 * </p>
 * <p>
 * That works because reflective access is checked against the module that
 * <em>performs</em> it, which is this one, rather than the module that asked.
 * Being static costs nothing: what matters to the module system is where the
 * code lives, not how it is called.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class Access {

	private Access() {
		// NB: prevent instantiation of utility class.
	}

	/**
	 * Gets a lookup with private access to the given class.
	 * <p>
	 * The result is scoped to that one class: its holder can read and write its
	 * fields and invoke its methods, and can do nothing with any other class.
	 * </p>
	 * <p>
	 * NB: {@link java.lang.invoke.MethodHandles.Lookup} is deliberately what
	 * comes back, rather than some narrower accessor of our own. It is the
	 * JDK's own token for delegated reflective access - the reason
	 * {@code privateLookupIn} exists - and it already covers fields, methods
	 * and constructors. A bespoke facade would have to be extended every time a
	 * caller needed a kind of access we had not anticipated, for no benefit
	 * beyond a shorter method name.
	 * </p>
	 *
	 * @param type the class to be reflected into
	 * @return a lookup with private access to it
	 * @throws IllegalArgumentException if the class's package is not open to
	 *           this module
	 */
	public static Lookup lookupIn(final Class<?> type) {
		try {
			// NB: privateLookupIn also requires this module to *read* the target's
			// module, which a container never declares -- plugins depend on it,
			// not the reverse. Only a module's own code may add that edge, which
			// is the other reason this lives here.
			Access.class.getModule().addReads(type.getModule());
			return MethodHandles.privateLookupIn(type, MethodHandles.lookup());
		}
		catch (final IllegalAccessException exc) {
			throw new IllegalArgumentException("Cannot reflect into " + type
				.getName() + ". Does its module declare `opens " + type
					.getPackageName() + " to org.scijava.context;`?", exc);
		}
	}
}
