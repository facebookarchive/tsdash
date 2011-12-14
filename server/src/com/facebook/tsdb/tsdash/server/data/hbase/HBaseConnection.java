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
    
    private static Logger logger =
            Logger.getLogger("com.facebook.tsdb.services");
    
    public static final String DATA_TABLE_KEY = "hbase.tsdash.datatable";
    public static final String UID_TABLE_KEY = "hbase.tsdash.uidtable";
    public static final String ZK_QUORUM_KEY = "hbase.zookeeper.quorum";    
    
    private static String dataTable = "tsdb";
    private static String idsTable = "tsdb-uid";
    private static String zookeeperQuorum = "localhost";
    
    private static Configuration conf = HBaseConfiguration.create();

    public static void configure(Properties tsdbConf) {
        dataTable = tsdbConf.getProperty(DATA_TABLE_KEY, dataTable);
        idsTable = tsdbConf.getProperty(UID_TABLE_KEY, idsTable);
        zookeeperQuorum = tsdbConf.getProperty(ZK_QUORUM_KEY, zookeeperQuorum);
        conf.setStrings(ZK_QUORUM_KEY, zookeeperQuorum);
        logger.info("HBase configuration: using tables '" + dataTable + "', "
                + "'" + idsTable + "', quorum '" + zookeeperQuorum + "'");
    }
    
    public static HTable getDataTableConn() throws IOException {
        return new HTable(conf, dataTable);
    }
    
    public static HTable getIDsTableConn() throws IOException {
        return new HTable(conf, idsTable);
    }
}
