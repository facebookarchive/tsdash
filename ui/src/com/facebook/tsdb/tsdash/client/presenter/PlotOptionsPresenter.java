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

import com.facebook.tsdb.tsdash.client.event.PlotOptionsEvent;
import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;

public class PlotOptionsPresenter implements Presenter {

    public interface Widget {
        HasClickHandlers interactiveMode();

        HasClickHandlers imageMode();

        void selectedMode(Object button);

        Object selectedMode();

        void imageOptionsVisible(boolean visible);

        HasClickHandlers lineChartButton();

        HasClickHandlers surfaceButton();

        HasValue<Boolean> lineChart();

        HasValue<Boolean> surface();

        void setImageTypeSelected(Object selected);

        void setSurfaceOptionsVisible(boolean visible);

        HasClickHandlers colorButton();

        boolean colorPaletteSelected();

        void colorPaletteSelected(boolean selected);
    }

    private final HandlerManager eventBus;
    private final Widget widget;

    public PlotOptionsPresenter(HandlerManager eventBus, Widget widget) {
        this.eventBus = eventBus;
        this.widget = widget;
        bindWidget();
    }

    private void bindWidget() {
        // MODE
        ClickHandler modeHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (event.getSource() == widget.selectedMode()) {
                    return;
                }
                widget.selectedMode(event.getSource());
                boolean interactive = widget.selectedMode() == widget
                        .interactiveMode();
                widget.imageOptionsVisible(!interactive);
                eventBus.fireEvent(new PlotOptionsEvent(interactive, widget
                        .surface().getValue(), widget.colorPaletteSelected()));
            }
        };
        widget.imageMode().addClickHandler(modeHandler);
        widget.interactiveMode().addClickHandler(modeHandler);
        // IMAGE TYPE
        ClickHandler imageTypeHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean surface = widget.surface().getValue();
                widget.setSurfaceOptionsVisible(surface);
                eventBus.fireEvent(new PlotOptionsEvent(
                        widget.selectedMode() == widget.interactiveMode(),
                        widget.surface().getValue(), widget
                                .colorPaletteSelected()));
            }
        };
        widget.lineChartButton().addClickHandler(imageTypeHandler);
        widget.surfaceButton().addClickHandler(imageTypeHandler);
        widget.colorButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new PlotOptionsEvent(
                        widget.selectedMode() == widget.interactiveMode(),
                        widget.surface().getValue(), widget
                                .colorPaletteSelected()));
            }
        });
    }

    @Override
    public void go(HasWidgets container, ApplicationState appState) {
        container.add((com.google.gwt.user.client.ui.Widget) widget);
        if (appState.interactive) {
            widget.selectedMode(widget.interactiveMode());
            widget.imageOptionsVisible(false);
        } else {
            widget.selectedMode(widget.imageMode());
            if (appState.surface) {
                widget.setImageTypeSelected(widget.surfaceButton());
            } else {
                widget.setImageTypeSelected(widget.lineChartButton());
            }
        }
    }
}
