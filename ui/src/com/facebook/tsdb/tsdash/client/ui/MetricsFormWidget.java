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

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.client.presenter.MetricPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

public class MetricsFormWidget extends Composite implements
        MetricPresenter.MetricsFormWidget {

    private static MetricsFormWidgetUiBinder uiBinder = GWT
            .create(MetricsFormWidgetUiBinder.class);

    interface MetricsFormWidgetUiBinder extends
            UiBinder<Widget, MetricsFormWidget> {
    }

    private final MultiWordSuggestOracle suggestOracle =
        new MultiWordSuggestOracle();

    @UiField
    Button addMetric;

    @UiField
    HTMLPanel metricsContainer;

    @UiField
    HTMLPanel container;

    @UiField
    Image loading;

    @UiField
    Anchor viewAll;

    @UiField(provided = true)
    SuggestBox suggest;

    public MetricsFormWidget() {
        suggest = new SuggestBox(suggestOracle);
        initWidget(uiBinder.createAndBindUi(this));
        suggest.getTextBox().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                suggest.getTextBox().setFocus(true);
                suggest.getTextBox().selectAll();
            }
        });
    }

    @Override
    public void focusSuggest(boolean focus) {
        suggest.getTextBox().setFocus(focus);
    }

    @Override
    public HasValue<String> typedMetric() {
        return suggest.getTextBox();
    }

    @Override
    public HasClickHandlers addMetricButton() {
        return addMetric;
    }

    @Override
    public HasWidgets metricsContainer() {
        return metricsContainer;
    }

    @Override
    public void setMetricSuggestions(ArrayList<String> options) {
        suggestOracle.clear();
        suggestOracle.addAll(options);
    }

    @Override
    public int metricsCount() {
        return metricsContainer.getWidgetCount();
    }

    @Override
    public void setLoadingVisible(boolean visible) {
        loading.setVisible(visible);
    }

    @Override
    public HasClickHandlers viewAllButton() {
        return viewAll;
    }

    @Override
    public HasText viewAllButtonText() {
        return viewAll;
    }

}
