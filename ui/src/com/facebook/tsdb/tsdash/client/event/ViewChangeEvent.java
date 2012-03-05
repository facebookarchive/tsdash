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
package com.facebook.tsdb.tsdash.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ViewChangeEvent extends GwtEvent<ViewChangeEventHandler> {

    public static final GwtEvent.Type<ViewChangeEventHandler> TYPE =
        new GwtEvent.Type<ViewChangeEventHandler>();

    public enum View {
        GRAPH, LOG;
    }

    private final View view;

    public ViewChangeEvent(View view) {
        this.view = view;
    }

    public ViewChangeEvent(String name) {
        this.view = View.valueOf(name);
    }

    public View getView() {
        return view;
    }

    public String getViewToken() {
        return view.toString().toLowerCase();
    }

    @Override
    public GwtEvent.Type<ViewChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ViewChangeEventHandler handler) {
        handler.onChange(this);
    }

}
