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
package com.facebook.tsdb.tsdash.client.ui;

import com.facebook.tsdb.tsdash.client.presenter.PlotOptionsPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class PlotOptionsWidget extends Composite implements
        PlotOptionsPresenter.Widget {

    private static PlotOptionsWidgetUiBinder uiBinder = GWT
            .create(PlotOptionsWidgetUiBinder.class);

    interface PlotOptionsWidgetUiBinder extends
            UiBinder<Widget, PlotOptionsWidget> {
    }

    interface Style extends CssResource {
        String selected();

        String active();
    }

    public PlotOptionsWidget() {
        color = new ToggleButton(new Image("img/color.png"));
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiField
    Anchor interactive;

    @UiField
    Anchor image;

    @UiField
    RadioButton lineChart;

    @UiField
    RadioButton surface;

    @UiField
    HTMLPanel imageOptions;

    @UiField
    HTMLPanel surfaceOptions;

    @UiField(provided = true)
    ToggleButton color;

    @UiField
    Style style;

    private Object selectedMode = null;

    @Override
    public HasClickHandlers interactiveMode() {
        return interactive;
    }

    @Override
    public HasClickHandlers imageMode() {
        return image;
    }

    @Override
    public void selectedMode(Object button) {
        if (selectedMode == button) {
            return;
        }
        if (selectedMode != null) {
            CssHelper.replaceClass((UIObject) selectedMode, style.selected(),
                    style.active());
        }
        if (button != null) {
            CssHelper.replaceClass((UIObject) button, style.active(),
                    style.selected());
        }
        selectedMode = button;
    }

    @Override
    public Object selectedMode() {
        return selectedMode;
    }

    @Override
    public void imageOptionsVisible(boolean visible) {
        imageOptions.setVisible(visible);
    }

    @Override
    public HasClickHandlers lineChartButton() {
        return lineChart;
    }

    @Override
    public HasClickHandlers surfaceButton() {
        return surface;
    }

    @Override
    public void setImageTypeSelected(Object selected) {
        if (selected == lineChart) {
            lineChart.setValue(true);
            surfaceOptions.setVisible(false);
        } else {
            surface.setValue(true);
            surfaceOptions.setVisible(true);
        }
    }

    @Override
    public HasClickHandlers colorButton() {
        return color;
    }

    @Override
    public HasValue<Boolean> lineChart() {
        return lineChart;
    }

    @Override
    public HasValue<Boolean> surface() {
        return surface;
    }

    @Override
    public void setSurfaceOptionsVisible(boolean visible) {
        surfaceOptions.setVisible(visible);
    }

    @Override
    public boolean colorPaletteSelected() {
        return color.isDown();
    }

    @Override
    public void colorPaletteSelected(boolean selected) {
        color.setDown(selected);
    }

}
