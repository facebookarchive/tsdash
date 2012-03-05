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

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.client.InvalidApplicationStateException;
import com.facebook.tsdb.tsdash.client.event.ViewChangeEvent.View;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONArray;

public class ApplicationState {

    public enum TimeMode {
        HISTORY, ABSOLUTE;
    }

    public View view = View.GRAPH;
    public TimeMode timeMode = TimeMode.HISTORY;
    public TimeRange timeRange = new TimeRange(15 * 60);
    public boolean autoReload = false;
    public boolean fullscreen = false;
    public int reloadPeriod = 5;
    public boolean interactive = false;
    public boolean surface = false;
    public boolean palette = false;
    public ArrayList<Metric> metrics = new ArrayList<Metric>();

    public ApplicationState() {
    }

    public ApplicationState(String json) throws Exception {
        fromJSON(json);
    }

    public TimeRange getAndUpdateTimeRange() {
        if (timeMode == TimeMode.HISTORY) {
            timeRange = new TimeRange(timeRange.getSeconds());
        }
        return timeRange;
    }

    public boolean needsAutoreload() {
        return metrics.size() > 0 && timeRange.getSeconds() <= TimeRange._1_H;
    }

    public String toJSON() {
        JSONObject topObj = new JSONObject();
        topObj.put("view", new JSONString(view.toString()));
        topObj.put("timeMode", new JSONString(timeMode.toString()));
        topObj.put("tsFrom", new JSONNumber(timeRange.from));
        topObj.put("tsTo", new JSONNumber(timeRange.to));
        topObj.put("autoReload", JSONBoolean.getInstance(autoReload));
        topObj.put("fullscreen", JSONBoolean.getInstance(fullscreen));
        topObj.put("reloadPeriod", new JSONNumber(reloadPeriod));
        topObj.put("interactive", JSONBoolean.getInstance(interactive));
        topObj.put("surface", JSONBoolean.getInstance(surface));
        topObj.put("palette", JSONBoolean.getInstance(palette));
        JSONArray metricsArray = new JSONArray();
        for (int i = 0; i < metrics.size(); i++) {
            metricsArray.set(i, metrics.get(i).toJSON());
        }
        topObj.put("m", metricsArray);
        return topObj.toString();
    }

    private void fromJSON(String json) throws Exception {
        // fill in all the fields from the JSON string
        JSONObject topObj = JSONParser.parseStrict(json).isObject();
        // view
        String viewStr = topObj.get("view").isString().stringValue();
        try {
            view = View.valueOf(viewStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidApplicationStateException("unknown view '"
                    + viewStr + "'");
        }
        // time related
        String timeModeStr = topObj.get("timeMode").isString().stringValue();
        try {
            timeMode = TimeMode.valueOf(timeModeStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidApplicationStateException("unknown time mode '"
                    + timeModeStr + "'");
        }
        long tsFrom = (long) topObj.get("tsFrom").isNumber().doubleValue();
        long tsTo = (long) topObj.get("tsTo").isNumber().doubleValue();
        if (tsFrom >= tsTo) {
            throw new InvalidApplicationStateException("invalid time range");
        }
        if (timeMode.equals(TimeMode.HISTORY)) {
            timeRange = new TimeRange((tsTo - tsFrom) / 1000);
        } else {
            timeRange = new TimeRange(tsFrom, tsTo);
        }
        // reload params
        autoReload = topObj.get("autoReload").isBoolean().booleanValue();
        fullscreen = topObj.get("fullscreen").isBoolean().booleanValue();
        reloadPeriod = (int) topObj.get("reloadPeriod").isNumber()
                .doubleValue();
        interactive = topObj.get("interactive").isBoolean().booleanValue();
        surface = topObj.get("surface").isBoolean().booleanValue();
        palette = topObj.get("palette").isBoolean().booleanValue();
        // metrics
        JSONArray metricsArray = topObj.get("m").isArray();
        for (int i = 0; i < metricsArray.size(); i++) {
            metrics.add(new Metric(metricsArray.get(i).isObject()));
        }
    }

    public boolean hasPlot() {
        for (Metric metric : metrics) {
            if (metric.isPlottable()) {
                return true;
            }
        }
        return false;
    }
}
