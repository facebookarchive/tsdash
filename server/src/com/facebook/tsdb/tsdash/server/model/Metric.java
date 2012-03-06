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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.facebook.tsdb.tsdash.server.data.agg.Aggregator;
import com.facebook.tsdb.tsdash.server.data.agg.AverageAggregator;
import com.facebook.tsdb.tsdash.server.data.agg.MaxAggregator;
import com.facebook.tsdb.tsdash.server.data.agg.MinAggregator;
import com.facebook.tsdb.tsdash.server.data.agg.SumAggregator;
import com.facebook.tsdb.tsdash.server.data.hbase.IDMap;
import com.facebook.tsdb.tsdash.server.data.hbase.IDNotFoundException;
import com.google.common.primitives.UnsignedBytes;

public class Metric {

    protected static Logger logger = Logger
            .getLogger("com.facebook.tsdb.services");

    private static final int GUESS_MATCH_THOLD = 5; // no. of matches
    private static final long DATA_MISSING_THOLD = 3 * 60; // 3 minutes

    public static Aggregator.Type DEFAULT_AGGREGATOR = Aggregator.Type.SUM;

    private final IDMap idMap;
    private static HashMap<Aggregator.Type, Aggregator> aggregators =
        new HashMap<Aggregator.Type, Aggregator>();

    static {
        loadAggregators();
    }

    private static void loadAggregators() {
        aggregators.put(Aggregator.Type.SUM, new SumAggregator());
        aggregators.put(Aggregator.Type.MIN, new MinAggregator());
        aggregators.put(Aggregator.Type.MAX, new MaxAggregator());
        aggregators.put(Aggregator.Type.AVG, new AverageAggregator());
    }

    private final byte[] id;
    private final String name;
    public TreeMap<TagsArray, ArrayList<DataPoint>> timeSeries =
        new TreeMap<TagsArray, ArrayList<DataPoint>>(
            Tag.arrayComparator());
    private final HashSet<String> dissolvedTags = new HashSet<String>();
    private String aggregatorName = null;
    private boolean rate = false;

    public Metric(byte[] id, String name, IDMap idMap) {
        this.id = id;
        this.name = name;
        this.idMap = idMap;
    }

    public String getName() {
        return name;
    }

    public String getAggregator() {
        return aggregatorName;
    }

    public boolean isAggregated() {
        return aggregatorName != null;
    }

