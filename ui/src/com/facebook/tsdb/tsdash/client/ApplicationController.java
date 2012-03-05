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
package com.facebook.tsdb.tsdash.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.facebook.tsdb.tsdash.client.event.AutoReloadEvent;
import com.facebook.tsdb.tsdash.client.event.AutoReloadEventHandler;
import com.facebook.tsdb.tsdash.client.event.KeyboardShortcutEvent;
import com.facebook.tsdb.tsdash.client.event.KeyboardShortcutHandler;
import com.facebook.tsdb.tsdash.client.event.MetricEvent;
import com.facebook.tsdb.tsdash.client.event.MetricEventHandler;
import com.facebook.tsdb.tsdash.client.event.PlotOptionsEvent;
import com.facebook.tsdb.tsdash.client.event.PlotOptionsEventHandler;
import com.facebook.tsdb.tsdash.client.event.StateChangeEvent;
import com.facebook.tsdb.tsdash.client.event.TagEvent;
import com.facebook.tsdb.tsdash.client.event.TagEventHandler;
import com.facebook.tsdb.tsdash.client.event.TimeRangeChangeEvent;
import com.facebook.tsdb.tsdash.client.event.TimeRangeChangeEventHandler;
import com.facebook.tsdb.tsdash.client.event.ViewChangeEvent;
import com.facebook.tsdb.tsdash.client.event.ViewChangeEventHandler;
import com.facebook.tsdb.tsdash.client.event.StateChangeEvent.StateChange;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.facebook.tsdb.tsdash.client.model.Metric;
import com.facebook.tsdb.tsdash.client.presenter.AutoreloadPresenter;
import com.facebook.tsdb.tsdash.client.presenter.ErrorPresenter;
import com.facebook.tsdb.tsdash.client.presenter.GraphPresenter;
import com.facebook.tsdb.tsdash.client.presenter.LogPresenter;
import com.facebook.tsdb.tsdash.client.presenter.MetricPresenter;
import com.facebook.tsdb.tsdash.client.presenter.PlotOptionsPresenter;
import com.facebook.tsdb.tsdash.client.presenter.Presenter;
import com.facebook.tsdb.tsdash.client.presenter.TimePresenter;
import com.facebook.tsdb.tsdash.client.presenter.TopMenuPresenter;
import com.facebook.tsdb.tsdash.client.presenter.WrapPresenter;
import com.facebook.tsdb.tsdash.client.service.HTTPService;
import com.facebook.tsdb.tsdash.client.ui.AutoreloadWidget;
import com.facebook.tsdb.tsdash.client.ui.ErrorWidget;
import com.facebook.tsdb.tsdash.client.ui.GraphWidget;
import com.facebook.tsdb.tsdash.client.ui.LogWidget;
import com.facebook.tsdb.tsdash.client.ui.MetricsFormWidget;
import com.facebook.tsdb.tsdash.client.ui.PlotOptionsWidget;
import com.facebook.tsdb.tsdash.client.ui.SelectTimeWidget;
import com.facebook.tsdb.tsdash.client.ui.TopMenuWidget;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootPanel;

