/*
 * Copyright 2011 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.tsdb.tsdash.client.plot;

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.client.model.Metric;
import com.facebook.tsdb.tsdash.client.model.MetricHeader;
import com.facebook.tsdb.tsdash.client.model.TimeRange;
import com.facebook.tsdb.tsdash.client.service.HTTPService;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public abstract class Plot {

    protected HandlerManager eventBus;
    protected HasWidgets container;

    private Widget current = null;
    private Widget newPlot = null;

    public Plot(HandlerManager eventBus, HasWidgets container) {
        this.eventBus = eventBus;
        this.container = container;
    }

    protected void startRender(Widget plot) {
        if (newPlot != null && !newPlot.isVisible()) {
            // this is when the new plot has not been loaded successfully and
            // it's mostly performed for cleanup
            container.remove(newPlot);
        }
        newPlot = plot;
        container.add(plot);
    }

    protected void endRender(Widget plot) {
        if (plot != newPlot) {
            return;
        }
        if (current != null) {
            container.remove(current);
        }
        current = newPlot;
        newPlot = null;
    }

    public Widget getWidget() {
        return current;
    }

    public boolean isRendered() {
        return current != null;
    }

    public abstract void render(final Command onRendered);

    public abstract void loadAndRender(TimeRange timeRange,
            ArrayList<Metric> metrics, HTTPService service,
            AsyncCallback<ArrayList<MetricHeader>> callback, Command onRender);
}
