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

import com.facebook.tsdb.tsdash.client.presenter.GraphPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class GraphWidget extends Composite implements
        GraphPresenter.GraphWidget {

    private static GraphWidgetUiBinder uiBinder = GWT
            .create(GraphWidgetUiBinder.class);

    @UiField
    HTMLPanel container;

    @UiField
    Label replacement;

    @UiField
    HTML json;

    interface GraphWidgetUiBinder extends UiBinder<Widget, GraphWidget> {
    }

    public GraphWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public HasWidgets container() {
        return container;
    }

    @Override
    public void setDumpJSON(String jsonDump) {
        json.setHTML(jsonDump);
    }

    @Override
    public void setReplacementVisible(boolean visible) {
        replacement.setVisible(visible);
    }
}
