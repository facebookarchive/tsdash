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
import java.util.HashMap;
import java.util.HashSet;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

public class MetricHeader implements Comparable<MetricHeader> {

    public String name;
    public HashSet<String> commonTags = new HashSet<String>();
    public HashMap<String, ArrayList<String>> tagsSet =
        new HashMap<String, ArrayList<String>>();

    public MetricHeader(String name) {
        this.name = name;
    }

    public static MetricHeader fromJSONObject(JSONObject obj) {
        MetricHeader metricData = new MetricHeader(obj.get("name").isString()
                .stringValue());
        // tags set
        JSONObject tagsObj = obj.get("tags").isObject();
        for (String tag : tagsObj.keySet()) {
            ArrayList<String> tagValues = new ArrayList<String>();
            JSONArray tagValuesArray = tagsObj.get(tag).isArray();
            for (int i = 0; i < tagValuesArray.size(); i++) {
                tagValues.add(tagValuesArray.get(i).isString().stringValue());
            }
            metricData.tagsSet.put(tag, tagValues);
        }
        // common tags
        JSONArray commonTagsArray = obj.get("commontags").isArray();
        for (int i = 0; i < commonTagsArray.size(); i++) {
            metricData.commonTags.add(commonTagsArray.get(i).isString()
                    .stringValue());
        }
        return metricData;
    }

    @Override
    public String toString() {
        String ret = name + ": ";
        for (String tag : tagsSet.keySet()) {
            ret += "{" + tag + ":";
            for (String tagValue : tagsSet.get(tag)) {
                ret += " " + tagValue;
            }
            ret += "}, ";
        }
        return ret;
    }

    @Override
    public int compareTo(MetricHeader other) {
        if (tagsSet.size() != other.tagsSet.size()) {
            return tagsSet.size() - other.tagsSet.size();
        }
        for (String tag : tagsSet.keySet()) {
            if (!other.tagsSet.containsKey(tag)) {
                return 1;
            }
            ArrayList<String> tagValues = tagsSet.get(tag);
            ArrayList<String> otherTagValues = other.tagsSet.get(tag);
            if (tagValues.size() != otherTagValues.size()) {
                return 1;
            }
            HashSet<String> otherSet = new HashSet<String>(otherTagValues);
            for (String tagValue : tagValues) {
                if (!otherSet.contains(tagValue)) {
                    return 1;
                }
            }
        }
        return 0;
    }
}
