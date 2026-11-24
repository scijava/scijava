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


package org.scijava.ops.image.lgpl;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;

/**
 * Base class for unit tests of the ops in this component, mirroring
 * {@code org.scijava.ops.image.AbstractOpTest} (which is not published in a
 * test JAR).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public abstract class AbstractOpTest {

	protected static final OpEnvironment ops = OpEnvironment.build();

	static {
		ops.setDefaultHints(new Hints("progress.TRACK"));
	}

}
