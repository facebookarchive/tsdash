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
package com.facebook.tsdb.tsdash.client.presenter;

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.client.event.ErrorEvent;
import com.facebook.tsdb.tsdash.client.event.GraphEvent;
import com.facebook.tsdb.tsdash.client.event.MetricHeaderEvent;
import com.facebook.tsdb.tsdash.client.event.StateChangeEvent;
import com.facebook.tsdb.tsdash.client.event.StateChangeHandler;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.facebook.tsdb.tsdash.client.model.MetricHeader;
import com.facebook.tsdb.tsdash.client.model.ApplicationState.TimeMode;
import com.facebook.tsdb.tsdash.client.plot.ImagePlot;
import com.facebook.tsdb.tsdash.client.plot.InteractivePlot;
import com.facebook.tsdb.tsdash.client.plot.Plot;
import com.facebook.tsdb.tsdash.client.service.HTTPService;
import com.facebook.tsdb.tsdash.client.ui.CssHelper;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class GraphPresenter implements Presenter {

    public interface GraphWidget {
        HasWidgets container();

        public void setDumpJSON(String jsonDump);

        void setReplacementVisible(boolean visible);
    }

    private final HandlerManager eventBus;
    private final HTTPService service;
    private final GraphWidget widget;

    private Plot plot;
    private final ImagePlot imagePlot;
    private final InteractivePlot interactivePlot;
    private boolean initialized = false;

    public GraphPresenter(HandlerManager eventBus, HTTPService service,
            GraphWidget widget) {
        this.eventBus = eventBus;
        this.service = service;
        this.widget = widget;
        imagePlot = new ImagePlot(eventBus, widget.container());
        interactivePlot = new InteractivePlot(eventBus, widget.container());
        listenApplicatioStateChange();
    }

    private void resetDimensions(ApplicationState appState) {
        Widget w = (Widget) widget.container();
        w.setHeight(estimateHeight(appState) + "px");
        w.setWidth(estimateWidth(appState) + "px");
    }

    private void renderPlot(ApplicationState appState) {
        if (!appState.hasPlot()) {
            return;
        }
        widget.setReplacementVisible(false);
        if (appState.timeMode == TimeMode.HISTORY) {
            appState.timeRange = appState.getAndUpdateTimeRange();
        }
        eventBus.fireEvent(new GraphEvent(GraphEvent.Action.LOADING_DATA));
        // mass-disable any input
        CssHelper.toggleClass(RootPanel.get(), "inputdisabled");
        // launch the load-and-render
        plot.loadAndRender(appState.timeRange, appState.metrics, service,
                new AsyncCallback<ArrayList<MetricHeader>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // fire error event
                        eventBus.fireEvent(new ErrorEvent(caught));
                        // stop the loading
                        eventBus.fireEvent(new GraphEvent(
                                GraphEvent.Action.LOADED));
                        // mass re-enable the input elements
                        CssHelper.toggleClass(RootPanel.get(),
                                "inputdisabled");
                    }

                    @Override
                    public void onSuccess(ArrayList<MetricHeader> headers) {
                        eventBus.fireEvent(new GraphEvent(
                                GraphEvent.Action.LOADED));
                        CssHelper.toggleClass(RootPanel.get(),
                                "inputdisabled");
                        // do the refresh
                        eventBus.fireEvent(new MetricHeaderEvent(headers));
                    }
                }, new Command() {
                    @Override
                    public void execute() {
                        eventBus.fireEvent(new GraphEvent(
                                GraphEvent.Action.START_RENDERING));
                    }
                });
    }

    private void listenApplicatioStateChange() {
        eventBus.addHandler(StateChangeEvent.TYPE, new StateChangeHandler() {

            @Override
            public void onViewChange(StateChangeEvent event) {
                // ignore
            }

            @Override
            public void onMetricChange(StateChangeEvent event) {
                renderPlot(event.getAppState());
            }

            @Override
            public void onTimeChange(StateChangeEvent event) {
                renderPlot(event.getAppState());
            }

            @Override
            public void onPlotParamsChange(StateChangeEvent event) {
                ApplicationState appState = event.getAppState();
                Plot newPlot = appState.interactive ? interactivePlot
                        : imagePlot;
                if (newPlot != plot && plot.isRendered()) {
                    widget.container().remove(plot.getWidget());
                }
                plot = newPlot;
                imagePlot.setOptions(appState.surface, appState.palette);
                renderPlot(appState);
            }

            @Override
            public void onAutoReloadChange(StateChangeEvent event) {
                // ignore
            }

            @Override
            public void onScreenChange(StateChangeEvent event) {
                resetDimensions(event.getAppState());
                eventBus.fireEvent(new GraphEvent(
                        GraphEvent.Action.START_RENDERING));
                plot.render(new Command() {
                    @Override
                    public void execute() {
                        eventBus.fireEvent(new GraphEvent(
                                GraphEvent.Action.LOADED));
                    }
                });
            }
        });
    }

    private int estimateHeight(ApplicationState appState) {
        // header + auto relaad
        int topHeight = appState.fullscreen ? 50 : 50 + 50 + 10;
        return Window.getClientHeight() - topHeight;
    }

    private int estimateWidth(ApplicationState appState) {
        double coef = appState.fullscreen ? 1.0 : 0.72;
        return (int) ((Window.getClientWidth() - 20) * coef);
    }

    @Override
    public void go(HasWidgets container, final ApplicationState appState) {
        Widget w = (Widget) widget;
        resetDimensions(appState);
        container.add(w);
        plot = appState.interactive ? interactivePlot : imagePlot;
        imagePlot.setOptions(appState.surface, appState.palette);
        if (!initialized) {
            w.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    if (event.isAttached()) {
                        renderPlot(appState);
                        initialized = true;
                    }
                }
            });
        }
    }

}
