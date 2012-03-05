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

import com.facebook.tsdb.tsdash.client.presenter.MetricPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class MetricWidget extends Composite implements
        MetricPresenter.MetricWidget {

    private static MetricWidgetUiBinder uiBinder = GWT
            .create(MetricWidgetUiBinder.class);

    interface MetricWidgetUiBinder extends UiBinder<Widget, MetricWidget> {
    }

    public interface Style extends CssResource {
        String pressedToggleButton();

        String enabled();

        String disabled();

        String blue();
    }

    private final String name;

    @UiField
    Label nameLabel;

    @UiField
    Label delete;

    @UiField
    Label clone;

    @UiField
    HTML rightAxis;

    @UiField
    Label rate;

    @UiField
    HTMLPanel tags;

    @UiField
    CheckBox aggregatorButton;

    @UiField
    ListBox aggregatorName;

    @UiField
    Button commit;

    @UiField
    Style style;

    private static String[] aggregators = new String[] { "SUM", "AVG", "MAX",
            "MIN" };

    public MetricWidget(String name) {
        this.name = name;
        initWidget(uiBinder.createAndBindUi(this));
        nameLabel.setText(name);
        for (String agg : aggregators) {
            aggregatorName.addItem(agg, agg);
        }
    }

    @Override
    public HasClickHandlers deleteButton() {
        return delete;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public HasWidgets tagsContainer() {
        return tags;
    }

    @Override
    public HasClickHandlers rightAxisButton() {
        return rightAxis;
    }

    @Override
    public HasClickHandlers rateButton() {
        return rate;
    }

    @Override
    public boolean isPressed(Object toggleButton) {
        if (toggleButton != rightAxis && toggleButton != rate) {
            return false;
        }
        UIObject button = (UIObject) toggleButton;
        return button.getStyleName().contains(style.pressedToggleButton());
    }

    @Override
    public void pressToggleButton(Object toggleButton, boolean pressed) {
        if (toggleButton != rightAxis && toggleButton != rate) {
            return;
        }
        UIObject button = (UIObject) toggleButton;
        if (pressed
                && !button.getStyleName().contains(style.pressedToggleButton())) {
            button.addStyleName(style.pressedToggleButton());
        } else if (!pressed
                && button.getStyleName().contains(style.pressedToggleButton())) {
            button.removeStyleName(style.pressedToggleButton());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            CssHelper
                    .replaceClass(nameLabel, style.disabled(), style.enabled());
            nameLabel.setTitle("");
            aggregatorButton.setVisible(true);
            aggregatorName.setVisible(true);
        } else {
            CssHelper
                    .replaceClass(nameLabel, style.enabled(), style.disabled());
            nameLabel.setTitle("no data for this metric");
            rightAxis.setVisible(false);
            rate.setVisible(false);
            aggregatorButton.setVisible(false);
            aggregatorName.setVisible(false);
        }
    }

    @Override
    public HasClickHandlers cloneButton() {
        return clone;
    }

    @Override
    public void markPlottable(boolean plottable) {
        if (plottable) {
            CssHelper.replaceClass(nameLabel, style.blue(), style.enabled());
        } else {
            CssHelper.replaceClass(nameLabel, style.enabled(), style.blue());
            nameLabel.setTitle("metric is not plottable");
        }
    }

    @Override
    public void aggregatorEnabled(boolean enabled) {
        aggregatorName.setEnabled(enabled);
        aggregatorButton.setEnabled(enabled);
    }

    @Override
    public String selectedAggregator() {
        return aggregatorName.getItemText(aggregatorName.getSelectedIndex());
    }

    @Override
    public HasChangeHandlers aggregator() {
        return aggregatorName;
    }

    @Override
    public HasValue<Boolean> aggregatorSwitch() {
        return aggregatorButton;
    }

    @Override
    public void selectedAggregator(String aggregator) {
        for (int i = 0; i < aggregators.length; i++) {
            if (aggregators[i].equals(aggregator)) {
                aggregatorName.setSelectedIndex(i);
            }
        }
    }

    @Override
    public HasClickHandlers commitButton() {
        return commit;
    }

    @Override
    public void commitEnabled(boolean enabled) {
        commit.setEnabled(enabled);
    }

    @Override
    public void commitVisible(boolean visible) {
        commit.setVisible(visible);
    }
}
