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

import com.facebook.tsdb.tsdash.client.event.LogEvent;
import com.facebook.tsdb.tsdash.client.model.Metric;
import com.facebook.tsdb.tsdash.client.model.MetricHeader;
import com.facebook.tsdb.tsdash.client.model.PlotResponse;
import com.facebook.tsdb.tsdash.client.model.TimeRange;
import com.facebook.tsdb.tsdash.client.service.HTTPService;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class ImagePlot extends Plot {

    private String url = "";

    public ImagePlot(HandlerManager eventBus, HasWidgets container) {
        super(eventBus, container);
    }

    protected boolean surface = false;
    protected boolean palette = false;

    public void setOptions(boolean surface, boolean palette) {
        this.surface = surface;
        this.palette = palette;
    }

    @Override
    public void loadAndRender(TimeRange timeRange, ArrayList<Metric> metrics,
            HTTPService service,
            final AsyncCallback<ArrayList<MetricHeader>> callback,
            final Command onRender) {
        Widget w = (Widget) container;
        int width = w.getOffsetWidth();
        int height = w.getOffsetHeight();
        service.loadPlot(timeRange, metrics, width, height, surface, palette,
                new AsyncCallback<PlotResponse>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(final PlotResponse result) {
                        eventBus.fireEvent(new LogEvent("Plot Load", ""
                                + result));
                        url = result.plotURL;
                        onRender.execute();
                        render(new Command() {
                            @Override
                            public void execute() {
                                callback.onSuccess(result.metrics);
                            }
                        });
                    }
                });
    }

    @Override
    public void render(final Command onRendered) {
        final Image newImage = new Image(url);
        newImage.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                endRender(newImage);
                newImage.setVisible(true);
                onRendered.execute();
            }
        });
        newImage.setVisible(false);
        startRender(newImage);
    }
}
