
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
