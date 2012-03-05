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
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import com.facebook.tsdb.tsdash.client.presenter.MetricPresenter;

public class MetricOptionWidget extends Composite implements
        MetricPresenter.MetricOptionWidget {

    private static MetricOptionWidgetUiBinder uiBinder = GWT
            .create(MetricOptionWidgetUiBinder.class);

    interface MetricOptionWidgetUiBinder extends
            UiBinder<Widget, MetricOptionWidget> {
    }

    @UiField
    Anchor link;

    public MetricOptionWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public HasText link() {
        return link;
    }

    @Override
    public HasClickHandlers linkButton() {
        return link;
    }

}
