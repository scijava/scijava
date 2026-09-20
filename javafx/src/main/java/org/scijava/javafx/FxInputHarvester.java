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

import java.util.ArrayList;
import java.util.List;

import org.scijava.context.Context;
import org.scijava.context.Dependency;
import org.scijava.context.Plugin;
import org.scijava.discovery.Discovery;
import org.scijava.execute.Preprocessor;
import org.scijava.harvest.ParameterModel;
import org.scijava.ui3.InputHarvester;
import org.scijava.ui3.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asks the user for a run's remaining inputs, in a JavaFX dialog.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Preprocessor.class)
public class FxInputHarvester extends InputHarvester {

	private static final Logger log = LoggerFactory.getLogger(
		FxInputHarvester.class);

	@Dependency(required = false)
	private Context context;

	private List<WidgetFactory<FxWidget>> factories;

	private String title = "Inputs";

	/** Creates a harvester whose widgets come from the container. */
	public FxInputHarvester() {
		// NB: the container injects the context and discovers the widgets.
	}

	/** Creates a harvester with the given widgets, and no container. */
	public FxInputHarvester(final List<WidgetFactory<FxWidget>> factories) {
		this.factories = List.copyOf(factories);
	}

	/** Creates a harvester whose widgets come from the given container. */
	public static FxInputHarvester of(final Context context) {
		final FxInputHarvester harvester = new FxInputHarvester();
		harvester.context = context;
		return harvester;
	}

	/** Sets the title the dialog shows. */
	public void setTitle(final String title) {
		this.title = title;
	}

	@Override
	public boolean harvest(final ParameterModel model) {
		// NB: building and showing are one hop, unlike Swing's two: showAndWait
		// runs a nested event loop, so the dialog must be built and shown by the
		// same visit to the JavaFX thread.
		return FxThread.get(() -> new FxDialog(null, title, model, factories())
			.showDialog());
	}

	// -- Helper methods --

	/** Gets the widgets available, discovering them on first use. */
	private synchronized List<WidgetFactory<FxWidget>> factories() {
		if (factories != null) return factories;
		final List<WidgetFactory<FxWidget>> found = new ArrayList<>();
		if (context != null) {
			for (final Discovery<WidgetFactory> discovery : context.plugins(
				WidgetFactory.class))
			{
				final WidgetFactory<?> factory = discovery.get();
				if (!FxWidget.class.isAssignableFrom(factory.widgetType())) continue;
				@SuppressWarnings("unchecked")
				final WidgetFactory<FxWidget> fx = (WidgetFactory<FxWidget>) factory;
				found.add(fx);
			}
		}
		if (found.isEmpty()) {
			log.warn("No JavaFX widget factories discovered. Is the annotation " +
				"index (META-INF/json/org.scijava.context.Plugin) on the classpath?");
		}
		factories = List.copyOf(found);
		return factories;
	}
}
