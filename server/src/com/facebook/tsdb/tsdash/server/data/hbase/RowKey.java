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

import java.util.Arrays;

import org.apache.hadoop.hbase.util.Bytes;

import com.facebook.tsdb.tsdash.server.model.ID;
import com.facebook.tsdb.tsdash.server.model.Tag;
import com.facebook.tsdb.tsdash.server.model.TagsArray;
import com.google.common.primitives.UnsignedBytes;

public class RowKey {

    public static final int PREFIX_TS_BYTES = 4;

    private final IDMap idMap;
    private final byte[] key;

    public RowKey(byte[] key, IDMap idMap) {
        this.key = key;
        this.idMap = idMap;
    }

    public byte[] getID() {
        return Arrays.copyOf(key, ID.BYTES);
    }

    public byte[] getKey() {
        return key;
    }

    public TagsArray getTags(ID[] tagsPri) {
        int prefixLen = ID.BYTES + PREFIX_TS_BYTES;
        int tagBytes = 2 * ID.BYTES;
        int tagsCount = (key.length - prefixLen) / tagBytes;
        Tag[] tags = new Tag[tagsCount];
        int offset = prefixLen;
        for (int i = 0; i < tags.length; i++) {
            tags[i] = new Tag(
                    Arrays.copyOfRange(key, offset, offset + ID.BYTES),
                    Arrays.copyOfRange(key, offset + ID.BYTES, offset + 2
                            * ID.BYTES), idMap);
            offset += tagBytes;
        }
        return new TagsArray(tags, tagsPri, idMap);
    }

    public static long baseTsFromRowKey(byte[] rowKey) {
        return Bytes.toInt(rowKey, ID.BYTES, PREFIX_TS_BYTES);
    }

    public String prefixToString() {
        return UnsignedBytes.join("",
                Arrays.copyOf(key, ID.BYTES + PREFIX_TS_BYTES));
    }

    public static int prefixLen() {
        return ID.BYTES + PREFIX_TS_BYTES;
    }

    @Override
    public String toString() {
        String ret = prefixToString();
        int tagsNo = (key.length - prefixLen()) / (2 * ID.BYTES);
        for (int i = 0; i < tagsNo; i++) {
            int offset = prefixLen() + i * 2 * ID.BYTES;
            ret += " "
                    + UnsignedBytes.join(
                            ".",
                            Arrays.copyOfRange(key, offset, offset + 2
                                    * ID.BYTES));
        }
        return ret;
    }
}
