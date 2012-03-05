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

import com.facebook.tsdb.tsdash.server.data.TsdbDataProvider;
import com.facebook.tsdb.tsdash.server.data.TsdbDataProviderFactory;

public class MetricsEndpoint extends TsdbServlet {

    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        PrintWriter out = response.getWriter();
        try {
            TsdbDataProvider dataProvider = TsdbDataProviderFactory.get();
            String[] metrics = dataProvider.getMetrics();
            response.setContentType("text/plain");
            JSONArray encoded = new JSONArray();
            for (String metric : metrics) {
                encoded.add(metric);
            }
            out.println(encoded.toJSONString());
        } catch (Exception e) {
            out.println(getErrorResponse(e));
        }
        out.close();
    }
}
