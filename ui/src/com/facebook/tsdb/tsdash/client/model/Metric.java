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
package com.facebook.tsdb.tsdash.client.model;

import java.util.HashMap;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class Metric {

    public String name;
    public boolean rightAxis = false;
    public boolean rate = false;
    public String aggregator = null;
    public HashMap<String, String> tags = new HashMap<String, String>();
    public boolean plottable = true;
    public MetricHeader header;

    public Metric(String name) {
        this.name = name;
        header = new MetricHeader(name);
    }

    public Metric(String name, MetricHeader header) {
        this.name = name;
        this.header = header;
    }

    public Metric(JSONObject obj) throws Exception {
        fromJSON(obj);
        header = new MetricHeader(name);
        plottable = true;
    }

    public boolean isPlottable() {
        return plottable;
    }

    public boolean isAggregated() {
        return aggregator != null;
    }

    public boolean allowsAggregation() {
        if (aggregator != null) {
            return true;
        }
        for (String tag : header.tagsSet.keySet()) {
            if (header.tagsSet.get(tag).size() > 1) {
                return true;
            }
        }
        return false;
    }

    public Metric dup() {
        Metric newMetric = new Metric(name, header);
        newMetric.rightAxis = rightAxis;
        newMetric.rate = rate;
        newMetric.aggregator = aggregator;
        for (String tag : tags.keySet()) {
            newMetric.tags.put(tag, tags.get(tag));
        }
        newMetric.plottable = false;
        return newMetric;
    }

    public String getSignature() {
        String sig = name;
        for (String tag : tags.keySet()) {
            sig += tag + "=" + tags.get(tag);
        }
        return sig;
    }

    public JSONObject encodeTags() {
        JSONObject tagsMap = new JSONObject();
        for (String key : tags.keySet()) {
            String tagValue = tags.get(key);
            if (tagValue == null) {
                tagsMap.put(key, JSONNull.getInstance());
            } else {
                tagsMap.put(key, new JSONString(tagValue));
            }
        }
        return tagsMap;
    }

    public JSONObject toJSONParam() {
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONString(name));
        obj.put("rate", JSONBoolean.getInstance(rate));
        obj.put("tags", encodeTags());
        obj.put("orders", new JSONArray());
        if (aggregator == null) {
            obj.put("aggregator", JSONNull.getInstance());
        } else {
            obj.put("aggregator", new JSONString(aggregator));
        }
        obj.put("dissolveTags", new JSONArray());
        return obj;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONString(name));
        obj.put("ry", JSONBoolean.getInstance(rightAxis));
        obj.put("rate", JSONBoolean.getInstance(rate));
        obj.put("tags", encodeTags());
        if (aggregator == null) {
            obj.put("aggregator", JSONNull.getInstance());
        } else {
            obj.put("aggregator", new JSONString(aggregator));
        }
        return obj;
    }

    public void fromJSON(JSONObject obj) throws Exception {
        name = obj.get("name").isString().stringValue();
        rightAxis = obj.get("ry").isBoolean().booleanValue();
        rate = obj.get("rate").isBoolean().booleanValue();
        if (obj.get("aggregator").isString() != null) {
            aggregator = obj.get("aggregator").isString().stringValue();
        } else {
            aggregator = null;
        }
        JSONObject tagsMap = obj.get("tags").isObject();
        for (String key : tagsMap.keySet()) {
            tags.put(key, tagsMap.get(key).isString().stringValue());
        }
    }
}
