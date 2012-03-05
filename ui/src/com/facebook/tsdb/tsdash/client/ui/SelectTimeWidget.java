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

import java.util.Date;
import java.util.HashMap;

import com.facebook.tsdb.tsdash.client.presenter.TimePresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class SelectTimeWidget extends Composite implements
        TimePresenter.TimeWidget {

    private static SelectTimeWidgetUiBinder uiBinder = GWT
            .create(SelectTimeWidgetUiBinder.class);

    interface SelectTimeWidgetUiBinder extends
            UiBinder<Widget, SelectTimeWidget> {
    }

    interface Style extends CssResource {
        String active();

        String selected();

        String historySelected();
    }

    private final HashMap<Object, Panel> modeContainers =
        new HashMap<Object, Panel>();
    private Object selectedMode = null;
    private Object selectedHistory = null;

    @UiField
    Anchor historyButton;

    @UiField
    Anchor absoluteButton;

    @UiField
    Style style;

    @UiField
    Panel absoluteContainer;

    @UiField
    Panel absoluteFromContainer;

    @UiField
    Panel absoluteToContainer;

    @UiField
    Panel historyContainer;

    @UiField
    RadioButton last15m;

    @UiField
    RadioButton last1h;

    @UiField
    RadioButton last6h;

    @UiField
    RadioButton last1d;

    @UiField
    RadioButton last1w;

    DateBox fromDateBox = new DateBox();
    DateBox toDateBox = new DateBox();

    public SelectTimeWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        // add the Date boxes
        DateTimeFormat dateFormat = DateTimeFormat
                .getFormat("EEE, MMM d, HH:mm");
        fromDateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
        toDateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
        Date now = new Date();
        fromDateBox.setValue(new Date(now.getTime() - 15 * 60 * 1000), false);
        toDateBox.setValue(now);
        absoluteFromContainer.add(fromDateBox);
        absoluteToContainer.add(toDateBox);
        // hook the containers to the mode buttons
        modeContainers.put(historyButton, historyContainer);
        modeContainers.put(absoluteButton, absoluteContainer);
    }

    @Override
    public HasClickHandlers historyModeButton() {
        return historyButton;
    }

    @Override
    public HasClickHandlers absoluteModeButton() {
        return absoluteButton;
    }

    @Override
    public Object selectedMode() {
        return selectedMode;
    }

    @Override
    public void selectedMode(Object button) {
        if (button == selectedMode) {
            return;
        }
        if (selectedMode != null) {
            CssHelper.replaceClass((UIObject) selectedMode, style.selected(),
                    style.active());
            modeContainers.get(selectedMode).setVisible(false);
        }
        if (button != null) {
            CssHelper.replaceClass((UIObject) button, style.active(),
                    style.selected());
            modeContainers.get(button).setVisible(true);
        }
        selectedMode = button;
    }

    @Override
    public HasClickHandlers last15mButton() {
        return last15m;
    }

    @Override
    public HasClickHandlers last1hButton() {
        return last1h;
    }

    @Override
    public HasClickHandlers last6hButton() {
        return last6h;
    }

    @Override
    public HasClickHandlers last1dButton() {
        return last1d;
    }

    @Override
    public HasClickHandlers last1wButton() {
        return last1w;
    }

    @Override
    public Object selectedHistory() {
        return selectedHistory;
    }

    @Override
    public void selectedHistory(Object button) {
        if (selectedHistory == button) {
            return;
        }
        if (selectedHistory != null) {
            CssHelper.toggleClass((UIObject) selectedHistory,
                    style.historySelected());
            RadioButton radio = (RadioButton) selectedHistory;
            if (button == null) {
                radio.setValue(false);
            }
        }
        if (button != null) {
            CssHelper.toggleClass((UIObject) button, style.historySelected());
        }
        selectedHistory = button;
        if (selectedHistory != null) {
            RadioButton radio = (RadioButton) selectedHistory;
            if (!radio.getValue()) {
                radio.setValue(true);
            }
        }
    }

    @Override
    public HasValue<Date> timeFromValue() {
        return fromDateBox;
    }

    @Override
    public HasValue<Date> timeToValue() {
        return toDateBox;
    }

    @Override
    public HasValueChangeHandlers<Date> timeFrom() {
        return fromDateBox;
    }

    @Override
    public HasValueChangeHandlers<Date> timeTo() {
        return toDateBox;
    }

}
