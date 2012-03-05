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
package com.facebook.tsdb.tsdash.client.model;

import java.util.Date;

public class TimeRange {

    public static final long _15_MIN = 15 * 60; // seconds
    public static final long _1_H = 4 * _15_MIN; // seconds

    // they are both timestamps
    public long from;
    public long to;

    public TimeRange(long from, long to) {
        this.from = from;
        this.to = to;
    }

    public TimeRange(long secondsBack) {
        Date now = new Date();
        this.to = now.getTime();
        this.from = this.to - secondsBack * 1000;
    }

    public long getSeconds() {
        return (to - from) / 1000;
    }

    /**
     * move the time range with an offset
     *
     * @param offset
     *            seconds
     */
    public void move(int offset) {
        Date now = new Date();
        // add the time skew to the offset
        offset += (now.getTime() - to) / 1000;
        to += offset * 1000;
        from += offset * 1000;
    }
}
