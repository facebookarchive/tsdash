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
package com.facebook.tsdb.tsdash.client.presenter;

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.client.model.ApplicationState;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;

public class WrapPresenter implements Presenter {

    private final HTMLPanel wrapper = new HTMLPanel("");
    private String className = "wrapper";

    private final ArrayList<Presenter> presenters = new ArrayList<Presenter>();

    public WrapPresenter() {
    }

    public WrapPresenter(String className) {
        this.className = className;
    }

    public void add(Presenter presenter) {
        presenters.add(presenter);
    }

    public ArrayList<Presenter> getPresenters() {
        return presenters;
    }

    @Override
    public void go(HasWidgets container, ApplicationState appState) {
        wrapper.clear();
        wrapper.setStyleName(className);
        for (Presenter presenter : presenters) {
            presenter.go(wrapper, appState);
        }
        container.add(wrapper);
    }

}
