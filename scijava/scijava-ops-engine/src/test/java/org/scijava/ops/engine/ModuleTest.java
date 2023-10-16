/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class ModuleTest {

	String moduleBaseDirectory = "src/main/java/org/scijava";

	/**
	 * Recursively finds the name of each package within the module org.scijava.ops
	 * 
	 * @param set     the set that will be populated with the module names
	 * @param dirName the starting directory
	 */
	private void findPackageNames(Set<String> set, String dirName) {
		File directory = new File(dirName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				String path = file.getPath();
				String packName = path.substring(path.indexOf("org"), path.lastIndexOf(File.separator));
				set.add(packName.replace(File.separator, "."));
			} else if (file.isDirectory()) {
				findPackageNames(set, file.getAbsolutePath());
			}
		}
	}

	private Set<String> getPackagesExported(String path) {
		try (Stream<String> stream = Files.lines(Paths.get(path))) {
			// remove outside whitespace
			Set<String> exportedPackages = stream.map(str -> str.trim())
					// consider only opens
					.filter(str -> str.startsWith("opens"))
					// consider only opens to therapi
					.filter(str -> str.contains("therapi.runtime.javadoc"))
					// get the package from the opens
					.map(str -> str.split(" ")[1]).collect(Collectors.toSet());
			return exportedPackages;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
