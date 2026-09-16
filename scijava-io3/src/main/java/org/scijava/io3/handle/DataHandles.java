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

import java.io.DataOutput;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.LongConsumer;

import org.scijava.io3.location.Location;

/**
 * Creates {@link DataHandle}s for {@link Location}s, and provides the
 * operations that work across any handle.
 * <p>
 * Handles come from {@link ServiceLoader}, so this needs no container and no
 * configuration. For an embedded or tested setting where implicit discovery is
 * unwanted, construct an instance with an explicit list of handles instead.
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Einsdorf
 */
public class DataHandles {

	/** Default buffer size for {@link #copy}. */
	public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

	private static class DefaultHolder {

		static final DataHandles INSTANCE = new DataHandles(discoverHandles());
	}

	private final List<DataHandle<?>> handles;

	/** Creates an instance that uses exactly the given handles. */
	public DataHandles(final List<DataHandle<?>> handles) {
		this.handles = new ArrayList<>(handles);
		// NB: highest priority first, so the first supporting handle wins.
		this.handles.sort(Comparator.comparingDouble( //
			(DataHandle<?> h) -> h.priority()).reversed());
	}

	/** Gets the shared instance, backed by {@link ServiceLoader} discovery. */
	public static DataHandles get() {
		return DefaultHolder.INSTANCE;
	}

	/**
	 * Creates a handle for the given location.
	 *
	 * @param location the location to access
	 * @return a handle set to that location, or null if no handle supports it
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <L extends Location> DataHandle<L> create(final L location) {
		final Optional<DataHandle<?>> prototype = handles.stream() //
			.filter(h -> h.supports(location)) //
			.findFirst();
		if (prototype.isEmpty()) return null;
		// NB: the discovered handles are prototypes; each caller needs its own,
		// since a handle carries the position and buffers of one open stream.
		final DataHandle handle = instantiate(prototype.get());
		handle.set(location);
		return handle;
	}

	/** Gets whether the given location exists. */
	public boolean exists(final Location location) throws IOException {
		try (final DataHandle<?> handle = create(location)) {
			return handle != null && handle.exists();
		}
	}

	/** Wraps the given handle so that reads are buffered. */
	public DataHandle<Location> readBuffer(final DataHandle<Location> handle) {
		return handle == null ? null : new ReadBufferDataHandle(handle);
	}

	/** Creates a read-buffered handle for the given location. */
	public DataHandle<Location> readBuffer(final Location location) {
		return readBuffer(create(location));
	}

	/** Wraps the given handle so that writes are buffered. */
	public DataHandle<Location> writeBuffer(final DataHandle<Location> handle) {
		return handle == null ? null : new WriteBufferDataHandle(handle);
	}

	/** Creates a write-buffered handle for the given location. */
	public DataHandle<Location> writeBuffer(final Location location) {
		return writeBuffer(create(location));
	}

	/** Gets the known handles, highest priority first. */
	public List<DataHandle<?>> handles() {
		return List.copyOf(handles);
	}

	// -- Static utility methods --

	/** Gets the exception thrown when writing to a read-only handle. */
	protected static IOException readOnlyException() {
		return new IOException("This handle is read-only!");
	}

	/** Gets the exception thrown when reading from a write-only handle. */
	protected static IOException writeOnlyException() {
		return new IOException("This handle is write-only!");
	}

