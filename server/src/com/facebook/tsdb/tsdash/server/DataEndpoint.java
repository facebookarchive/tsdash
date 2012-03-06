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

import com.facebook.tsdb.tsdash.server.data.DataTable;
import com.facebook.tsdb.tsdash.server.data.TsdbDataProvider;
import com.facebook.tsdb.tsdash.server.data.TsdbDataProviderFactory;
import com.facebook.tsdb.tsdash.server.model.Metric;
import com.facebook.tsdb.tsdash.server.model.MetricQuery;

public class DataEndpoint extends TsdbServlet {

    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        try {
            long ts = System.currentTimeMillis();
            // decode parameters
            String jsonParams = request.getParameter("params");
            if (jsonParams == null) {
                throw new Exception("Parameters not specified");
            }
            JSONObject jsonParamsObj = (JSONObject) JSONValue.parse(jsonParams);
            long tsFrom = (Long) jsonParamsObj.get("tsFrom");
            long tsTo = (Long) jsonParamsObj.get("tsTo");
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
            JSONObject responseObj = new JSONObject();
            JSONArray encodedMetrics = new JSONArray();
            for (Metric metric : metrics) {
                encodedMetrics.add(metric.toJSONObject());
            }
            responseObj.put("metrics", encodedMetrics);
            responseObj.put("loadtime", loadTime);
            DataTable dataTable = new DataTable(metrics);
            responseObj.put("datatable", dataTable.toJSONObject());
            out.println(responseObj.toJSONString());
            long encodingTime = System.currentTimeMillis() - ts - loadTime;
            logger.info("[Data] time frame: " + (tsTo - tsFrom) + "s, "
                    + "load time: " + loadTime + "ms, " + "encoding time: "
                    + encodingTime + "ms");
        } catch (Exception e) {
            out.println(getErrorResponse(e));
        }
        out.close();
    }

}
