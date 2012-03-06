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
package com.facebook.tsdb.tsdash.server.data;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.facebook.tsdb.tsdash.server.model.DataPoint;
import com.facebook.tsdb.tsdash.server.model.Metric;
import com.facebook.tsdb.tsdash.server.model.TagsArray;

public class DataTable {

    private final Metric[] metrics;

    public DataTable(Metric[] metrics) {
        this.metrics = metrics;
    }

    @SuppressWarnings("unchecked")
    private JSONObject newColumn(String label, String type) {
        JSONObject col = new JSONObject();
        col.put("label", label);
        col.put("type", type);
        return col;
    }

    private String renderLineTitle(Metric metric, TagsArray tags) {
        String suffix = metric.isRate() ? " /s" : "";
        return metric.getName() + ": " + tags.getTitle() + suffix;
    }

    @SuppressWarnings("unchecked")
    private JSONArray generateColumns() {
        JSONArray cols = new JSONArray();
        cols.add(newColumn("Date", "datetime"));
        for (Metric metric : metrics) {
            for (TagsArray timeSeries : metric.timeSeries.keySet()) {
                cols.add(newColumn(renderLineTitle(metric, timeSeries),
                        "number"));
            }
        }
        return cols;
    }

    @SuppressWarnings("unchecked")
    private JSONObject newDataCell(double value) {
        JSONObject cell = new JSONObject();
        cell.put("v", value);
        return cell;
    }

    @SuppressWarnings("unchecked")
    private JSONObject newTsCell(long ts) {
        JSONObject cell = new JSONObject();
        cell.put("v", ts * 1000);
        return cell;
    }

    @SuppressWarnings("unchecked")
    private JSONArray generateRows() {
        // figure out the entire time series
        ArrayList<Long> timeSeries = new ArrayList<Long>();
        for (Metric metric : metrics) {
            for (TagsArray t : metric.timeSeries.keySet()) {
                timeSeries = TimeSeries.merge(timeSeries,
                        metric.timeSeries.get(t));
            }
        }
        JSONObject nullCell = new JSONObject();
        JSONArray rows = new JSONArray();
        for (long ts : timeSeries) {
            JSONObject row = new JSONObject();
            JSONArray cells = new JSONArray();
            cells.add(newTsCell(ts));
            for (Metric metric : metrics) {
                for (TagsArray t : metric.timeSeries.keySet()) {
                    ArrayList<DataPoint> points = metric.timeSeries.get(t);
                    if (points.size() > 0 && points.get(0).ts == ts) {
                        cells.add(newDataCell(points.get(0).value));
                        points.remove(0);
                    } else {
                        cells.add(nullCell);
                    }
                }
            }
            row.put("c", cells);
            rows.add(row);
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject result = new JSONObject();
        result.put("cols", generateColumns());
        result.put("rows", generateRows());
        return result;
    }
}
