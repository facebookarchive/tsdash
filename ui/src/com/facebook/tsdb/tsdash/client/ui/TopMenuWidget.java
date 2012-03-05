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

import com.facebook.tsdb.tsdash.client.presenter.TopMenuPresenter;
import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class TopMenuWidget extends Composite
implements TopMenuPresenter.Widget {

    private static TopMenuWidgetUiBinder uiBinder = GWT
            .create(TopMenuWidgetUiBinder.class);

    interface TopMenuWidgetUiBinder extends UiBinder<Widget, TopMenuWidget> {
    }

    interface TopMenuStyle extends CssResource {
        String clicked();

        String link();
    }

    public TopMenuWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    private Object selected = null;

    @UiField
    Anchor graph;

    @UiField
    Anchor log;

    @UiField
    TopMenuStyle style;

    @Override
    public HasClickHandlers getGraphButton() {
        return graph;
    }

    @Override
    public HasClickHandlers getLogButton() {
        return log;
    }

    @Override
    public Object getSelected() {
        return selected;
    }

    @Override
    public void setSelected(Object button) {
        if (selected == button) {
            return;
        }
        if (selected != null) {
            CssHelper.replaceClass((UIObject) selected, style.clicked(),
                    style.link());
        }
        selected = button;
        CssHelper
                .replaceClass((UIObject) button, style.link(), style.clicked());
    }

}
