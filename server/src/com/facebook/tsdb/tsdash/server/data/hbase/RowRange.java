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

import com.facebook.tsdb.tsdash.server.model.ID;
import com.google.common.primitives.UnsignedBytes;

public class RowRange {

    public static final int PREFIX_TS_ROUND = 60 * 60; // 1h, in seconds

    private final byte[] start = new byte[ID.BYTES + RowKey.PREFIX_TS_BYTES];
    private final byte[] stop = new byte[ID.BYTES + RowKey.PREFIX_TS_BYTES];
    private final byte[] last = new byte[ID.BYTES + RowKey.PREFIX_TS_BYTES];

    private final long startOffset;
    private final long lastOffset;

    public RowRange(byte[] metricID, long fromTs, long toTs) {
        Arrays.fill(start, (byte) 0);
        Arrays.fill(stop, (byte) 0);
        for (int i = 0; i < ID.BYTES; i++) {
            start[i] = metricID[i];
            stop[i] = metricID[i];
            last[i] = metricID[i];
        }
        long startTs = roundTs(fromTs);
        long lastTs = roundTs(toTs);
        long stopTs = roundTs(toTs) + PREFIX_TS_ROUND;
        int shift = 0;
        for (int i = ID.BYTES + RowKey.PREFIX_TS_BYTES - 1; i >= ID.BYTES; i--){
            start[i] = (byte) ((startTs >> shift) & 0xFF);
            stop[i] = (byte) ((stopTs >> shift) & 0xFF);
            last[i] = (byte) ((lastTs >> shift) & 0xFF);
            shift += 8;
        }
        startOffset = fromTs % PREFIX_TS_ROUND;
        lastOffset = toTs % PREFIX_TS_ROUND;
    }

    public byte[] getStart() {
        return start;
    }

    public byte[] getStop() {
        return stop;
    }

    public byte[] getLast() {
        return last;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getLastOffset() {
        return lastOffset;
    }

    public static long roundTs(long ts) {
        return ts - (ts % PREFIX_TS_ROUND);
    }

    @Override
    public String toString() {
        String res = "";
        res += UnsignedBytes.join(" ", start)
                + " offset: "
                + UnsignedBytes.join(" ",
                        DataPointQualifier.packedOffset(startOffset)) + '\n';
        res += UnsignedBytes.join(" ", last)
                + " offset: "
                + UnsignedBytes.join(" ",
                        DataPointQualifier.packedOffset(lastOffset)) + '\n';
        res += startOffset + " " + lastOffset + '\n';
        res += UnsignedBytes.join(" ", stop) + '\n';
        return res;
    }

    public static int prefixBytes() {
        return ID.BYTES + RowKey.PREFIX_TS_BYTES;
    }

}
