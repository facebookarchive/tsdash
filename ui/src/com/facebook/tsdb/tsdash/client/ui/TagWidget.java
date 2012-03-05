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
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TagWidget extends Composite implements MetricPresenter.TagWidget {

    private static TagWidgetUiBinder uiBinder = GWT
            .create(TagWidgetUiBinder.class);

    interface TagWidgetUiBinder extends UiBinder<Widget, TagWidget> {
    }

    @UiField
    Panel container;

    @UiField
    Label name;

    @UiField
    Label value;

    @UiField
    Label removeValue;

    @UiField
    ListBox options;

    @UiField
    Anchor apply;

    public TagWidget(String tagName, String tagValue) {
        initWidget(uiBinder.createAndBindUi(this));
        name.setText(tagName);
        value.setText("=" + tagValue);
        // add the only option even if we hide the list box
        // this way we can still access getSelectedValue()
        options.addItem(tagValue, tagValue);
        optionsVisible(false);
    }

    public TagWidget(String tagName, ArrayList<String> options) {
        initWidget(uiBinder.createAndBindUi(this));
        name.setText(tagName);
        removeValue.setVisible(false);
        if (options.size() == 0) {
            optionsVisible(false);
            value.setText("NULL");
        } else if (options.size() == 1) {
            String tagValue = options.get(0);
            value.setText("=" + tagValue);
            optionsVisible(false);
        } else {
            // multiple options
            for (String tagValue : options) {
                this.options.addItem(tagValue, tagValue);
            }
            value.setText("(" + options.size() + ")");
            value.setTitle(options.size() + " values for this tag");
        }
    }

    @Override
    public HasClickHandlers deleteButton() {
        return null;
    }

    @Override
    public HasClickHandlers setValueButton() {
        return apply;
    }

    @Override
    public HasClickHandlers removeValueButton() {
        return removeValue;
    }

    @Override
    public String getSelectedValue() {
        return options.getItemText(options.getSelectedIndex());
    }

    @Override
    public void optionsVisible(boolean visible) {
        options.setVisible(visible);
        apply.setVisible(visible);
    }

    @Override
    public void setValue(String value) {
        optionsVisible(false);
        this.value.setText("=" + value);
        this.value.setTitle("");
    }

}
