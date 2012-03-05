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

import java.util.Arrays;
import java.util.regex.Pattern;

import com.google.common.primitives.UnsignedBytes;

public class ID implements Comparable<ID> {

    public static final int BYTES = 3;
    private static final byte[] null_id = { 0, 0, 0 };
    public static final ID NULL_ID = new ID(null_id);

    public byte[] id;

    public ID(byte[] id) {
        this.id = id;
    }

    @Override
    public int compareTo(ID other) {
        int ret = UnsignedBytes.lexicographicalComparator().compare(id,
                other.id);
        return ret;
    }

    public boolean isNull() {
        return this == NULL_ID || this.compareTo(NULL_ID) == 0;
    }

    @Override
    public String toString() {
        return UnsignedBytes.join(".", id);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    public String toHexString() {
        return Pattern.quote(new String(id));
    }

}
