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
package com.facebook.tsdb.tsdash.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.facebook.tsdb.tsdash.server.data.TsdbDataProvider;
import com.facebook.tsdb.tsdash.server.data.TsdbDataProviderFactory;
import com.facebook.tsdb.tsdash.server.gnuplot.GnuplotOptions;
import com.facebook.tsdb.tsdash.server.gnuplot.GnuplotProcess;
import com.facebook.tsdb.tsdash.server.model.Metric;
import com.facebook.tsdb.tsdash.server.model.MetricQuery;

public class PlotEndpoint extends TsdbServlet {

    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        try {
            // decode parameters
            String jsonParams = request.getParameter("params");
            if (jsonParams == null) {
                throw new Exception("Parameters not specified");
            }
            JSONObject jsonParamsObj = (JSONObject) JSONValue.parse(jsonParams);
            long tsFrom = (Long) jsonParamsObj.get("tsFrom");
            long tsTo = (Long) jsonParamsObj.get("tsTo");
            long width = (Long) jsonParamsObj.get("width");
            long height = (Long) jsonParamsObj.get("height");
            boolean surface = (Boolean) jsonParamsObj.get("surface");
            boolean palette = false;
            if (jsonParams.contains("palette")) {
                palette = (Boolean) jsonParamsObj.get("palette");
            }
            JSONArray metricsArray = (JSONArray) jsonParamsObj.get("metrics");
            if (metricsArray.size() == 0) {
                throw new Exception("No metrics to fetch");
            }
            MetricQuery[] metricQueries = new MetricQuery[metricsArray.size()];
            for (int i = 0; i < metricsArray.size(); i++) {
                metricQueries[i] = MetricQuery
                        .fromJSONObject((JSONObject) metricsArray.get(i));
            }
            TsdbDataProvider dataProvider = TsdbDataProviderFactory.get();
            long ts = System.currentTimeMillis();
            Metric[] metrics = new Metric[metricQueries.length];
            for (int i = 0; i < metrics.length; i++) {
                MetricQuery q = metricQueries[i];
                metrics[i] = dataProvider.fetchMetric(q.name, tsFrom, tsTo,
                        q.tags, q.orders);
                metrics[i] = metrics[i].dissolveTags(q.getDissolveList(),
                        q.aggregator);
                if (q.rate) {
                    metrics[i].computeRate();
                }
            }
            long loadTime = System.currentTimeMillis() - ts;
            // check to see if we have data
            boolean hasData = false;
            for (Metric metric : metrics) {
                if (metric.hasData()) {
                    hasData = true;
                    break;
                }
            }
            if (!hasData) {
                throw new Exception("No data");
            }
            JSONObject responseObj = new JSONObject();
            JSONArray encodedMetrics = new JSONArray();
            for (Metric metric : metrics) {
                encodedMetrics.add(metric.toJSONObject());
            }
            // plot just the first metric for now
            GnuplotProcess gnuplot = GnuplotProcess.create(surface);
            GnuplotOptions options = new GnuplotOptions(surface);
            options.enablePalette(palette);
            options.setDimensions((int) width, (int) height)
                   .setTimeRange(tsFrom, tsTo);
            String plotFilename = gnuplot.plot(metrics, options);
            gnuplot.close();

            responseObj.put("metrics", encodedMetrics);
            responseObj.put("loadtime", loadTime);
            responseObj.put("ploturl", generatePlotURL(plotFilename));
            out.println(responseObj.toJSONString());
            long renderTime = System.currentTimeMillis() - ts - loadTime;
            logger.info("[Plot] time frame: " + (tsTo - tsFrom) + "s, "
                    + "load time: " + loadTime + "ms, " + "render time: "
                    + renderTime + "ms");
        } catch (Throwable e) {
            out.println(getErrorResponse(e));
        }
        out.close();
    }

}
