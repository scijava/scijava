/*
 * #%L
 * Locations and data handles: a uniform way to address and read bytes.
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

package org.scijava.io3.handle;

import org.scijava.io3.location.Location;

/**
 * Abstract base class for {@link DataHandle} implementations.
 *
 * @author Curtis Rueden
 */
public abstract class AbstractDataHandle<L extends Location> implements
	DataHandle<L>
{

	private L location;

	// -- DataHandle methods --

	@Override
	public void set(final L location) {
		if (!supports(location)) {
			throw new IllegalArgumentException("Incompatible location: " + location);
		}
		this.location = location;
	}

	@Override
	public L get() {
		return location;
	}

	private byte[] conversionBuffer = new byte[8];
	
	@Override
	public byte[] conversionBuffer() {
		return conversionBuffer;
	}
 
	// -- Fields --

	private ByteOrder order = ByteOrder.BIG_ENDIAN;
	private String encoding = "UTF-8";

	// -- DataHandle methods --

	@Override
	public ByteOrder getOrder() {
		return order;
	}

	@Override
	public void setOrder(final ByteOrder order) {
		this.order = order;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

}
