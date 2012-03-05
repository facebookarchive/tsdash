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
package com.facebook.tsdb.tsdash.server.data.hbase;

import java.util.HashSet;

import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;

import com.facebook.tsdb.tsdash.server.model.ID;

public class RowTagComparator extends WritableByteArrayComparable {

    private final HashSet<byte[]> rawTags;
    private final byte[] rawTag = new byte[2 * ID.BYTES];

    public RowTagComparator(HashSet<byte[]> rawTags) {
        this.rawTags = rawTags;
    }

    @Override
    public int compareTo(byte[] value) {
        int prefixLen = RowRange.prefixBytes();
        int tagsNo = (value.length - prefixLen) / (2 * ID.BYTES);
        if (tagsNo <= 0) {
            // the row has no tags
            return 0;
        }
        int matches = 0;
        for (int t = 0; t < tagsNo; t++) {
            for (int i = 0; i < 2 * ID.BYTES; i++) {
                rawTag[i] = value[prefixLen + t * 2 * ID.BYTES + i];
            }
            if (rawTags.contains(rawTag)) {
                matches++;
            }
        }
        // it should be 0 - equal - when all tags have been matched
        return matches - tagsNo;
    }
}
