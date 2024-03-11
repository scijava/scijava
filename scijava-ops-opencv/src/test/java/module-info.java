/*-
 * #%L
 * SciJava Ops OpenCV: OpenCV configuration for ops
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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
module org.scijava.ops.opencv {
	exports org.scijava.ops.opencv;

	requires java.scripting;

	// FIXME: these module names derive from filenames and are thus unstable
	requires org.bytedeco.opencv;

	// HACK: Because org.scijava.io.http is not JPMSified yet.
	requires kotlin.stdlib;
	requires okhttp3;
	requires okio;

	// HACK: Because org.bytedeco.opencv is not JPMSified yet.
	requires org.bytedeco.openblas;
	requires org.bytedeco.javacpp;

	// Automatic modules.
	requires io.scif;
	requires net.imagej.opencv;
	requires net.imglib2;
	requires org.scijava.io.http;

	// JPMS modules.
	requires org.scijava.ops.api;
	requires org.scijava.ops.engine;
	requires org.scijava.ops.image;
	requires org.scijava.types;

	requires transitive org.junit.jupiter.api;
	requires transitive org.junit.jupiter.engine;
}
