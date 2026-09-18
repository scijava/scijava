/*
 * #%L
 * AWT widgets and platform plumbing, with no Swing anywhere.
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

package org.scijava.awt;

import java.awt.Component;

import org.scijava.harvest.ParameterModel;
import org.scijava.harvest.ParameterNode;
import org.scijava.ui3.AbstractWidget;
import org.scijava.ui3.WidgetFactory;

/**
 * A {@link org.scijava.ui3.Widget} made of AWT components.
 * <p>
 * NB: deliberately <em>not</em> a supertype of the Swing binding's widget,
 * although {@code JComponent} is a {@code Component} and the hierarchy would
 * allow it. Discovery filters factories by
 * {@link WidgetFactory#widgetType()}, so making one a subtype of the other
 * would mean a pure-AWT application silently filling its dialogs with Swing
 * controls the moment scijava-swing appeared on the classpath - which is
 * exactly what a pure-AWT application is trying to avoid. Sibling types keep
 * that an explicit choice.
 * </p>
 *
 * @author Curtis Rueden
 */
public abstract class AwtWidget extends AbstractWidget {

	protected AwtWidget(final ParameterNode node, final ParameterModel model) {
		super(node, model);
	}

	/** Gets the component to place in the dialog. */
	public abstract Component component();
}
