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

public class PlotResponse {

    public long dataSize = 0;
    public long serverLoadTime = 0;
    public ArrayList<MetricHeader> metrics = new ArrayList<MetricHeader>();
    public String plotURL;

    @Override
    public String toString() {
        return "Data: " + dataSize + " bytes; " + "Load time: "
                + serverLoadTime + " ms; " + "Plot URL: " + plotURL + " "
                + "Metrics loaded: " + metrics.size();
    }
}
