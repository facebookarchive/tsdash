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

import com.facebook.tsdb.tsdash.client.presenter.AutoreloadPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class AutoreloadWidget extends Composite implements
        AutoreloadPresenter.AutoreloadWidget {

    private static AutoreloadWidgetUiBinder uiBinder = GWT
            .create(AutoreloadWidgetUiBinder.class);

    interface AutoreloadWidgetUiBinder extends
            UiBinder<Widget, AutoreloadWidget> {
    }

    @UiField
    HTML start;

    @UiField
    HTML stop;

    @UiField
    HTMLPanel period;

    @UiField
    HTMLPanel status;

    @UiField
    Anchor seconds;

    @UiField
    ListBox periodOption;

    @UiField
    HTMLPanel loading;

    @UiField
    Label loadingStatus;

    public AutoreloadWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public HasClickHandlers startButton() {
        return start;
    }

    @Override
    public HasClickHandlers stopButton() {
        return stop;
    }

    @Override
    public boolean isReloading() {
        return stop.isVisible();
    }

    @Override
    public void setStartVisible(boolean visible) {
        start.setVisible(visible);
    }

    @Override
    public void setStopVisible(boolean visible) {
        stop.setVisible(visible);
    }

    @Override
    public void setPeriodVisible(boolean visible) {
        period.setVisible(visible);
    }

    @Override
    public void setStatusVisible(boolean visible) {
        status.setVisible(visible);
    }

    @Override
    public void setRemainingSeconds(int seconds) {
        this.seconds.setText("" + seconds);
    }

    @Override
    public void selectPeriodOption(int index) {
        periodOption.setSelectedIndex(index);
    }

    @Override
    public int selectedPeriodOption() {
        return periodOption.getSelectedIndex();
    }

    @Override
    public void setPeriodOptions(int[] periodOptions) {
        periodOption.clear();
        for (int period : periodOptions) {
            this.periodOption.addItem("" + period);
        }
    }

    @Override
    public HasChangeHandlers period() {
        return periodOption;
    }

    @Override
    public void setLoadingVisible(boolean visible) {
        loading.setVisible(visible);
    }

    @Override
    public HasText loadingStatus() {
        return loadingStatus;
    }

}
