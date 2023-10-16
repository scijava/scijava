/*-
 * #%L
 * SciJava Common 3: Functionality widely used across SciJava modules
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
package org.scijava.common3;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class Files {

	/**
	 * Gets the absolute path to the given file, with the directory separator
	 * standardized to forward slash, like most platforms use.
	 * 
	 * @param file The file whose path will be obtained and standardized.
	 * @return The file's standardized absolute path.
	 */
	public static String absolutePath(final File file) {
		final String path = file.getAbsolutePath();
		final String slash = System.getProperty("file.separator");
		return standardPath(path, slash);
	}

	/**
	 * Gets a standardized path based on the given one, with the directory
	 * separator standardized from the specific separator to forward slash, like
	 * most platforms use.
	 * 
	 * @param path The path to standardize.
	 * @param separator The directory separator to be standardized.
	 * @return The standardized path.
	 */
	public static String standardPath(final String path, final String separator) {
		// NB: Standardize directory separator (i.e., avoid Windows nonsense!).
		return path.replaceAll(Pattern.quote(separator), "/");
	}

	/**
	 * Extracts the suffix (i.e., extension) of a file.
	 * 
	 * @param file the file object
	 * @return the file extension (excluding the dot), or the empty string when
	 *         the file name does not contain dots
	 */
	public static String suffix(final File file) {
		final String name = file.getName();
		final int dot = name.lastIndexOf('.');
		if (dot < 0) return "";
		return name.substring(dot + 1);
	}

	/** Gets the {@link Date} of the file's last modification. */
	public static Date modifiedTime(final File file) {
		final long modifiedTime = file.lastModified();
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(modifiedTime);
		return c.getTime();
	}
}
