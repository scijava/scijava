/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl.morphology.attribute;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.RealType;

/**
 * Shared implementation of the attribute openings and closings of this
 * package, which prune the component tree of an image with the direct rule.
 *
 * @author Curtis Rueden
 */
final class AttributeFilters {

	private AttributeFilters() {
		// Prevent instantiation of static utility class.
	}

	/** The attributes measured on the nodes of a component tree. */
	enum Attribute {

			/** The number of pixels of the component. */
			AREA("area"),

			/**
			 * The diagonal of the bounding box of the component, in pixels.
			 */
			BOX_DIAGONAL("boxDiagonal"),

			/**
			 * The sum, over the pixels of the component, of their height above the
			 * level at which the component merges with another one.
			 */
			VOLUME("volume");

		private final String label;

		Attribute(final String label) {
			this.label = label;
		}

		/**
		 * Looks up an attribute by name, ignoring case.
		 *
		 * @param name the name of the attribute
		 * @return the attribute
		 */
		static Attribute of(final String name) {
			for (final var a : values()) {
				if (a.label.equalsIgnoreCase(name)) return a;
			}
			throw new IllegalArgumentException("Unknown attribute: " + name +
				" (expected one of " + Arrays.stream(values()).map(a -> a.label)
					.collect(Collectors.joining(", ")) + ")");
		}

		private double[] measure(final ComponentTree tree) {
			switch (this) {
				case AREA:
					return tree.areas();
				case BOX_DIAGONAL:
					return tree.boxDiagonals();
				default:
					return tree.volumes();
			}
		}
	}

	/**
	 * Computes an attribute opening or closing.
	 *
	 * @param input the image to filter
	 * @param attribute the attribute measured on the connected components
	 * @param threshold the minimal attribute value of preserved components
	 * @param connectivity the neighborhood defining which pixels are adjacent,
	 *          or null for full connectivity
	 * @param closing true for a closing (dark components), false for an opening
	 *          (bright components)
	 * @param output the image to write the result into
	 */
	static void filter(final RandomAccessibleInterval<? extends RealType<?>> input,
		final Attribute attribute, final double threshold,
		final Shape connectivity, final boolean closing,
		final RandomAccessibleInterval<? extends RealType<?>> output)
	{
		if (Double.isNaN(threshold)) {
			throw new IllegalArgumentException("Threshold must not be NaN");
		}
		final var tree = new ComponentTree(input, connectivity, closing);
		tree.write(tree.filter(attribute.measure(tree), threshold), output);
	}
}
