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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.facebook.tsdb.tsdash.server.data.TsdbDataProvider;
import com.facebook.tsdb.tsdash.server.model.DataPoint;
import com.facebook.tsdb.tsdash.server.model.ID;
import com.facebook.tsdb.tsdash.server.model.Metric;
import com.facebook.tsdb.tsdash.server.model.TagsArray;

public class HBaseDataProvider implements TsdbDataProvider {

    protected static Logger logger = Logger
            .getLogger("com.facebook.tsdb.services");

    public static final String ID_FAMILY = "id";
    public static final String NAME_FAMILY = "name";
    public static final String DATAPOINT_FAMILY = "t";

    public static final String METRIC_QUALIFIER = "metrics";
    public static final String TAG_QUALIFIER = "tagk";
    public static final String TAG_VALUE_QUALIFIER = "tagv";

    private final HTable dataTable;
    private final IDMap idMap = new IDMap();

    public HBaseDataProvider() throws IOException {
        dataTable = HBaseConnection.getDataTableConn();
    }

    private void pickDataPoints(ArrayList<DataPoint> dataPoints,
            RowRange rowRange, byte[] rowKey, Map<byte[], byte[]> cells) {
        boolean first = Bytes.startsWith(rowKey, rowRange.getStart());
        boolean last = Bytes.startsWith(rowKey, rowRange.getLast());
        long baseTs = RowKey.baseTsFromRowKey(rowKey);
        // skipping the points outside our time range
        for (byte[] key : cells.keySet()) {
            long offset = DataPointQualifier.offsetFromQualifier(key);
            if (first && offset < rowRange.getStartOffset()) {
                continue;
            }
            if (last && offset > rowRange.getLastOffset()) {
                continue;
            }
            DataPoint dataPoint = new DataPoint(baseTs + offset,
                    DataPoint.decodeValue(cells.get(key), key));
            dataPoints.add(dataPoint);
        }
    }

    private ID[] getTagIDs(String[] tags) {
        ArrayList<ID> tagsIDs = new ArrayList<ID>();
        for (String tag : tags) {
            try {
                ID tagID = idMap.getTagID(tag);
                tagsIDs.add(tagID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tagsIDs.toArray(new ID[0]);
    }

    @Override
    public Metric fetchMetric(String metric, long startTs, long toTs,
            Map<String, String> tags, String[] tagOrders) throws Exception {
        ID metricID = idMap.getMetricID(metric);
        Metric metricData = new Metric(metricID.id, metric, idMap);
        RowRange rowRange = new RowRange(metricID.id, startTs, toTs);
        RowTagFilter tagFilter = new RowTagFilter(tags, idMap);
        ID[] tagsPrio = getTagIDs(tagOrders);

        Scan scan = new Scan(rowRange.getStart(), rowRange.getStop());
        if (tags.size() > 0) {
            scan.setFilter(tagFilter.getRemoteFilter());
        }
        ResultScanner scanner = dataTable.getScanner(scan);
        int count = 0;
        int falsePositives = 0;
        // rows are in lexicographic order, which means there are already
        // ordered by time
        for (Result result : scanner) {
            RowKey rowKey = new RowKey(result.getRow(), idMap);
            TagsArray rowTags = rowKey.getTags(tagsPrio);
            if (tagFilter.filterRow(rowTags.asArray())) {
                falsePositives++;
            } else {
                pickDataPoints(metricData.getDataPoints(rowTags), rowRange,
                        rowKey.getKey(),
                        result.getFamilyMap(DATAPOINT_FAMILY.getBytes()));
            }
            count++;
        }
        logger.info(metric + ": " + count + " rows scanned, " + falsePositives
                + " false positives found");
        return metricData;
    }

    @Override
    public Metric fetchMetricHeader(String metric, long startTs, long toTs,
            Map<String, String> tags) throws Exception {
        ID metricID = idMap.getMetricID(metric);
        Metric metricData = new Metric(metricID.id, metric, idMap);
        RowRange rowRange = new RowRange(metricID.id, startTs, toTs);
        RowTagFilter tagFilter = new RowTagFilter(tags, idMap);

        Scan scan = new Scan(rowRange.getStart(), rowRange.getStop());
        if (tags.size() > 0) {
            scan.setFilter(tagFilter.getRemoteFilter());
        }
        ResultScanner scanner = dataTable.getScanner(scan);
        int count = 0;
        int falsePositives = 0;
        for (Result result : scanner) {
            RowKey rowKey = new RowKey(result.getRow(), idMap);
            TagsArray rowTags = rowKey.getTags(TagsArray.NATURAL_ORDER);
            if (tagFilter.filterRow(rowTags.asArray())) {
                falsePositives++;
            } else if (!metricData.timeSeries.containsKey(rowTags)) {
                metricData.timeSeries.put(rowTags, new ArrayList<DataPoint>());
            }
            count++;
        }
        logger.info("Fetching header for " + metric + ": " + count + " rows, "
                + falsePositives + " false positives");
        return metricData;
    }

    @Override
    public String[] getMetrics() throws Exception {
        return idMap.getMetrics();
    }

    @Override
    public String[] getTags(String metric) throws Exception {
        return idMap.getTags();
    }

    @Override
    public String[] getTagValues(String tag) throws Exception {
        return idMap.getTagValues();
    }

    @Override
    public byte[] getMetricID(String metric) throws Exception {
        return idMap.getMetricID(metric).id;
    }

    @Override
    public byte[] getTagID(String tag) throws Exception {
        return idMap.getTagID(tag).id;
    }

    @Override
    public byte[] getTagValueID(String tagValue) throws Exception {
        return idMap.getTagValueID(tagValue).id;
    }

}
