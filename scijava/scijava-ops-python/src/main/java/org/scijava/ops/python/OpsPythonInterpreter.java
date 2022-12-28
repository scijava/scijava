package org.scijava.ops.python;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jep.Interpreter;
import jep.MainInterpreter;
import jep.SharedInterpreter;

public class OpsPythonInterpreter {

	private static OpsPythonInterpreter instance;
	private final Interpreter interp;

	private OpsPythonInterpreter() {
		// TODO: To use Numpy, I have to set the environment variable
		// LD_PRELOAD=~/miniconda3/envs/scijava-ops-python/lib/libpython3.11.so
		// Can we automate this?
		// See https://github.com/ninia/jep/issues/338
		try {
			MainInterpreter.setJepLibraryPath(getJepPath());
		} catch(IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		interp = new SharedInterpreter();
	}

	public static Interpreter interpreter() {
		if (instance == null) {
			instance = new OpsPythonInterpreter();
		}
		return instance.interp;
	}

	private static String getJepPath() throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder("conda",
				"run", "-n", "scijava-ops-python", "python", "jep_path.py");
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();
		StringBuilder processOutput = new StringBuilder();

		try (BufferedReader processOutputReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));)
		{
			String readLine;

			while ((readLine = processOutputReader.readLine()) != null)
			{
				processOutput.append(readLine + System.lineSeparator());
			}

			process.waitFor();
		}

		return processOutput.toString().trim();

	}

}
