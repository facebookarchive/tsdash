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

import com.facebook.tsdb.tsdash.client.presenter.ErrorPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class ErrorWidget extends Composite implements
        ErrorPresenter.ErrorWidget {

    private static ErrorWidgetUiBinder uiBinder = GWT
            .create(ErrorWidgetUiBinder.class);

    interface ErrorWidgetUiBinder extends UiBinder<Widget, ErrorWidget> {
    }

    @UiField
    Label title;

    HTML details = new HTML("");

    @UiField
    Anchor detailsButton;

    PopupPanel popup = new PopupPanel(true, true);

    public ErrorWidget() {
        setupWidget();
        initWidget(uiBinder.createAndBindUi(this));
    }

    private void setupWidget() {
        popup.setWidget(details);
        popup.setGlassEnabled(true);
        details.addStyleName("errorDetails");
        popup.addStyleName("errorDetailsPopup");
    }

    @Override
    public HasText title() {
        return title;
    }

    @Override
    public void showDetails() {
        popup.setVisible(true);
        popup.center();
        popup.show();
    }

    @Override
    public HasHTML details() {
        return details;
    }

    @Override
    public HasClickHandlers detailsButton() {
        return detailsButton;
    }

}
