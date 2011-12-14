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

import java.util.ArrayList;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;

public class ArrayListDecoder extends JSONDecoder<ArrayList<String>> {

    @Override
    public ArrayList<String> decode(String jsonText) {
        JSONArray jsonArray = JSONParser.parseStrict(jsonText).isArray();
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < jsonArray.size(); i++) {
            result.add(jsonArray.get(i).isString().stringValue());
        }
        return result;
    }
}
