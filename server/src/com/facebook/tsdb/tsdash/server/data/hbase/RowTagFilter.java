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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.log4j.Logger;

import com.facebook.tsdb.tsdash.server.model.ID;
import com.facebook.tsdb.tsdash.server.model.Tag;

public class RowTagFilter {

    protected static Logger logger = Logger
            .getLogger("com.facebook.tsdb.services");

    private final Tag[] tags;

    public RowTagFilter(Tag[] tags) {
        this.tags = tags;
    }

    public RowTagFilter(Map<String, String> tags, IDMap idMap) {
        ArrayList<Tag> tagsList = new ArrayList<Tag>();
        for (String tag : tags.keySet()) {
            try {
                ID tagID = idMap.getTagID(tag);
                ID tagValueID = idMap.getTagValueID(tags.get(tag));
                tagsList.add(new Tag(tagID, tagValueID, idMap));
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        Collections.sort(tagsList, Tag.keyComparator());
        this.tags = tagsList.toArray(new Tag[0]);
    }

    /**
     * decide if we filter the row or not
     *
     * @param timeSeries
     * @return true if we have to filter it false if we can accept it
     */
    public boolean filterRow(Tag[] rowTags) {
        // all filter tags must exist in the row tag list
        for (Tag tag : tags) {
            boolean found = false;
            // we can just iterate over the row tags, as there are just a few
            for (Tag rowTag : rowTags) {
                if (Tag.keyValueComparator().compare(tag, rowTag) == 0) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }
        return false;
    }

    /**
     * generate the RowFilter used for filter rows remotely, in the RegionServer
     *
     * @return
     */
    public Filter getRemoteFilter() {
        FilterList filterList = new FilterList();
        for (Tag tag : tags) {
            filterList.addFilter(new RowFilter(CompareFilter.CompareOp.EQUAL,
                    new RegexStringComparator(tag.toHexString())));
        }
        return filterList;
    }

    @Override
    public String toString() {
        String ret = "";
        for (Tag tag : tags) {
            ret += tag + "\n";
        }
        return ret;
    }

}
