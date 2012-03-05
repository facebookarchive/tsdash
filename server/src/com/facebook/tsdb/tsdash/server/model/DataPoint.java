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

import org.apache.hadoop.hbase.util.Bytes;
import org.json.simple.JSONArray;

import com.facebook.tsdb.tsdash.server.data.hbase.DataPointQualifier;

public class DataPoint implements Comparable<DataPoint> {

    public long ts;
    public double value;

    public DataPoint(long ts, double value) {
        this.ts = ts;
        this.value = value;
    }

    public int intValue() {
        return (int) value;
    }

    @Override
    public String toString() {
        return "(" + ts + " -> " + String.format("%.3f", value) + ")";
    }

    public static double decodeValue(byte[] encoded, byte[] qualifier) {
        if (DataPointQualifier.isFloat(qualifier)) {
            if (encoded.length == 8) {
                return Float.intBitsToFloat(Bytes.toInt(encoded, 4));
            } else {
                // length is 4
                return Float.intBitsToFloat(Bytes.toInt(encoded));
            }
        }
        return Bytes.toLong(encoded);
    }

    @Override
    public int compareTo(DataPoint other) {
        return (int) (ts - other.ts);
    }

    @SuppressWarnings("unchecked")
    public JSONArray toJSONObject() {
        JSONArray obj = new JSONArray();
        obj.add(ts);
        obj.add(value);
        return obj;
    }
}
