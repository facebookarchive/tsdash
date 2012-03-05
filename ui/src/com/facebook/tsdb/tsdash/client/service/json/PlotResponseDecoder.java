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
package com.facebook.tsdb.tsdash.client.service.json;

import com.facebook.tsdb.tsdash.client.model.MetricHeader;
import com.facebook.tsdb.tsdash.client.model.PlotResponse;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class PlotResponseDecoder extends JSONDecoder<PlotResponse> {
    @Override
    PlotResponse decode(String jsonText) {
        PlotResponse response = new PlotResponse();
        response.dataSize = jsonText.length();
        JSONObject jsonObj = JSONParser.parseStrict(jsonText).isObject();
        response.serverLoadTime = (long) jsonObj.get("loadtime").isNumber()
                .doubleValue();
        response.plotURL = jsonObj.get("ploturl").isString().stringValue();
        JSONArray jsonMetrics = jsonObj.get("metrics").isArray();
        for (int i = 0; i < jsonMetrics.size(); i++) {
            JSONObject metricObj = jsonMetrics.get(i).isObject();
            response.metrics.add(MetricHeader.fromJSONObject(metricObj));
        }
        return response;

    }
}
