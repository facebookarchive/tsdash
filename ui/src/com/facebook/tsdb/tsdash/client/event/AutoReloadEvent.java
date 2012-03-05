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

public class AutoReloadEvent extends GwtEvent<AutoReloadEventHandler> {

    public static final GwtEvent.Type<AutoReloadEventHandler> TYPE =
        new GwtEvent.Type<AutoReloadEventHandler>();

    public enum Action {
        ENABLE, PERIOD_CHANGE, LAUNCH;
    }

    private final Action action;
    private final boolean autoReload;
    private final int period;

    public AutoReloadEvent(Action action, boolean autoReload, int period) {
        this.action = action;
        this.autoReload = autoReload;
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    public boolean isAutoReloading() {
        return autoReload;
    }

    @Override
    public GwtEvent.Type<AutoReloadEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AutoReloadEventHandler handler) {
        if (action == Action.PERIOD_CHANGE) {
            handler.onPeriodChange(this);
        } else if (action == Action.LAUNCH) {
            handler.onLaunch(this);
        } else if (action == Action.ENABLE) {
            handler.onEnable(this);
        }
    }
}
