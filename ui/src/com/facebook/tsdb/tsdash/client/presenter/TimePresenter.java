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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import com.facebook.tsdb.tsdash.client.event.AutoReloadEvent;
import com.facebook.tsdb.tsdash.client.event.AutoReloadEventHandler;
import com.facebook.tsdb.tsdash.client.event.TimeRangeChangeEvent;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.facebook.tsdb.tsdash.client.model.TimeRange;
import com.facebook.tsdb.tsdash.client.model.ApplicationState.TimeMode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;

public class TimePresenter implements Presenter {

    public interface TimeWidget {
        HasClickHandlers historyModeButton();

        HasClickHandlers absoluteModeButton();

        Object selectedMode();

        void selectedMode(Object button);

        HasClickHandlers last15mButton();

        HasClickHandlers last1hButton();

        HasClickHandlers last6hButton();

        HasClickHandlers last1dButton();

        HasClickHandlers last1wButton();

        Object selectedHistory();

        void selectedHistory(Object button);

        HasValue<Date> timeFromValue();

        HasValue<Date> timeToValue();

        HasValueChangeHandlers<Date> timeFrom();

        HasValueChangeHandlers<Date> timeTo();
    }

    private final HandlerManager eventBus;
    private final TimeWidget widget;

    // values for the history buttons, in seconds
    private final HashMap<Object, Long> historyButton2Range =
        new HashMap<Object, Long>();
    private final TreeMap<Long, Object> historyRange2Button =
        new TreeMap<Long, Object>();

    public TimePresenter(HandlerManager eventBus, TimeWidget widget) {
        this.eventBus = eventBus;
        this.widget = widget;
        bindWidget();
        bindAutoReload();

        historyButton2Range.put(widget.last15mButton(), (long) 15 * 60);
        historyButton2Range.put(widget.last1hButton(), (long) 60 * 60);
        historyButton2Range.put(widget.last6hButton(), (long) 6 * 60 * 60);
        historyButton2Range.put(widget.last1dButton(), (long) 24 * 60 * 60);
        historyButton2Range.put(widget.last1wButton(), (long) 7 * 24 * 60 * 60);
        // create the inverse mapping by using a tree-map in order to have the
        // ranges sorted
        for (Object button : historyButton2Range.keySet()) {
            historyRange2Button.put(historyButton2Range.get(button), button);
        }
    }

    private void bindAutoReload() {
        eventBus.addHandler(AutoReloadEvent.TYPE, new AutoReloadEventHandler() {
            @Override
            public void onPeriodChange(AutoReloadEvent event) {
                // not interesting here
            }

            @Override
            public void onEnable(AutoReloadEvent event) {
                // this is not interesting either
            }

            @Override
            public void onLaunch(AutoReloadEvent event) {
                TimeRange current = new TimeRange(event.getPeriod());
                // update only the absolute date values
                widget.timeFromValue().setValue(new Date(current.from), false);
                widget.timeToValue().setValue(new Date(current.to), false);
            }
        });
    }

    private void bindWidget() {
        ClickHandler modeHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // ignore the selected click
                if (widget.selectedMode().equals(event.getSource())) {
                    return;
                }
                widget.selectedMode(event.getSource());
            }
        };
        widget.historyModeButton().addClickHandler(modeHandler);
        widget.absoluteModeButton().addClickHandler(modeHandler);
        // bind the history mode buttons
        ClickHandler historyOptionsHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (event.getSource().equals(widget.selectedHistory())) {
                    return;
                }
                widget.selectedHistory(event.getSource());
                // fire time range change event
                TimeRange timeRange = new TimeRange(
                        historyButton2Range.get(event.getSource()));
                // sync the absolute time elements
                widget.timeFromValue()
                        .setValue(new Date(timeRange.from), false);
                widget.timeToValue().setValue(new Date(timeRange.to), false);
                // fire the event to the application controller
                eventBus.fireEvent(new TimeRangeChangeEvent(TimeMode.HISTORY,
                        timeRange));
            }
        };
        widget.last15mButton().addClickHandler(historyOptionsHandler);
        widget.last1hButton().addClickHandler(historyOptionsHandler);
        widget.last6hButton().addClickHandler(historyOptionsHandler);
        widget.last1dButton().addClickHandler(historyOptionsHandler);
        widget.last1wButton().addClickHandler(historyOptionsHandler);
        // bind the absolute mode date pickers
        widget.timeFrom().addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                Date to = widget.timeToValue().getValue();
                if (event.getValue().after(to)) {
                    Window.alert("Incorrect 'from' date");
                    widget.timeFromValue().setValue(
                            new Date(to.getTime() - 15 * 60 * 1000), false);
                    return;
                }
                TimeRange range = new TimeRange(event.getValue().getTime(), to
                        .getTime());
                eventBus.fireEvent(new TimeRangeChangeEvent(TimeMode.ABSOLUTE,
                        range));
                widget.selectedHistory(null);
            }
        });
        widget.timeTo().addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                Date from = widget.timeFromValue().getValue();
                if (from.after(event.getValue())) {
                    Window.alert("Incorrect 'to' date");
                    widget.timeToValue().setValue(new Date(), false);
                    return;
                }
                TimeRange range = new TimeRange(from.getTime(), event
                        .getValue().getTime());
                eventBus.fireEvent(new TimeRangeChangeEvent(TimeMode.ABSOLUTE,
                        range));
                widget.selectedHistory(null);
            }
        });
    }

    private void selectHistoryRange(TimeRange timeRange) {
        boolean selected = false;
        // assuming the ranges are sorted (we use a tree-map)
        long range = 0;
        for (Iterator<Long> it = historyRange2Button.keySet().iterator(); it
                .hasNext();) {
            range = it.next();
            if (timeRange.getSeconds() <= range) {
                widget.selectedHistory(historyRange2Button.get(range));
                selected = true;
                break;
            }
        }
        if (!selected) {
            // we get the last value of range
            widget.selectedHistory(historyRange2Button.get(range));
        }
    }

    private void initTimeWidget(ApplicationState appState) {
        selectHistoryRange(appState.timeRange);
        widget.timeToValue().setValue(new Date(appState.timeRange.to), false);
        widget.timeFromValue().setValue(new Date(appState.timeRange.from),
                false);
        if (appState.timeMode.equals(TimeMode.HISTORY)) {
            widget.selectedMode(widget.historyModeButton());
        } else if (appState.timeMode.equals(TimeMode.ABSOLUTE)) {
            widget.selectedMode(widget.absoluteModeButton());
        }
    }

    @Override
    public void go(HasWidgets container, ApplicationState appState) {
        container.add((com.google.gwt.user.client.ui.Widget) widget);
        initTimeWidget(appState);
    }

}
