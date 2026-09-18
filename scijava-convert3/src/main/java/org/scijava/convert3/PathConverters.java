/*
 * #%L
 * Converting a value to the type something else wants.
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

package org.scijava.convert3;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.scijava.common3.Types;
import org.scijava.priority.Priority;

/**
 * Converts between the three ways Java names a file.
 * <p>
 * NB: {@code String} to {@code File} would work through
 * {@link ConstructorConverter} anyway, but {@code String} to {@code Path} has
 * no constructor to find, and neither direction between {@code File} and
 * {@code Path} does. They are the same thing said three ways, and code should
 * not have to care which way a caller said it.
 * </p>
 *
 * @author Curtis Rueden
 */
public final class PathConverters {

	private PathConverters() {
		// prevent instantiation of utility class
	}

	/** Converts a file to a path. */
	public static class FileToPath implements Converter<File, Path> {

		@Override
		public Type sourceType() {
			return File.class;
		}

		@Override
		public Type destType() {
			return Path.class;
		}

		@Override
		public Object convert(final Object source, final Type dest) {
			return ((File) source).toPath();
		}

		@Override
		public double priority() {
			return Priority.HIGH;
		}
	}

	/** Converts a path to a file. */
	public static class PathToFile implements Converter<Path, File> {

		@Override
		public Type sourceType() {
			return Path.class;
		}

		@Override
		public Type destType() {
			return File.class;
		}

		@Override
		public Object convert(final Object source, final Type dest) {
			return ((Path) source).toFile();
		}

		@Override
		public double priority() {
			return Priority.HIGH;
		}
	}

	/** Converts text to a path. */
	public static class StringToPath implements Converter<String, Path> {

		@Override
		public Type sourceType() {
			return String.class;
		}

		@Override
		public Type destType() {
			return Path.class;
		}

		@Override
		public boolean supports(final Object source, final Type dest) {
			return source instanceof String && !((String) source).isEmpty() && //
				Types.raw(dest) == Path.class;
		}

		@Override
		public Object convert(final Object source, final Type dest) {
			return Paths.get((String) source);
		}

		@Override
		public double priority() {
			return Priority.HIGH;
		}
	}
}
