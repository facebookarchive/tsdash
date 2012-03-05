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
package com.facebook.tsdb.tsdash.client.service;

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.client.model.Metric;
import com.facebook.tsdb.tsdash.client.model.MetricHeader;
import com.facebook.tsdb.tsdash.client.model.PlotResponse;
import com.facebook.tsdb.tsdash.client.model.TimeRange;
import com.facebook.tsdb.tsdash.client.model.TimeSeriesResponse;
import com.facebook.tsdb.tsdash.client.service.json.ArrayListDecoder;
import com.facebook.tsdb.tsdash.client.service.json.JSONDecoder;
import com.facebook.tsdb.tsdash.client.service.json.JSONParseException;
import com.facebook.tsdb.tsdash.client.service.json.MetricHeaderDecoder;
import com.facebook.tsdb.tsdash.client.service.json.PlotResponseDecoder;
import com.facebook.tsdb.tsdash.client.service.json.TimeSeriesDecoder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class HTTPService {

    public static final int TIMEOUT = 30000; // ms

    public static final String METRICS_URL = "metrics";
    public static final String DATA_URL = "data";
    public static final String PLOT_URL = "plot";
    public static final String METRIC_HEADER_URL = "header";

    public void loadMetricsName(
            final AsyncCallback<ArrayList<String>> callback) {
        get(callback, METRICS_URL, "", new ArrayListDecoder());
    }

    public void loadTimeSeries(TimeRange timeRange, ArrayList<Metric> metrics,
            final AsyncCallback<TimeSeriesResponse> callback) {
        // encode parameters
        JSONObject paramObj = new JSONObject();
        paramObj.put("tsFrom", new JSONNumber(timeRange.from / 1000));
        paramObj.put("tsTo", new JSONNumber(timeRange.to / 1000));
        JSONArray metricsArray = new JSONArray();
        for (int i = 0; i < metrics.size(); i++) {
            if (metrics.get(i).isPlottable()) {
                metricsArray.set(i, metrics.get(i).toJSONParam());
            }
        }
        paramObj.put("metrics", metricsArray);
        String param = "params=" + paramObj.toString();
        get(callback, DATA_URL, param, new TimeSeriesDecoder());
    }

    public void loadMetricHeader(Metric metric, TimeRange timeRange,
            final AsyncCallback<MetricHeader> callback) {
        JSONObject paramObj = new JSONObject();
        paramObj.put("tsFrom", new JSONNumber(timeRange.from / 1000));
        paramObj.put("tsTo", new JSONNumber(timeRange.to / 1000));
        paramObj.put("metric", new JSONString(metric.name));
        paramObj.put("tags", metric.encodeTags());
        String encodedParams = "params=" + paramObj.toString();
        get(callback, METRIC_HEADER_URL, encodedParams,
                new MetricHeaderDecoder());
    }

    public void loadPlot(TimeRange timeRange, ArrayList<Metric> metrics,
            int width, int height, boolean surface, boolean palette,
            final AsyncCallback<PlotResponse> callback) {
        JSONObject paramObj = new JSONObject();
        paramObj.put("tsFrom", new JSONNumber(timeRange.from / 1000));
        paramObj.put("tsTo", new JSONNumber(timeRange.to / 1000));
        paramObj.put("width", new JSONNumber(width));
        paramObj.put("height", new JSONNumber(height));
        paramObj.put("surface", JSONBoolean.getInstance(surface));
        paramObj.put("palette", JSONBoolean.getInstance(palette));
        JSONArray metricsArray = new JSONArray();
        for (int i = 0; i < metrics.size(); i++) {
            if (metrics.get(i).isPlottable()) {
                metricsArray.set(i, metrics.get(i).toJSONParam());
            }
        }
        paramObj.put("metrics", metricsArray);
        String param = "params=" + paramObj.toString();
        get(callback, PLOT_URL, param, new PlotResponseDecoder());
    }

    private <T> void get(final AsyncCallback<T> callback, final String url,
            String params, final JSONDecoder<T> decoder) {
        RequestBuilder req = new RequestBuilder(RequestBuilder.GET, url + "?"
                + params);
        req.setTimeoutMillis(TIMEOUT);
        req.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                try {
                    T result = decoder.tryDecodeFromService(response.getText());
                    callback.onSuccess(result);
                } catch (JSONParseException e) {
                    GWT.log("Error parsing data from '" + url + "'", e);
                    callback.onFailure(e);
                } catch (ServiceException e) {
                    GWT.log("Error in remote service", e);
                    callback.onFailure(e);
                }
            }

            @Override
            public void onError(Request request, Throwable e) {
                GWT.log("Error sending GET request to '" + url + "'", e);
                callback.onFailure(e);
            }
        });
        try {
            req.send();
        } catch (RequestException e) {
            GWT.log("Request exception for '" + url + "'", e);
            callback.onFailure(e);
        }
    }

}
