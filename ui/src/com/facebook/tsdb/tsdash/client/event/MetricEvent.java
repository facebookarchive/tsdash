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

import com.facebook.tsdb.tsdash.client.model.Metric;
import com.google.gwt.event.shared.GwtEvent;

public class MetricEvent extends GwtEvent<MetricEventHandler> {

    public static final GwtEvent.Type<MetricEventHandler> TYPE =
        new GwtEvent.Type<MetricEventHandler>();

    public enum Operation {
        ADD, DELETE, AGGREGATE, PARAM_TOGGLE;
    }

    private final Operation op;
    private final Metric metric;

    public MetricEvent(Operation op, Metric metric) {
        this.op = op;
        this.metric = metric;
    }

    public Metric getMetric() {
        return metric;
    }

    @Override
    public GwtEvent.Type<MetricEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MetricEventHandler handler) {
        if (op == Operation.ADD) {
            handler.onAdd(this);
        } else if (op == Operation.DELETE) {
            handler.onDelete(this);
        } else if (op == Operation.PARAM_TOGGLE) {
            handler.onToggle(this);
        } else if (op == Operation.AGGREGATE) {
            handler.onAggregatorChange(this);
        }
    }
}
