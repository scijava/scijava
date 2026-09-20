/*
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.common3;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Useful methods for computing digests, and for rendering bytes as text.
 *
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public final class Digests {

	private static final char[] HEX = "0123456789abcdef".toCharArray();

	private Digests() {
		// NB: prevent instantiation of utility class.
	}

	/** Converts the given bytes to a string, assuming UTF-8 encoding. */
	public static String string(final byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/** Converts the given string to bytes, using UTF-8 encoding. */
	public static byte[] bytes(final String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}

	/** Converts the given int to bytes, most significant byte first. */
	public static byte[] bytes(final int i) {
		return new byte[] { //
			(byte) (0xff & (i >>> 24)), //
			(byte) (0xff & (i >>> 16)), //
			(byte) (0xff & (i >>> 8)), //
			(byte) (0xff & i) //
		};
	}

	/** Renders the given bytes as a lowercase hexadecimal string. */
	public static String hex(final byte[] bytes) {
		final char[] buffer = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			buffer[i * 2] = HEX[(bytes[i] & 0xf0) >> 4];
			buffer[i * 2 + 1] = HEX[bytes[i] & 0xf];
		}
		return new String(buffer);
	}

	/** Renders the given bytes as a base64 string. */
	public static String base64(final byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	/** Computes a weak but deterministic hash of the given string. */
	public static byte[] hash(final String s) {
		return bytes(s.hashCode());
	}

	/**
	 * Computes a weak but deterministic hash of the given bytes.
	 * <p>
	 * NB: the hash code of the byte array cannot be used directly, because
	 * primitive arrays inherit {@link Object#hashCode}, which differs per
	 * instance. Converting to a string first yields a consistent result.
	 * </p>
	 */
	public static byte[] hash(final byte[] bytes) {
		return hash(string(bytes));
	}

	/** Computes the SHA-1 digest of the given bytes. */
	public static byte[] sha1(final byte[] bytes) {
		return digest("SHA-1", bytes);
	}

	/** Computes the MD5 digest of the given bytes. */
	public static byte[] md5(final byte[] bytes) {
		return digest("MD5", bytes);
	}

	/**
	 * Computes the digest of the given bytes using the given algorithm.
	 *
	 * @param algorithm the digest algorithm; every Java platform is required to
	 *          support {@code MD5}, {@code SHA-1} and {@code SHA-256}
	 * @param bytes the bytes to digest
	 * @return the digest
	 * @throws IllegalArgumentException if the algorithm is not available
	 */
	public static byte[] digest(final String algorithm, final byte[] bytes) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(bytes);
			return digest.digest();
		}
		catch (final NoSuchAlgorithmException exc) {
			throw new IllegalArgumentException("No such digest algorithm: " +
				algorithm, exc);
		}
	}

	/** Computes the strongest available digest of the given string. */
	public static byte[] best(final String s) {
		return best(bytes(s));
	}

	/** Computes the strongest available digest of the given bytes. */
	public static byte[] best(final byte[] bytes) {
		return sha1(bytes);
	}

	/** Computes {@link #best(String)}, rendered as hexadecimal. */
	public static String bestHex(final String text) {
		return hex(best(text));
	}

	/** Computes {@link #best(byte[])}, rendered as hexadecimal. */
	public static String bestHex(final byte[] bytes) {
		return hex(best(bytes));
	}

	/** Computes {@link #best(String)}, rendered as base64. */
	public static String bestBase64(final String text) {
		return base64(best(text));
	}

	/** Computes {@link #best(byte[])}, rendered as base64. */
	public static String bestBase64(final byte[] bytes) {
		return base64(best(bytes));
	}
}
