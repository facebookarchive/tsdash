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
import java.util.Comparator;

import com.facebook.tsdb.tsdash.server.data.hbase.IDMap;
import com.facebook.tsdb.tsdash.server.data.hbase.IDNotFoundException;

public class Tag {

    public ID keyID;
    public ID valueID;
    public String key = "";
    public String value = "";

    private static Comparator<Tag> keyComparator = new Comparator<Tag>() {
        @Override
        public int compare(Tag t1, Tag t2) {
            return t1.keyID.compareTo(t2.keyID);
        }
    };

    private static Comparator<Tag> keyValueComparator = new Comparator<Tag>() {
        @Override
        public int compare(Tag t1, Tag t2) {
            int keyCmp = t1.keyID.compareTo(t2.keyID);
            if (keyCmp == 0) {
                return t1.valueID.compareTo(t2.valueID);
            }
            return keyCmp;
        }
    };

    private static class TagsArrayComparator implements Comparator<TagsArray> {

        private int[] map = new int[0];

        private void ensureMapLength(int length) {
            if (length > map.length) {
                map = new int[length];
            }
        }

        @Override
        public int compare(TagsArray tlist1, TagsArray tlist2) {
            // first set mapping between the two lists
            TagsArray shortList = tlist1;
            TagsArray longList = tlist2;
            if (tlist1.length() > tlist2.length()) {
                shortList = tlist2;
                longList = tlist1;
            }
            ensureMapLength(shortList.length());
            // do the mapping
            for (int i = 0; i < shortList.length(); i++) {
                map[i] = longList.binarySearch(shortList.getOrdered(i));
            }
            for (int i = 0; i < shortList.length(); i++) {
                if (map[i] < 0) {
                    // skip this tag, as it doesn't exist in the other list
                    continue;
                }
                int tagCmp = shortList.getOrdered(i).valueID.compareTo(longList
                        .get(map[i]).valueID);
                if (tagCmp != 0) {
                    return tagCmp;
                }
            }
            return 0;
        }
    };

    private void loadStringFields(IDMap idMap) {
        try {
            key = this.keyID.isNull() ? "NULL" : idMap.getTag(this.keyID);
            value = this.valueID.isNull() ? "NULL" : idMap
                    .getTagValue(this.valueID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Tag(byte[] rawKeyID, byte[] rawValueID, IDMap idMap) {
        this.keyID = new ID(rawKeyID);
        this.valueID = new ID(rawValueID);
        loadStringFields(idMap);
    }

    public Tag(ID keyID, ID valueID, IDMap idMap) {
        this.keyID = keyID;
        this.valueID = valueID;
        loadStringFields(idMap);
    }

    public Tag(String key, String value, IDMap idMap) throws IOException,
            IDNotFoundException {
        this.key = key;
        this.value = value;
        this.keyID = idMap.getTagID(key);
        this.valueID = idMap.getTagValueID(value);
    }

    public Tag(String tagName, IDMap idMap) throws IOException,
            IDNotFoundException {
        this.key = tagName;
        this.keyID = idMap.getTagID(tagName);
    }

    public static Comparator<TagsArray> arrayComparator() {
        // we need to create a separate instance because the comparator is
        // not thread safe, as it needs the temporary storage array
        return new TagsArrayComparator();
    }

    @Override
    public String toString() {
        return "(" + key + "[" + keyID + "]:" + value + "[" + valueID + "])";
    }

    public static String join(String glue, Tag[] tags) {
        String ret = "";
        for (int i = 0; i < tags.length; i++) {
            if (i > 0) {
                ret += glue;
            }
            ret += tags[i].toString();
        }
        return ret;
    }

    public String toHexString() {
        return keyID.toHexString() + valueID.toHexString();
    }

    public static Comparator<Tag> keyComparator() {
        return keyComparator;
    }

    public static Comparator<Tag> keyValueComparator() {
        return keyValueComparator;
    }
}
