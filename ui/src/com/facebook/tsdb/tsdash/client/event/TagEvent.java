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

public class TagEvent extends GwtEvent<TagEventHandler> {

    public static final GwtEvent.Type<TagEventHandler> TYPE =
        new GwtEvent.Type<TagEventHandler>();

    public enum Operation {
        SET, REMOVE;
    }

    private final Operation op;
    private final Metric metric;
    private final String tagName;
    private final String tagValue;

    public TagEvent(Operation op, Metric metric, String tagName,
            String tagValue) {
        this.op = op;
        this.metric = metric;
        this.tagName = tagName;
        this.tagValue = tagValue;
    }

    public Metric getMetric() {
        return metric;
    }

    public String getTagName() {
        return tagName;
    }

    public String getTagValue() {
        return tagValue;
    }

    @Override
    public GwtEvent.Type<TagEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TagEventHandler handler) {
        if (op == Operation.SET) {
            handler.onSet(this);
        } else if (op == Operation.REMOVE) {
            handler.onRemove(this);
        }
    }

}
