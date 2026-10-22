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

package org.scijava.ops.image.gpl.ridge;

import java.util.ArrayList;

/**
 * A list of lines, which also hands out the line IDs of one detection run.
 * <p>
 * Note: the plugin numbers lines with a global counter that is never reset;
 * here the IDs restart at 0 for each detection, as in the first detection of
 * a fresh plugin session.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 */
class Lines extends ArrayList<Line> {

	private final int[] idCounter;

	/** Creates an empty list with a new ID counter. */
	Lines() {
		idCounter = new int[1];
	}

	/**
	 * Creates an empty list that shares the ID counter of another list.
	 *
	 * @param ids the list whose ID counter to share
	 */
	Lines(final Lines ids) {
		idCounter = ids.idCounter;
	}

	int nextId() {
		return idCounter[0]++;
	}

	int getIndexByID(final int id) {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getID() == id) {
				return i;
			}
		}
		return -1;
	}
}