public class ApplicationController implements ValueChangeHandler<String>,
        Presenter {

    private final HandlerManager eventBus;
    private final HTTPService service;
    private HasWidgets topContainer = null;
    private HasWidgets viewContainer = null;
    private HasWidgets errorContainer = null;

    private final TopMenuPresenter topPresenter;
    private final ErrorPresenter errorPresenter;
    private final MetricPresenter metricPresenter;
    private final Map<String, ArrayList<Presenter>> viewPresenters =
        new HashMap<String, ArrayList<Presenter>>();
    private ApplicationState appState = new ApplicationState();

    private long keyShortcutTs = 0;

    public ApplicationController(HandlerManager eventBus, HTTPService service) {
        this.eventBus = eventBus;
        this.service = service;
        History.addValueChangeHandler(this);
        listenViewChange();
        listenTimeRangeChange();
        listenMetricEvents();
        listenPlotOptionsEvents();
        listenCtrlF();
        bindKeyShortcuts();
        bindAutoReload();
        topPresenter = new TopMenuPresenter(eventBus, new TopMenuWidget());
        errorPresenter = new ErrorPresenter(eventBus, new ErrorWidget());
        // create view presenters
        // graph
        ArrayList<Presenter> graphPresenters = new ArrayList<Presenter>();
        WrapPresenter graphMenu = new WrapPresenter("graphMenu");
        WrapPresenter graphContent = new WrapPresenter("graphContent");
        graphMenu.add(new TimePresenter(eventBus, new SelectTimeWidget()));
        graphMenu.add(new PlotOptionsPresenter(eventBus,
                new PlotOptionsWidget()));
        metricPresenter = new MetricPresenter(eventBus, service,
                new MetricsFormWidget());
        graphMenu.add(metricPresenter);
        graphContent.add(new AutoreloadPresenter(eventBus,
                new AutoreloadWidget()));
        graphContent.add(new GraphPresenter(eventBus, service,
                new GraphWidget()));
        graphPresenters.add(graphMenu);
        graphPresenters.add(graphContent);
        // log
        ArrayList<Presenter> logPresenters = new ArrayList<Presenter>();
        logPresenters.add(new LogPresenter(eventBus, new LogWidget()));
        // set the mapping
        viewPresenters.put(ViewChangeEvent.View.GRAPH.toString(),
                graphPresenters);
        viewPresenters.put(ViewChangeEvent.View.LOG.toString(), logPresenters);
    }

    private void fireStateChange(StateChange stateChange) {
        History.newItem(appState.toJSON(), false);
        eventBus.fireEvent(new StateChangeEvent(stateChange, appState));
    }

    public void go() {
        this.topContainer = RootPanel.get("topmenu");
        this.viewContainer = RootPanel.get("content");
        this.errorContainer = RootPanel.get("error");
        fireInit();
    }

    private void listenViewChange() {
        eventBus.addHandler(ViewChangeEvent.TYPE, new ViewChangeEventHandler() {
            @Override
            public void onChange(ViewChangeEvent event) {
                appState.view = event.getView();
                // update the selected item from the top menu
                topPresenter.setSelected(event.getView());
                displayView();
                // fire the view state change
                fireStateChange(StateChange.VIEW);
            }
        });
    }

    private void listenTimeRangeChange() {
        eventBus.addHandler(TimeRangeChangeEvent.TYPE,
                new TimeRangeChangeEventHandler() {
                    @Override
                    public void onChange(TimeRangeChangeEvent event) {
                        appState.timeMode = event.getMode();
                        appState.timeRange = event.getTimeRange();
                        fireStateChange(StateChange.TIME);
                    }
                });
    }

    private void listenMetricEvents() {
        eventBus.addHandler(MetricEvent.TYPE, new MetricEventHandler() {
            @Override
            public void onAdd(final MetricEvent event) {
                Metric newMetric = event.getMetric();
                if (!newMetric.isPlottable()) {
                    // only plot-able metrics in the state
                    return;
                }
                appState.metrics.add(newMetric);
                fireStateChange(StateChange.METRIC);
            }

            @Override
            public void onDelete(MetricEvent event) {
                if (!event.getMetric().isPlottable()) {
                    return;
                }
                appState.metrics.remove(event.getMetric());
                fireStateChange(StateChange.METRIC);
            }

            @Override
            public void onToggle(MetricEvent event) {
                if (!event.getMetric().isPlottable()) {
                    return;
                }
                fireStateChange(StateChange.METRIC);
            }

            @Override
            public void onAggregatorChange(MetricEvent event) {
                if (!event.getMetric().isPlottable()) {
                    return;
                }
                fireStateChange(StateChange.METRIC);
            }
        });
        // metric tag events
        eventBus.addHandler(TagEvent.TYPE, new TagEventHandler() {
            @Override
            public void onSet(TagEvent event) {
                if (!event.getMetric().isPlottable()) {
                    return;
                }
                fireStateChange(StateChange.METRIC);
            }

            @Override
            public void onRemove(TagEvent event) {
                if (!event.getMetric().isPlottable()) {
                    return;
                }
                fireStateChange(StateChange.METRIC);
            }
        });
    }

    private void listenPlotOptionsEvents() {
        eventBus.addHandler(PlotOptionsEvent.TYPE,
                new PlotOptionsEventHandler() {
                    @Override
                    public void onPlotOptionsChange(PlotOptionsEvent event) {
                        appState.interactive = event.isInteractive();
                        appState.surface = event.surfaceEnabled();
                        appState.palette = event.colorPaletteEnabled();
                        fireStateChange(StateChange.PLOT);
                    }
                });
    }

    private void bindAutoReload() {
        eventBus.addHandler(AutoReloadEvent.TYPE, new AutoReloadEventHandler() {
            @Override
            public void onPeriodChange(AutoReloadEvent event) {
                appState.reloadPeriod = event.getPeriod();
                History.newItem(appState.toJSON(), false);
            }

            @Override
            public void onLaunch(AutoReloadEvent event) {
                appState.timeRange.move(event.getPeriod());
                fireStateChange(StateChange.TIME);
            }

            @Override
            public void onEnable(AutoReloadEvent event) {
                appState.autoReload = event.isAutoReloading();
                History.newItem(appState.toJSON(), false);
            }
        });
    }

    private void bindKeyShortcuts() {
        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                NativeEvent nativeEvent = event.getNativeEvent();
                if (nativeEvent.getCtrlKey() && nativeEvent.getKeyCode() == ' ') {
                    nativeEvent.preventDefault();
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            long now = (new Date()).getTime();
                            if (now - keyShortcutTs < 500) {
                                return;
                            }
                            keyShortcutTs = now;
                            if (appState.needsAutoreload()) {
                                eventBus.fireEvent(new KeyboardShortcutEvent(
                                        KeyboardShortcutEvent.Shortcut.CTRL_SPACE));
                            }
                        }
                    });
                } else if (nativeEvent.getCtrlKey()
                        && nativeEvent.getKeyCode() == 'F') {
                    nativeEvent.preventDefault();
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            long now = (new Date()).getTime();
                            if (now - keyShortcutTs < 500) {
                                return;
                            }
                            keyShortcutTs = now;
                            eventBus.fireEvent(new KeyboardShortcutEvent(
                                    KeyboardShortcutEvent.Shortcut.CTRL_F));
                        }
                    });
                }
            }
        });
    }

    private void listenCtrlF() {
        eventBus.addHandler(KeyboardShortcutEvent.TYPE,
                new KeyboardShortcutHandler() {
                    @Override
                    public void onCtrlSpace(KeyboardShortcutEvent event) {
                        // ignore
                    }

                    @Override
                    public void onCtrlF(KeyboardShortcutEvent event) {
                        if (appState.view != ViewChangeEvent.View.GRAPH) {
                            Window.alert("You can change fullscreen mode only "
                                    + "in graph view");
                            return;
                        }
                        if (!appState.fullscreen && !appState.hasPlot()) {
                            Window.alert("Nothing to plot");
                            return;
                        }
                        // toggle fullscreen
                        appState.fullscreen = !appState.fullscreen;
                        if (appState.fullscreen) {
                            RootPanel.getBodyElement().addClassName(
                                    "fullscreen");
                        } else {
                            RootPanel.getBodyElement().removeClassName(
                                    "fullscreen");
                        }
                        fireStateChange(StateChange.SCREEN);
                    }
                });
    }

    private void displayView() {
        viewContainer.clear();
        if (appState.fullscreen) {
            RootPanel.getBodyElement().addClassName("fullscreen");
        }
        for (Presenter view : viewPresenters.get(appState.view.toString())) {
            view.go(viewContainer, appState);
        }
    }

    private void fireInit() {
        History.fireCurrentHistoryState();
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        String historyToken = event.getValue();
        if (!historyToken.isEmpty()) {
            try {
                appState = new ApplicationState(historyToken);
            } catch (Exception e) {
                Window.alert("Error: " + e);
                // reset the application state to default
                appState = new ApplicationState();
                History.newItem(appState.toJSON(), false);
            }
        }
        // display
        topPresenter.go(topContainer, appState);
        errorPresenter.go(errorContainer, appState);
        displayView();
    }

    @Override
    public void go(HasWidgets container, ApplicationState appState) {
        go();
    }
}
