/*
 * #%L
 * JavaFX widgets, and a dialog to harvest inputs with them.
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

package org.scijava.javafx;

import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import org.junit.jupiter.api.condition.DisabledIf;
import org.scijava.command3.MenuTree;
import org.scijava.ui3.test.MenuConformance;

/**
 * Runs the shared menu conformance suite against the JavaFX menus.
 *
 * @author Curtis Rueden
 */
@DisabledIf("isHeadless")
public class FxMenusTest extends MenuConformance<MenuBar> {

	static boolean isHeadless() {
		return FxWidgetsTest.isHeadless();
	}

	@Override
	protected MenuBar menuBar(final MenuTree root) {
		return FxThread.get(() -> FxMenus.create(root, command -> {}));
	}

	@Override
	protected List<String> topMenus(final MenuBar bar) {
		return bar.getMenus().stream().map(Menu::getText) //
			.collect(Collectors.toList());
	}

	@Override
	protected List<String> items(final MenuBar bar, final String... path) {
		Menu menu = bar.getMenus().stream() //
			.filter(m -> m.getText().equals(path[0])).findFirst() //
			.orElseThrow(() -> new AssertionError("No such menu: " + path[0]));
		for (int i = 1; i < path.length; i++) {
			menu = (Menu) item(menu, path[i]);
		}
		return menu.getItems().stream().map(MenuItem::getText) //
			.collect(Collectors.toList());
	}

	// -- Helper methods --

	private static MenuItem item(final Menu menu, final String label) {
		return menu.getItems().stream() //
			.filter(i -> i.getText().equals(label)).findFirst() //
			.orElseThrow(() -> new AssertionError("No such item: " + label));
	}
}
