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

import com.facebook.tsdb.tsdash.client.event.AutoReloadEvent;
import com.facebook.tsdb.tsdash.client.event.GraphEvent;
import com.facebook.tsdb.tsdash.client.event.GraphEventHandler;
import com.facebook.tsdb.tsdash.client.event.KeyboardShortcutEvent;
import com.facebook.tsdb.tsdash.client.event.KeyboardShortcutHandler;
import com.facebook.tsdb.tsdash.client.event.StateChangeEvent;
import com.facebook.tsdb.tsdash.client.event.StateChangeHandler;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class AutoreloadPresenter implements Presenter {

    private int remaining;
    private Scheduler.RepeatingCommand activeCmd = null;

    private static int[] periodOptions = { 3, 5, 15 };

    public interface AutoreloadWidget {
        HasClickHandlers startButton();

        HasClickHandlers stopButton();

        void setStartVisible(boolean visible);

        void setStopVisible(boolean visible);

        boolean isReloading();

        void setPeriodVisible(boolean visible);

        void setStatusVisible(boolean visible);

        void setRemainingSeconds(int seconds);

        void selectPeriodOption(int index);

        int selectedPeriodOption();

        void setPeriodOptions(int[] periodOptions);

        HasChangeHandlers period();

        void setLoadingVisible(boolean visible);

        HasText loadingStatus();
    }

    private final HandlerManager eventBus;
    private final AutoreloadWidget widget;

    public AutoreloadPresenter(HandlerManager eventBus,
            AutoreloadWidget widget) {
        this.eventBus = eventBus;
        this.widget = widget;
        bindWidget();
        widget.setPeriodOptions(periodOptions);
        listenCtrlSpaceShortcut();
        listenGraphEvents();
        listenStateChange();
    }

    private void bindWidget() {
        ClickHandler toggleHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                enable(!widget.isReloading());
                eventBus.fireEvent(new AutoReloadEvent(
                        AutoReloadEvent.Action.ENABLE, widget.isReloading(),
                        getPeriodOption()));
            }
        };
        widget.startButton().addClickHandler(toggleHandler);
        widget.stopButton().addClickHandler(toggleHandler);
        widget.period().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                // fire period change
                eventBus.fireEvent(new AutoReloadEvent(
                        AutoReloadEvent.Action.PERIOD_CHANGE, widget
                                .isReloading(), getPeriodOption()));
                fireTimeCount();
            }
        });
    }

    private void listenStateChange() {
        eventBus.addHandler(StateChangeEvent.TYPE, new StateChangeHandler() {
            @Override
            public void onViewChange(StateChangeEvent event) {
            }

            @Override
            public void onMetricChange(StateChangeEvent event) {
                setByState(event.getAppState());
            }

            @Override
            public void onTimeChange(StateChangeEvent event) {
                setByState(event.getAppState());
            }

            @Override
            public void onPlotParamsChange(StateChangeEvent event) {
            }

            @Override
            public void onAutoReloadChange(StateChangeEvent event) {
            }

            @Override
            public void onScreenChange(StateChangeEvent event) {
            }
        });
    }

    private void listenCtrlSpaceShortcut() {
        eventBus.addHandler(KeyboardShortcutEvent.TYPE,
                new KeyboardShortcutHandler() {
                    @Override
                    public void onCtrlSpace(KeyboardShortcutEvent event) {
                        enable(!widget.isReloading());
                        eventBus.fireEvent(new AutoReloadEvent(
                                AutoReloadEvent.Action.ENABLE, widget
                                        .isReloading(), getPeriodOption()));
                    }

                    @Override
                    public void onCtrlF(KeyboardShortcutEvent event) {
                        // ignore
                    }
                });
    }

    private void triggerReload() {
        if (!widget.isReloading()) {
            return;
        }
        widget.setStatusVisible(false);
        eventBus.fireEvent(new AutoReloadEvent(AutoReloadEvent.Action.LAUNCH,
                widget.isReloading(), getPeriodOption()));
    }

    private void listenGraphEvents() {
        eventBus.addHandler(GraphEvent.TYPE, new GraphEventHandler() {
            @Override
            public void onLoaded(GraphEvent event) {
                widget.setLoadingVisible(false);
                if (widget.isReloading()) {
                    widget.setStatusVisible(true);
                    fireTimeCount();
                }
            }

            @Override
            public void onLoadingData(GraphEvent event) {
                widget.setLoadingVisible(true);
                widget.loadingStatus().setText("Loading data...");
            }

            @Override
            public void onStartRendering(GraphEvent event) {
                widget.setLoadingVisible(true);
                widget.loadingStatus().setText("Rendering chart...");
            }
        });
    }

    private int getPeriodOption() {
        int index = widget.selectedPeriodOption();
        return periodOptions[index];
    }

    private void selectPeriodOption(int periodValue) {
        for (int i = 0; i < periodOptions.length; i++) {
            if (periodOptions[i] == periodValue) {
                widget.selectPeriodOption(i);
                break;
            }
        }
    }

    private void fireTimeCount() {
        remaining = getPeriodOption();
        widget.setRemainingSeconds(remaining);
        activeCmd = new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                if (activeCmd != this) {
                    return false;
                }
                remaining--;
                if (remaining == 0) {
                    // trigger time update
                    triggerReload();
                    return false;
                }
                widget.setRemainingSeconds(remaining);
                return true;
            }
        };
        Scheduler.get().scheduleFixedDelay(activeCmd, 1000);
    }

    private void enable(boolean autoreload) {
        widget.setLoadingVisible(false);
        widget.setStartVisible(!autoreload);
        widget.setStopVisible(autoreload);
        widget.setStatusVisible(autoreload);
        widget.setPeriodVisible(autoreload);
        if (autoreload) {
            fireTimeCount();
        }
    }

    private void disable() {
        widget.setLoadingVisible(false);
        widget.setStartVisible(false);
        widget.setStopVisible(false);
        widget.setStatusVisible(false);
        widget.setPeriodVisible(false);
    }

    private void setByState(ApplicationState appState) {
        if (appState.needsAutoreload()) {
            enable(appState.autoReload);
        } else {
            disable();
        }
    }

    @Override
    public void go(HasWidgets container, ApplicationState appState) {
        container.add((Widget) widget);
        selectPeriodOption(appState.reloadPeriod);
        setByState(appState);
    }
}
