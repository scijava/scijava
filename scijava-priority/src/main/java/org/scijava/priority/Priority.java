/*
 * #%L
 * SciJava constants facilitating a consistent priority order.
 * %%
 * Copyright (C) 2022 - 2024 SciJava developers.
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

package org.scijava.priority;

/**
 * Constants for specifying an item's priority.
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @see org.scijava.priority.Prioritized#priority()
 */
public final class Priority {

	private Priority() {
		// prevent instantiation of utility class
	}

	/**
	 * Priority for items that must be sorted first.
	 * <p>
	 * Note that it <em>is</em> still possible to prioritize something earlier
	 * than this value (e.g., for testing purposes), although doing so strongly
	 * discouraged in production.
	 * </p>
	 */
	public static final double FIRST = +1e300;

	/** Priority for items that very strongly prefer to be sorted early. */
	public static final double EXTREMELY_HIGH = +1000000;

	/** Priority for items that strongly prefer to be sorted early. */
	public static final double VERY_HIGH = +10000;

	/** Priority for items that prefer to be sorted earlier. */
	public static final double HIGH = +100;

	/** Default priority for items. */
	public static final double NORMAL = 0;

	/** Priority for items that prefer to be sorted later. */
	public static final double LOW = -100;

	/** Priority for items that strongly prefer to be sorted late. */
	public static final double VERY_LOW = -10000;

	/** Priority for items that very strongly prefer to be sorted late. */
	public static final double EXTREMELY_LOW = -1000000;

	/**
	 * Priority for items that must be sorted last.
	 * <p>
	 * Note that it <em>is</em> still possible to prioritize something later than
	 * this value (e.g., for testing purposes), although doing so strongly
	 * discouraged in production.
	 * </p>
	 */
	public static final double LAST = -1e300;
}
