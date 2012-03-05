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
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.facebook.tsdb.tsdash.server.data.TsdbDataProvider;
import com.facebook.tsdb.tsdash.server.data.TsdbDataProviderFactory;
import com.facebook.tsdb.tsdash.server.model.Metric;
import com.facebook.tsdb.tsdash.server.model.MetricQuery;

/**
 * fetches the header information (tags set, common tags) for a given time frame
 *
 * @author cgheorghe
 *
 */
public class MetricHeaderEndpoint extends TsdbServlet {

    private static final long serialVersionUID = 1L;

    /**
     * GET params:
     * "metric" - metric name
     * "from" - start of the time range
     * "to" - end of the time range
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        PrintWriter out = response.getWriter();
        try {
            // decode parameters
            long ts = System.currentTimeMillis();
            String jsonParams = request.getParameter("params");
            if (jsonParams == null) {
                throw new Exception("Parameters not specified");
            }
            JSONObject jsonParamsObj = (JSONObject) JSONValue.parse(jsonParams);
            long tsFrom = (Long) jsonParamsObj.get("tsFrom");
            long tsTo = (Long) jsonParamsObj.get("tsTo");
            String metricName = (String) jsonParamsObj.get("metric");
            HashMap<String, String> tags = MetricQuery
                    .decodeTags((JSONObject) jsonParamsObj.get("tags"));
            if (metricName == null) {
                throw new Exception("Missing parameter");
            }
            TsdbDataProvider dataProvider = TsdbDataProviderFactory.get();
            Metric metric = dataProvider.fetchMetricHeader(metricName, tsFrom,
                    tsTo, tags);
            out.println(metric.toJSONString());
            long loadTime = System.currentTimeMillis() - ts;
            logger.info("[Header] time frame: " + (tsTo - tsFrom) + "s, "
                    + "metric: " + metricName + ", tags: " + tags + ", "
                    + "load time: " + loadTime + "ms");
        } catch (Exception e) {
            out.println(getErrorResponse(e));
        }
        out.close();
    }
}
