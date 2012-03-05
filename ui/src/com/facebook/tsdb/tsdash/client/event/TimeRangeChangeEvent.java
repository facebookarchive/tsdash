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

import com.facebook.tsdb.tsdash.client.model.TimeRange;
import com.facebook.tsdb.tsdash.client.model.ApplicationState.TimeMode;
import com.google.gwt.event.shared.GwtEvent;

public class TimeRangeChangeEvent extends GwtEvent<TimeRangeChangeEventHandler> {

    public static final GwtEvent.Type<TimeRangeChangeEventHandler> TYPE =
        new GwtEvent.Type<TimeRangeChangeEventHandler>();

    private final TimeMode mode;
    private final TimeRange timeRange;

    public TimeRangeChangeEvent(TimeMode mode, TimeRange timeRange) {
        this.mode = mode;
        this.timeRange = timeRange;
    }

    public TimeMode getMode() {
        return mode;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    @Override
    public GwtEvent.Type<TimeRangeChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TimeRangeChangeEventHandler handler) {
        handler.onChange(this);
    }
}
