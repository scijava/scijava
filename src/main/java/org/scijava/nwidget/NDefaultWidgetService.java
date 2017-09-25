/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.nwidget;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.Service;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.StructInstance;

@Plugin(type = Service.class)
public class NDefaultWidgetService extends
	AbstractSingletonService<NWidgetFactory<?>> implements NWidgetService
{

	@Parameter
	private PluginService pluginService;

	@Override
	public <C, W extends NWidget> NWidgetPanel<C> createPanel(
		final StructInstance<C> structInstance,
		final Predicate<MemberInstance<?>> included,
		final Predicate<MemberInstance<?>> required,
		final NWidgetPanelFactory<C, W> factory)
	{
		final ArrayList<W> widgets = createWidgets(structInstance, factory
			.widgetType(), included, required);

		return factory.create(structInstance, widgets);
	}

	// -- Helper methods --

	private <W extends NWidget> ArrayList<W> createWidgets(
		final StructInstance<?> structInstance, final Class<W> widgetType,
		final Predicate<MemberInstance<?>> included,
		final Predicate<MemberInstance<?>> required)
	{
		final ArrayList<W> widgets = new ArrayList<>();

		for (final MemberInstance<?> memberInstance : structInstance.members()) {
			if (!included.test(memberInstance)) continue;

			final W widget = createWidget(memberInstance, widgetType);
			if (widget == null && required.test(memberInstance)) {
				// fail - FIXME
				throw new RuntimeException(memberInstance + " is required but none exist.");
			}
			if (widget != null) widgets.add(widget);
		}
		return widgets;
	}

	private <T extends NWidget> T createWidget(
		final MemberInstance<?> memberInstance, final Class<T> widgetType)
	{
		for (final NWidgetFactory<?> factory : getInstances()) {
			if (!widgetType.isAssignableFrom(factory.widgetType())) continue;
			if (!factory.supports(memberInstance)) continue;
			@SuppressWarnings("unchecked")
			final T tWidget = (T) factory.create(memberInstance);
			return tWidget;
		}
		return null;
	}
}
