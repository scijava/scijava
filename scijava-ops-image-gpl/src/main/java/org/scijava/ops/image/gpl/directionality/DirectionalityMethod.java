/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.directionality;

/**
 * Methods for measuring the local orientation of structures, as in the Fiji
 * Directionality plugin.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jean-Yves Tinevez
 */
public enum DirectionalityMethod {

		/**
		 * Fourier components analysis: the image is cut into square blocks whose
		 * power spectra are measured in angular wedges, using the filters of Liu
		 * (Appl. Opt. 30(11), 1991).
		 */
		FOURIER_COMPONENTS,

		/**
		 * Local gradient orientation: the orientation of the 5x5 Sobel gradient
		 * at each pixel, weighted by the squared gradient magnitude.
		 */
		LOCAL_GRADIENT_ORIENTATION
}
