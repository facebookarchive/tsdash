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

public class GraphEvent extends GwtEvent<GraphEventHandler> {

    public static final GwtEvent.Type<GraphEventHandler> TYPE =
        new GwtEvent.Type<GraphEventHandler>();

    public enum Action {
        LOADING_DATA, START_RENDERING, LOADED;
    }

    private final Action action;

    public GraphEvent(Action action) {
        this.action = action;
    }

    @Override
    public GwtEvent.Type<GraphEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(GraphEventHandler handler) {
        if (action == Action.LOADING_DATA) {
            handler.onLoadingData(this);
        } else if (action == Action.START_RENDERING) {
            handler.onStartRendering(this);
        } else if (action == Action.LOADED) {
            handler.onLoaded(this);
        }
    }

}
