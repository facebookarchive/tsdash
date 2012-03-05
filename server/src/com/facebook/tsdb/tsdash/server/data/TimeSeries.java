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

import com.facebook.tsdb.tsdash.server.model.DataPoint;

public class TimeSeries {

    public static ArrayList<Long> merge(ArrayList<Long> timeSeries,
            ArrayList<DataPoint> dataPoints) {
        ArrayList<Long> result = new ArrayList<Long>();
        int i = 0;
        int j = 0;
        while (i < timeSeries.size() && j < dataPoints.size()) {
            long cmp = timeSeries.get(i) - dataPoints.get(j).ts;
            if (cmp < 0) {
                result.add(timeSeries.get(i));
                i++;
            } else if (cmp > 0) {
                result.add(dataPoints.get(j).ts);
                j++;
            } else {
                result.add(timeSeries.get(i));
                i++;
                j++;
            }
        }
        for (; i < timeSeries.size(); i++) {
            result.add(timeSeries.get(i));
        }
        for (; j < dataPoints.size(); j++) {
            result.add(dataPoints.get(j).ts);
        }
        return result;
    }

}
