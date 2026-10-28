/*
 * #%L
 * Swing widgets, and a dialog to harvest inputs with them.
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

package org.scijava.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

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
 * Asks the user for a run's remaining inputs, in a Swing dialog.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Preprocessor.class)
public class SwingInputHarvester extends InputHarvester {

	private static final Logger log = LoggerFactory.getLogger(
		SwingInputHarvester.class);

	@Dependency(required = false)
	private Context context;

	private List<WidgetFactory<SwingWidget>> factories;

	private String title = "Inputs";

	/** Creates a harvester whose widgets come from the container. */
	public SwingInputHarvester() {
		// NB: the container injects the context and discovers the widgets.
	}

	/** Creates a harvester with the given widgets, and no container. */
	public SwingInputHarvester(
		final List<WidgetFactory<SwingWidget>> factories)
	{
		this.factories = List.copyOf(factories);
	}

	/** Creates a harvester whose widgets come from the given container. */
	public static SwingInputHarvester of(final Context context) {
		final SwingInputHarvester harvester = new SwingInputHarvester();
		harvester.context = context;
		return harvester;
	}

	/** Sets the title the dialog shows. */
	public void setTitle(final String title) {
		this.title = title;
	}

	@Override
	public boolean harvest(final ParameterModel model) {
		final AtomicBoolean accepted = new AtomicBoolean();
		try {
			// NB: harvesting happens on whichever thread is running the
			// preprocessors, which is not the event dispatch thread; building the
			// dialog there and showing it from there would be two bugs at once.
			final SwingDialog[] dialog = new SwingDialog[1];
			SwingUtilities.invokeAndWait(() -> dialog[0] = new SwingDialog(null,
				title, model, factories()));
			SwingUtilities.invokeAndWait(() -> accepted.set(dialog[0]
				.showDialog()));
		}
		catch (final InterruptedException exc) {
			Thread.currentThread().interrupt();
			return false;
		}
		catch (final InvocationTargetException exc) {
			throw new IllegalStateException("Failed to harvest inputs", exc);
		}
		return accepted.get();
	}

	// -- Helper methods --

	/** Gets the widgets available, discovering them on first use. */
	private synchronized List<WidgetFactory<SwingWidget>> factories() {
		if (factories != null) return factories;
		final List<WidgetFactory<SwingWidget>> found = new ArrayList<>();
		if (context != null) {
			for (final Discovery<WidgetFactory> discovery : context.plugins(
				WidgetFactory.class))
			{
				final WidgetFactory<?> factory = discovery.get();
				// NB: every toolkit's widgets are discovered together, so keep the
				// ones this dialog can actually place.
				if (!SwingWidget.class.isAssignableFrom(factory.widgetType())) continue;
				@SuppressWarnings("unchecked")
				final WidgetFactory<SwingWidget> swing =
					(WidgetFactory<SwingWidget>) factory;
				found.add(swing);
			}
		}
		if (found.isEmpty()) {
			// NB: the usual cause is a missing annotation index, which javac
			// produces only when scijava-index is on the annotation processor path
			// -- and says nothing at all when it is not. The dialog would otherwise
			// come up empty, with no clue as to why.
			log.warn("No Swing widget factories discovered. Is the annotation " +
				"index (META-INF/json/org.scijava.context.Plugin) on the classpath?");
		}
		else if (log.isDebugEnabled()) {
			found.forEach(f -> log.debug("Widget factory: {}", f.getClass()
				.getName()));
		}
		factories = List.copyOf(found);
		return factories;
	}
}
