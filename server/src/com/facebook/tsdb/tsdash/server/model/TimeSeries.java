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
package com.facebook.tsdb.tsdash.server.model;

import java.util.ArrayList;

import com.facebook.tsdb.tsdash.server.data.agg.Aggregator;
import com.facebook.tsdb.tsdash.server.data.agg.AverageAggregator;

public class TimeSeries {

    private static DataPoint marker = new DataPoint(0, Double.MAX_VALUE);

    public static boolean isAligned(ArrayList<DataPoint> points, long cycle) {
        for (int i = 0; i < points.size() - 1; i++) {
            if (points.get(i + 1).ts - points.get(i).ts != cycle) {
                return false;
            }
        }
        return true;
    }

    public static ArrayList<DataPoint> averageDuplicates(
            ArrayList<DataPoint> points) {
        ArrayList<DataPoint> unique = new ArrayList<DataPoint>();
        Aggregator agg = new AverageAggregator();
        points.add(marker);
        int i = 0;
        while (i < points.size() - 1) {
            DataPoint point = points.get(i);
            agg.add(point.value);
            if (point.ts != points.get(i + 1).ts) {
                point.value = agg.getValue();
                unique.add(point);
                agg.reset();
            } // else it will continue to add to the aggregator
            i++;
        }
        return unique;
    }

    private static double linearEq(double x, double x1, double y1, double x2,
            double y2) {
        return (x - x1) * (y2 - y1) / (x2 - x1) + y1;
    }

    private static ArrayList<DataPoint> linearConnect(DataPoint x1,
            DataPoint x2, long cycle) {
        ArrayList<DataPoint> conn = new ArrayList<DataPoint>();
        for (long ts = x1.ts + cycle; ts < x2.ts; ts += cycle) {
            conn.add(new DataPoint(ts, linearEq(ts, x1.ts, x1.value, x2.ts,
                    x2.value)));
        }
        return conn;
    }

    public static ArrayList<DataPoint> lerp(ArrayList<DataPoint> points,
            long cycle) {
        ArrayList<DataPoint> continous = new ArrayList<DataPoint>();
        for (int i = 0; i < points.size() - 1; i++) {
            continous.add(points.get(i));
            if (points.get(i + 1).ts - points.get(i).ts != cycle) {
                continous.addAll(linearConnect(points.get(i),
                        points.get(i + 1), cycle));
            }
        }
        return continous;
    }

    /**
     * there are two sources of problems: multiple points on the same ts or
     * missing points for certain ts
     *
     * @param points
     * @param cycle
     * @return
     */
    public static ArrayList<DataPoint> align(ArrayList<DataPoint> points,
            long cycle) {
        if (isAligned(points, cycle)) {
            return points;
        }
        return lerp(averageDuplicates(points), cycle);
    }

    public static ArrayList<DataPoint> aggregate(
            ArrayList<ArrayList<DataPoint>> grouped, Aggregator agg) {
        ArrayList<DataPoint> aggregated = new ArrayList<DataPoint>();
        int maxSize = Integer.MIN_VALUE;
        for (ArrayList<DataPoint> points : grouped) {
            if (points.size() > maxSize) {
                maxSize = points.size();
            }
        }
        for (int i = 0; i < maxSize; i++) {
            agg.reset();
            DataPoint point = null;
            for (ArrayList<DataPoint> points : grouped) {
                if (i < points.size()) {
                    point = points.get(i);
                    agg.add(point.value);
                }
            }
            point.value = agg.getValue();
            aggregated.add(point);
        }
        return aggregated;
    }

    /**
     * Compute the rate for the given timeseries in-place
     * @param points
     */
    public static void computeRate(ArrayList<DataPoint> points) {
        for (int i = points.size() - 1; i > 0; i--) {
            DataPoint p1 = points.get(i);
            DataPoint p0 = points.get(i - 1);
            double rate = (p1.value - p0.value) / (p1.ts - p0.ts);
            p1.value = rate;
        }
        if (points.size() > 1) {
            points.get(0).value = points.get(1).value;
        }
    }
}