    public boolean hasData() {
        for (ArrayList<DataPoint> dataPoints : timeSeries.values()) {
            if (dataPoints.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<DataPoint> getDataPoints(TagsArray tagsArray) {
        if (!timeSeries.containsKey(tagsArray)) {
            timeSeries.put(tagsArray, new ArrayList<DataPoint>());
        }
        return timeSeries.get(tagsArray);
    }

    private static boolean arrayIsSorted(ArrayList<DataPoint> array) {
        for (int i = 0; i < array.size() - 1; i++) {
            if (array.get(i).compareTo(array.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    public HashMap<String, HashSet<String>> getTagsSet() {
        HashMap<String, HashSet<String>> tagsSet =
            new HashMap<String, HashSet<String>>();
        for (TagsArray rowTags : timeSeries.keySet()) {
            for (Tag tag : rowTags.asArray()) {
                if (!tagsSet.containsKey(tag.key)) {
                    tagsSet.put(tag.key, new HashSet<String>());
                }
                HashSet<String> values = tagsSet.get(tag.key);
                if (!tag.valueID.isNull() && !values.contains(tag.value)) {
                    values.add(tag.value);
                }
            }
        }
        return tagsSet;
    }

    public HashSet<String> getCommonTags(Set<String> tagsSet) {
        HashSet<String> commonTags = new HashSet<String>();
        HashMap<String, Integer> tagCount = new HashMap<String, Integer>();
        for (String tag : tagsSet) {
            tagCount.put(tag, 0);
        }
        // count tags
        for (TagsArray rowTags : timeSeries.keySet()) {
            for (Tag tag : rowTags.asArray()) {
                tagCount.put(tag.key, tagCount.get(tag.key) + 1);
            }
        }
        // select only those tags that are in all rows fetched
        for (String tag : tagCount.keySet()) {
            if (tagCount.get(tag) == timeSeries.size()) {
                commonTags.add(tag);
            }
        }
        return commonTags;
    }

    /**
     * the input time series are ordered by time stamp and they have no
     * duplicates
     *
     * @param ts1
     * @param ts2
     * @return the merged time series
     */
    public ArrayList<DataPoint> mergeAndCombineTimeSeries(
            ArrayList<DataPoint> ts1, ArrayList<DataPoint> ts2,
            Aggregator agg) {
        ArrayList<DataPoint> merged = new ArrayList<DataPoint>();
        int i = 0;
        int j = 0;
        while (i < ts1.size() && j < ts2.size()) {
            DataPoint dp1 = ts1.get(i);
            DataPoint dp2 = ts2.get(j);
            DataPoint dp;
            int cmp = dp1.compareTo(dp2);
            if (cmp < 0) {
                dp = dp1;
                i++;
            } else if (cmp > 0) {
                dp = dp2;
                j++;
            } else {
                // reduce
                agg.reset();
                agg.add(dp1.value);
                agg.add(dp2.value);
                // reusing one of the object
                dp = dp1;
                dp.value = agg.getValue();
                i++;
                j++;
            }
            merged.add(dp);
        }
        for (; i < ts1.size(); i++) {
            merged.add(ts1.get(i));
        }
        for (; j < ts2.size(); j++) {
            merged.add(ts2.get(j));
        }
        return merged;
    }

    private Aggregator getAggregator(String aggregatorName) {
        Aggregator agg = aggregators.get(Aggregator.Type.valueOf(aggregatorName
                .toUpperCase()));
        if (agg == null) {
            return aggregators.get(DEFAULT_AGGREGATOR);
        }
        return agg;
    }

    public long guessTimeCycle() {
        long cycle = Integer.MAX_VALUE;
        int matches = 0;
        for (ArrayList<DataPoint> points : timeSeries.values()) {
            for (int i = 0; i < points.size() - 1; i++) {
                if (points.get(i + 1).ts - points.get(i).ts != cycle) {
                    cycle = points.get(i + 1).ts - points.get(i).ts;
                    matches = 0;
                } else {
                    matches++;
                }
                if (matches == GUESS_MATCH_THOLD) {
                    return cycle;
                }
            }
        }
        return cycle;
    }

    public void alignAllTimeSeries() {
        long cycle = guessTimeCycle();
        // wrap the time stamps to a multiple of cycle
        for (ArrayList<DataPoint> points : timeSeries.values()) {
            for (DataPoint p : points) {
                p.ts -= p.ts % cycle;
            }
        }
        // do the actual aligning
        TreeMap<TagsArray, ArrayList<DataPoint>> aligned =
            new TreeMap<TagsArray, ArrayList<DataPoint>>(
                Tag.arrayComparator());
        for (TagsArray header : timeSeries.keySet()) {
            aligned.put(header, TimeSeries.align(timeSeries.get(header),
                    cycle));
        }
        // align the time series between each other
        long maxmin = Long.MIN_VALUE;
        long minmax = Long.MAX_VALUE;
        long maxmax = Long.MIN_VALUE;
        for (ArrayList<DataPoint> points : aligned.values()) {
            if (points.size() == 0) {
                logger.error("We have found an empty timeseries");
                continue;
            }
            DataPoint first = points.get(0);
            if (points.size() > 0 && points.get(0).ts > maxmin) {
                maxmin = first.ts;
            }
            DataPoint last = points.get(points.size() - 1);
            if (last.ts < minmax) {
                minmax = last.ts;
            }
            if (last.ts > maxmax) {
                maxmax = last.ts;
            }
        }
        if (maxmax - minmax > DATA_MISSING_THOLD) {
            // we've just detected missing data from this set of time series
            logger.error("Missing data detected");

            // add padding to maxmax
            for (ArrayList<DataPoint> points : aligned.values()) {
                if (points.size() == 0) {
                    continue;
                }
                long max = points.get(points.size() - 1).ts;
                for (long ts = max + cycle; ts <= maxmax; ts += cycle) {
                    points.add(new DataPoint(ts, 0.0));
                }
            }
        } else {
            // cut off the tail
            for (ArrayList<DataPoint> points : aligned.values()) {
                while (points.size() > 0
                        && points.get(points.size() - 1).ts > minmax) {
                    points.remove(points.size() - 1);
                }
            }
        }
        // cut off the head
        for (ArrayList<DataPoint> points : aligned.values()) {
            while (points.size() > 0 && points.get(0).ts < maxmin) {
                points.remove(0);
            }
        }
        this.timeSeries = aligned;
    }

    /**
     * create a new metric with rows aggregated after dissolving the given tags.
     * The resulted metric will not be able to accept filters on this tag
     * anymore.
     *
     * @param tagName
     * @param aggregatorName
     *            'sum', 'max', 'min' or 'avg'
     * @return a new Metric object that contains the aggregated rows
     * @throws IDNotFoundException
     * @throws IOException
     */
    public Metric dissolveTags(ArrayList<String> tagsName,
            String aggregatorName)
            throws IOException, IDNotFoundException {
        if (tagsName.size() == 0) {
            return this;
        }
        HashMap<String, HashSet<String>> tagsSet = getTagsSet();
        for (String tagName : tagsName) {
            if (!tagsSet.containsKey(tagName)) {
                // TODO: throw an exception here
                logger.error("Dissolve error: tag '" + tagName
                        + "' is not part of the tag set");
                return null;
            }
            // we can only dissolve once a given tag
            if (dissolvedTags.contains(tagName)) {
                // TODO: throw an exception here
                logger.error("Metric already dissolved tag " + tagName);
                return null;
            }
        }
        // this aligns the time series in a perfect grid
        alignAllTimeSeries();

        Metric newData = new Metric(id, name, idMap);
        Tag[] toDissolve = new Tag[tagsName.size()];
        for (int i = 0; i < toDissolve.length; i++) {
            toDissolve[i] = new Tag(tagsName.get(i), idMap);
            newData.dissolvedTags.add(tagsName.get(i));
        }
        TreeMap<TagsArray, ArrayList<ArrayList<DataPoint>>> dissolved =
            new TreeMap<TagsArray, ArrayList<ArrayList<DataPoint>>>(
                Tag.arrayComparator());
        // sort the tags we will dissolve for calling disableTags()
        Arrays.sort(toDissolve, Tag.keyComparator());
        for (TagsArray header : timeSeries.keySet()) {
            TagsArray dissolvedRowTags = header.copy();
            if (toDissolve.length == 1) {
                dissolvedRowTags.disableTag(toDissolve[0]);
            } else {
                dissolvedRowTags.disableTags(toDissolve);
            }
            if (!dissolved.containsKey(dissolvedRowTags)) {
                dissolved.put(dissolvedRowTags,
                        new ArrayList<ArrayList<DataPoint>>());
            }
            dissolved.get(dissolvedRowTags).add(timeSeries.get(header));
        }
        Aggregator aggregator = getAggregator(aggregatorName);
        newData.aggregatorName = aggregatorName;
        for (TagsArray header : dissolved.keySet()) {
            newData.timeSeries.put(header,
                    TimeSeries.aggregate(dissolved.get(header), aggregator));
        }
        return newData;
    }

    /**
     * replace the time series with the rate of change
     */
    public void computeRate() {
        for (ArrayList<DataPoint> points : timeSeries.values()) {
            TimeSeries.computeRate(points);
        }
        rate = true;
    }

    public boolean isRate() {
        return rate;
    }

    @SuppressWarnings("unchecked")
    private JSONObject encodeTagsSet(HashMap<String, HashSet<String>> tagsSet) {
        JSONObject tagsSetObj = new JSONObject();
        for (String tag : tagsSet.keySet()) {
            JSONArray tagValuesArray = new JSONArray();
            if (tagsSet.get(tag) != null) {
                for (String value : tagsSet.get(tag)) {
                    tagValuesArray.add(value);
                }
            }
            tagsSetObj.put(tag, tagValuesArray);
        }
        return tagsSetObj;
    }

    @SuppressWarnings("unchecked")
    private JSONArray encodeCommonTags(HashSet<String> commonTags) {
        JSONArray commonTagsArray = new JSONArray();
        for (String tag : commonTags) {
            commonTagsArray.add(tag);
        }
        return commonTagsArray;
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject() {
        JSONObject topObj = new JSONObject();
        topObj.put("name", name);
        HashMap<String, HashSet<String>> tagsSet = getTagsSet();
        topObj.put("tags", encodeTagsSet(tagsSet));
        topObj.put("commontags",
                encodeCommonTags(getCommonTags(tagsSet.keySet())));
        return topObj;
    }

    public String toJSONString() {
        return toJSONObject().toJSONString();
    }

    @Override
    public String toString() {
        String ret = "Metric " + UnsignedBytes.join("", id) + '\n';
        for (TagsArray tagsArray : timeSeries.keySet()) {
            ret += Tag.join(" ", tagsArray.asArray()) + "\n";
            ret += "datapoints sorted: "
                    + arrayIsSorted(timeSeries.get(tagsArray));
            ret += "\n";
            /*
             * int count = 0; for (DataPoint dataPoint :
             * timeSeries.get(tagsArray)) { ret += " " + dataPoint; if (count ==
             * 100) { break; } count++; }
             */
            ret += "\n";
        }
        return ret;
    }

}
