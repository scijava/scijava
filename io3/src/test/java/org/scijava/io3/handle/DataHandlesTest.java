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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.scijava.io3.location.BytesLocation;
import org.scijava.io3.location.DummyLocation;
import org.scijava.io3.location.FileLocation;
import org.scijava.io3.location.Location;

/**
 * Tests {@link DataHandles}.
 *
 * @author Curtis Rueden
 */
public class DataHandlesTest {

	/** Handles are found via ServiceLoader, with no container involved. */
	@Test
	public void testDiscoveryNeedsNoContainer(@TempDir final Path dir)
		throws IOException
	{
		final File file = dir.resolve("data.txt").toFile();
		Files.write(file.toPath(), "hello".getBytes(StandardCharsets.UTF_8));

		try (final DataHandle<FileLocation> handle = DataHandles.get().create(
			new FileLocation(file)))
		{
			assertInstanceOf(FileHandle.class, handle);
			assertEquals(5, handle.length());
			final byte[] bytes = new byte[5];
			handle.read(bytes);
			assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), bytes);
		}
	}

	/** Each call yields its own handle, since a handle carries stream state. */
	@Test
	public void testHandlesAreNotShared(@TempDir final Path dir)
		throws IOException
	{
		final File file = dir.resolve("data.txt").toFile();
		Files.write(file.toPath(), "abcdef".getBytes(StandardCharsets.UTF_8));
		final FileLocation location = new FileLocation(file);

		try (final DataHandle<FileLocation> a = DataHandles.get().create(location);
				final DataHandle<FileLocation> b = DataHandles.get().create(location))
		{
			assertNotSame(a, b);
			a.seek(3);
			// NB: were these the same object, b would be at offset 3 too.
			assertEquals(0, b.offset());
		}
	}

	/** An unsupported location yields null rather than throwing. */
	@Test
	public void testUnsupportedLocation() {
		assertNull(DataHandles.get().create(new UnsupportedLocation()));
	}

	@Test
	public void testExists(@TempDir final Path dir) throws IOException {
		final File file = dir.resolve("there.txt").toFile();
		Files.write(file.toPath(), new byte[] { 1, 2, 3 });
		assertTrue(DataHandles.get().exists(new FileLocation(file)));
		assertFalse(DataHandles.get().exists(new FileLocation(dir.resolve(
			"absent.txt").toFile())));
	}

	/** Bytes locations work in memory, with no file system involved. */
	@Test
	public void testBytesHandle() throws IOException {
		final BytesLocation location = new BytesLocation(new byte[] { 10, 20, 30 });
		try (final DataHandle<BytesLocation> handle = DataHandles.get().create(
			location))
		{
			assertInstanceOf(BytesHandle.class, handle);
			assertEquals(3, handle.length());
			assertEquals(10, handle.readByte());
		}
	}

	/** Copying reports progress as a running byte count. */
	@Test
	@SuppressWarnings("unchecked")
	public void testCopyReportsProgress(@TempDir final Path dir)
		throws IOException
	{
		final File in = dir.resolve("in.bin").toFile();
		final byte[] data = new byte[5000];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) i;
		Files.write(in.toPath(), data);
		final File out = dir.resolve("out.bin").toFile();

		final List<Long> progress = new ArrayList<>();
		try (final DataHandle<Location> source = (DataHandle<Location>) //
		(DataHandle<?>) DataHandles.get().create(new FileLocation(in));
				final DataHandle<Location> target = (DataHandle<Location>) //
				(DataHandle<?>) DataHandles.get().create(new FileLocation(out)))
		{
			final long copied = DataHandles.copy(source, target, 0, progress::add,
				1024);
			assertEquals(data.length, copied);
		}
		assertArrayEquals(data, Files.readAllBytes(out.toPath()));
		assertFalse(progress.isEmpty(), "progress was never reported");
		assertEquals(data.length, progress.get(progress.size() - 1));
	}

	/**
	 * Cancellation is thread interruption: a copy stops at the next block
	 * boundary, reports how far it got, and leaves the interrupt flag set for
	 * whoever is further up the stack.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testCopyIsCancelledByInterruption(@TempDir final Path dir)
		throws Exception
	{
		final File in = dir.resolve("in.bin").toFile();
		Files.write(in.toPath(), new byte[20000]);
		final File out = dir.resolve("out.bin").toFile();

		final AtomicReference<InterruptedIOException> thrown =
			new AtomicReference<>();
		final AtomicBoolean flagStillSet = new AtomicBoolean();
		final CountDownLatch started = new CountDownLatch(1);

		final Thread worker = new Thread(() -> {
			try (final DataHandle<Location> source = (DataHandle<Location>) //
			(DataHandle<?>) DataHandles.get().create(new FileLocation(in));
					final DataHandle<Location> target = (DataHandle<Location>) //
					(DataHandle<?>) DataHandles.get().create(new FileLocation(out)))
			{
				started.countDown();
				// NB: one byte at a time, so the copy is still running when the
				// interrupt lands.
				DataHandles.copy(source, target, 0, bytes -> {
					try {
						Thread.sleep(1);
					}
					catch (final InterruptedException exc) {
						Thread.currentThread().interrupt();
					}
				}, 1);
			}
			catch (final InterruptedIOException exc) {
				thrown.set(exc);
				flagStillSet.set(Thread.currentThread().isInterrupted());
			}
			catch (final IOException exc) {
				throw new AssertionError(exc);
			}
		});
		worker.start();
		assertTrue(started.await(5, TimeUnit.SECONDS));
		Thread.sleep(50);
		worker.interrupt();
		worker.join(5000);

		final InterruptedIOException exc = thrown.get();
		assertNotNull(exc, "copy was not cancelled");
		assertTrue(exc.bytesTransferred > 0, "no partial progress reported");
		assertTrue(exc.bytesTransferred < 20000, "copy ran to completion");
		assertTrue(flagStillSet.get(), "the interrupt flag must be restored");
	}

	/** Where two handles support a location, the higher priority one wins. */
	@Test
	public void testPriorityOrdering() {
		final DataHandle<DummyLocation> low = new DummyHandle();
		final DataHandle<DummyLocation> high = new DummyHandle() {

			@Override
			public double priority() {
				return 100;
			}
		};
		final List<DataHandle<?>> both = new ArrayList<>(List.of(low, high));
		final DataHandles handles = new DataHandles(both);
		assertEquals(high.getClass(), handles.handles().get(0).getClass());
	}

	/** A location type no handle claims. */
	private static class UnsupportedLocation implements Location {
		// NB: no implementation needed.
	}
}
