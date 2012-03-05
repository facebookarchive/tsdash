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

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;

public class DataPoint {

    public long ts;
    public double value;
    private static DateTimeFormat formatter = DateTimeFormat
            .getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE_SECOND);

    public DataPoint(long ts, double value) {
        this.ts = ts;
        this.value = value;
    }

    public static DataPoint fromJSONObject(JSONArray obj) {
        long ts = (long) obj.get(0).isNumber().doubleValue();
        double value = (long) obj.get(1).isNumber().doubleValue();
        return new DataPoint(ts, value);
    }

    public String getDayTime() {
        return formatter.format(new Date(ts * 1000));
    }
}