	/**
	 * Copies bytes from one handle to another, starting at each handle's
	 * current offset.
	 *
	 * @param in the handle to read from
	 * @param out the handle to write to
	 * @return the number of bytes copied
	 */
	public static long copy(final DataHandle<Location> in,
		final DataHandle<Location> out) throws IOException
	{
		return copy(in, out, 0, null, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copies bytes from one handle to another, starting at each handle's
	 * current offset.
	 *
	 * @param in the handle to read from
	 * @param out the handle to write to
	 * @param length how many bytes to copy, or 0 for all of them
	 * @return the number of bytes copied
	 */
	public static long copy(final DataHandle<Location> in,
		final DataHandle<Location> out, final long length) throws IOException
	{
		return copy(in, out, length, null, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copies bytes from one handle to another, starting at each handle's
	 * current offset.
	 * <p>
	 * NB: SciJava Common reported progress to a {@code Task}, which also
	 * carried cancellation. Here progress is a plain {@link LongConsumer} of
	 * the running byte count, so that this component stays standalone; callers
	 * wanting {@code org.scijava.progress} can adapt it in one lambda, and
	 * cancellation belongs to the caller's own thread handling.
	 * </p>
	 *
	 * @param in the handle to read from
	 * @param out the handle to write to
	 * @param length how many bytes to copy, or 0 for all of them
	 * @param progress notified with the running total, or null for none
	 * @param bufferSize the size of the copy buffer
	 * @return the number of bytes copied
	 * @throws InterruptedIOException if the thread is interrupted, with
	 *           {@link InterruptedIOException#bytesTransferred} set to what had
	 *           been copied
	 */
	public static long copy(final DataHandle<Location> in,
		final DataHandle<Location> out, final long length,
		final LongConsumer progress, final int bufferSize) throws IOException
	{
		final byte[] buffer = new byte[bufferSize];
		long totalRead = 0;
		while (true) {
			// NB: cancellation is thread interruption. The check is per block, the
			// same granularity as the Task.isCanceled() check this replaces; a read
			// already under way cannot be aborted, since plain streams are not
			// interruptible.
			if (Thread.interrupted()) {
				// NB: Thread.interrupted() clears the flag, so restore it: callers
				// further up the stack must still see that this thread was cancelled.
				Thread.currentThread().interrupt();
				final InterruptedIOException exc = new InterruptedIOException(
					"Copy cancelled");
				// NB: int, per the field's type; saturate rather than wrap around.
				exc.bytesTransferred = (int) Math.min(totalRead, Integer.MAX_VALUE);
				throw exc;
			}
			final int r;
			// NB: do not read past the requested length.
			if (length > 0 && totalRead + bufferSize > length) {
				r = in.read(buffer, 0, (int) (length - totalRead));
			}
			else {
				r = in.read(buffer);
			}
			if (r <= 0) break; // EOF
			out.write(buffer, 0, r);
			totalRead += r;
			if (progress != null) progress.accept(totalRead);
		}
		return totalRead;
	}

	/**
	 * Writes a string to the specified DataOutput using modified UTF-8 encoding
	 * in a machine-independent manner.
	 * <p>
	 * First, two bytes are written to out as if by the {@code writeShort} method
	 * giving the number of bytes to follow. This value is the number of bytes
	 * actually written out, not the length of the string. Following the length,
	 * each character of the string is output, in sequence, using the modified
	 * UTF-8 encoding for the character. If no exception is thrown, the counter
	 * {@code written} is incremented by the total number of bytes written to the
	 * output stream. This will be at least two plus the length of {@code str},
	 * and at most two plus thrice the length of {@code str}.
	 * </p>
	 *
	 * @param str a string to be written.
	 * @param out destination to write to
	 * @return The number of bytes written out.
	 * @throws IOException if an I/O error occurs.
	 */
	public static int writeUTF(final String str, final DataOutput out)
		throws IOException
	{
		// Encode string as modified UTF-8 per java.io.DataOutput specification.
		final int strlen = str.length();
		int utflen = 0;
		for (int i = 0; i < strlen; i++) {
			final char c = str.charAt(i);
			if (c >= '\u0001' && c <= '\u007F') utflen += 1;
			else if (c <= '\u07FF') utflen += 2;
			else utflen += 3;
		}
		if (utflen > 65535) throw new UTFDataFormatException(
			"encoded string too long: " + utflen + " bytes");
		final byte[] bytes = new byte[utflen + 2];
		bytes[0] = (byte) ((utflen >>> 8) & 0xFF);
		bytes[1] = (byte) (utflen & 0xFF);
		int pos = 2;
		for (int i = 0; i < strlen; i++) {
			final char c = str.charAt(i);
			if (c >= '\u0001' && c <= '\u007F') {
				bytes[pos++] = (byte) c;
			}
			else if (c <= '\u07FF') {
				bytes[pos++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				bytes[pos++] = (byte) (0x80 | (c & 0x3F));
			}
			else {
				bytes[pos++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				bytes[pos++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				bytes[pos++] = (byte) (0x80 | (c & 0x3F));
			}
		}
		out.write(bytes);
		return utflen + 2;
	}
	// -- Helper methods --

	private static DataHandle<?> instantiate(final DataHandle<?> prototype) {
		try {
			return prototype.getClass().getDeclaredConstructor().newInstance();
		}
		catch (final ReflectiveOperationException exc) {
			throw new IllegalStateException("Cannot instantiate handle: " + //
				prototype.getClass().getName(), exc);
		}
	}

	private static List<DataHandle<?>> discoverHandles() {
		final List<DataHandle<?>> list = new ArrayList<>();
		ServiceLoader.load(DataHandle.class).forEach(list::add);
		return list;
	}
}
