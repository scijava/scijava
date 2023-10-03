
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
