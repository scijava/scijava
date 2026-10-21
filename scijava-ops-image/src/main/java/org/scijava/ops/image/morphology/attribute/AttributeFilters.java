/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.morphology.attribute;

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
