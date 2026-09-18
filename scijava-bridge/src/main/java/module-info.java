/*
 * #%L
 * Presenting SciJava Common's modules as SciJava3 commands.
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

/**
 * The one place the two worlds meet.
 * <p>
 * NB: {@code requires org.scijava} is SciJava Common, whose jar declares that
 * name itself ({@code Automatic-Module-Name: org.scijava}) - so it is stable,
 * not filename-derived. That the two can sit on the module path together at
 * all is what the {@code org.scijava.command3} rename bought: SciJava Common
 * owns the package {@code org.scijava.command}, and two modules containing one
 * package is a JVM that refuses to start.
 * </p>
 */
open module org.scijava.bridge {

	exports org.scijava.bridge;

	requires transitive org.scijava;
	requires transitive org.scijava.command3;
	requires transitive org.scijava.execute;
	requires transitive org.scijava.struct;
	requires org.scijava.common3;

}
