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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.facebook.tsdb.tsdash.client.presenter.LogPresenter;

public class LogWidget extends Composite implements LogPresenter.LogWidget {

    private static LogWidgetUiBinder uiBinder = GWT
            .create(LogWidgetUiBinder.class);

    interface LogWidgetUiBinder extends UiBinder<Widget, LogWidget> {
    }

    public LogWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiField
    HTMLPanel container;

    @Override
    public HasWidgets container() {
        return container;
    }
}
