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

package org.scijava.ops.image.gpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;

/**
 * Guards against ops silently dropped by the op indexer (e.g. after an
 * apostrophe in an {@code @param} description), like
 * {@code org.scijava.ops.image.OpRegressionTest}.
 * <p>
 * Only the op infos implemented in this module are counted, i.e. those whose
 * implementation name starts with this package, so the count does not change
 * with the ops of the other modules on the classpath. As in the other test,
 * each {@code @Nullable} parameter adds a reduced op info per signature.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class OpRegressionTest {

	private static final String PREFIX = "org.scijava.ops.image.gpl.";

	protected static final OpEnvironment ops = OpEnvironment.build();

	@Test
	public void testOpDiscoveryRegression() {
		final long expected = 47;
		final long actual = ops.infos().stream() //
			.filter(info -> info.implementationName().startsWith(PREFIX)) //
			.count();
		assertEquals(expected, actual);
	}

	@Test
	public void testOpDescriptionRegression() {
		for (final OpInfo info : ops.infos()) {
			if (!info.implementationName().startsWith(PREFIX)) continue;
			assertNotNull(info.description(), () -> "Info from " + info.id() +
				" has a null description");
		}
	}
}
