/*-
 * #%L
 * Extensible serialization mechanism for persisting objects.
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

package org.scijava.persist;

import org.scijava.plugin.Plugin;

public class Shapes {

	public static class Rectangle implements Shape {

		public int width;
		public int height;
	}

	public static class Diamond implements Shape {

		public int width;
		public int height;
	}

	public static class Drawing {

		public Shape bottomShape;
		public Shape middleShape;
		public Shape topShape;
	}

	/**
	 * It's not necessarily a good idea to put adapters as inner classes, but if
	 * you do, make sure the class is static or you will get an
	 * {@link InstantiationError}.
	 */
	@Plugin(type = IClassRuntimeAdapter.class)
	public static class RectangleAdapter implements
		IClassRuntimeAdapter<Shape, Rectangle>
	{

		@Override
		public Class<? extends Shape> getBaseClass() {
			return Shape.class;
		}

		@Override
		public Class<? extends Rectangle> getRunTimeClass() {
			return Rectangle.class;
		}

	}

	@Plugin(type = IClassRuntimeAdapter.class)
	public static class DiamondAdapter implements
		IClassRuntimeAdapter<Shape, Diamond>
	{

		@Override
		public Class<? extends Shape> getBaseClass() {
			return Shape.class;
		}

		@Override
		public Class<? extends Diamond> getRunTimeClass() {
			return Diamond.class;
		}

	}

}
