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

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import com.facebook.tsdb.tsdash.server.model.ID;
import com.google.common.collect.ImmutableBiMap;

/**
 * Object used for loading a particular ID map from HBase in a thread-safe
 * manner. The maps are immutable, so they don't need thread-safety.
 *
 * @author cgheorghe
 *
 */
public class IDMapSyncLoader {

    private static final byte[] ID_FAMILY = HBaseDataProvider.ID_FAMILY
            .getBytes();

    private HTable IDsTable = null;
    private ImmutableBiMap<String, ID> map = null;
    private final byte[] qualifier;

    public IDMapSyncLoader(byte[] qualifier) {
        this.qualifier = qualifier;
    }

    public synchronized ImmutableBiMap<String, ID> get() throws IOException {
        if (map == null) {
            map = loadMap(qualifier);
        }
        return map;
    }

    private ImmutableBiMap<String, ID> loadMap(byte[] qualifier)
            throws IOException {
        if (IDsTable == null) {
            IDsTable = HBaseConnection.getIDsTableConn();
        }
        ResultScanner scanner = IDsTable.getScanner(ID_FAMILY, qualifier);
        ImmutableBiMap.Builder<String, ID> mapBuilder =
            new ImmutableBiMap.Builder<String, ID>();
        for (Result result : scanner) {
            String rowKey = (new String(result.getRow())).trim();
            if (!rowKey.equals("")) {
                byte[] value = result.getValue(ID_FAMILY, qualifier);
                mapBuilder.put(rowKey, new ID(value));
            }
        }
        ImmutableBiMap<String, ID> map = mapBuilder.build();
        return map;
    }
}
