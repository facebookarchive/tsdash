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
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

public class HBaseConnection {

    private static Logger logger = Logger
            .getLogger("com.facebook.tsdb.services");

    public static final String TSDB_DATA_TABLE = "hbase.tsdash.datatable";
    public static final String TSDB_UID_TABLE = "hbase.tsdash.uidtable";
    public static final String ZK_QUORUM = "hbase.zookeeper.quorum";
    public static final String ZK_CLIENTPORT = "hbase.zookeeper.property.clientPort";
    public static final String ZK_ZNODE_PARENT = "zookeeper.znode.parent";

    private static String dataTable = "tsdb";
    private static String idsTable = "tsdb-uid";

    private static Configuration conf = HBaseConfiguration.create();

    public static void configure(Properties tsdbConf) {
        dataTable = tsdbConf.getProperty(TSDB_DATA_TABLE, dataTable);
        idsTable = tsdbConf.getProperty(TSDB_UID_TABLE, idsTable);
        final String zookeeperQuorum =
            tsdbConf.getProperty(ZK_QUORUM,"localhost");
        final String zookeeperClientPort =
            tsdbConf.getProperty(ZK_CLIENTPORT,"2181");
        final String parentZnode =
            tsdbConf.getProperty(ZK_ZNODE_PARENT, "/hbase");
        conf.setStrings(ZK_QUORUM, zookeeperQuorum);
        conf.setStrings(ZK_CLIENTPORT, zookeeperClientPort);
        conf.setStrings(ZK_ZNODE_PARENT, parentZnode);
        logger.info("HBase configuration: " + " using tables '" + dataTable
                + "', " + "'" + idsTable + "', quorum '" + zookeeperQuorum
                + "':" + parentZnode);
    }

    public static HTable getDataTableConn() throws IOException {
        return new HTable(conf, dataTable);
    }

    public static HTable getIDsTableConn() throws IOException {
        return new HTable(conf, idsTable);
    }
}
