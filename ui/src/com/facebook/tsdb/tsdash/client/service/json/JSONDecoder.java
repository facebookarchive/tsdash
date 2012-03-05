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
package com.facebook.tsdb.tsdash.client.service.json;

import com.facebook.tsdb.tsdash.client.service.ServiceException;

public abstract class JSONDecoder<T> {

    abstract T decode(String jsonText);

    private static ErrorDecoder errorDecoder = new ErrorDecoder();

    public T tryDecode(String jsonText) throws JSONParseException {
        try {
            return decode(jsonText);
        } catch (Exception e) {
            throw new JSONParseException(jsonText);
        }
    }

    public T tryDecodeFromService(String jsonText) throws JSONParseException,
            ServiceException {
        try {
            return decode(jsonText);
        } catch (Exception e) {
            // we try to decode an error
            ServiceException serviceException = null;
            try {
                serviceException = errorDecoder.decode(jsonText);
            } catch (Exception errorException) {
                throw new JSONParseException(jsonText);
            }
            throw serviceException;
        }
    }
}
