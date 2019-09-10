/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.subscriptions.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class JSONHelper {


    public static Object readJSONValue(String jsonPath, Object jsonObject) {
        return JSONHelper.readJSONValue(jsonPath, jsonObject, false);
    }

    public static Object readJSONValue(String jsonPath, Object jsonObject, boolean partialFind) {
        try {
            String key = jsonPath.contains(".")?jsonPath.substring(0,jsonPath.indexOf(".")):jsonPath;
            if (!StringUtils.isEmpty(key)
                    && jsonObject instanceof HashMap
                    && ((HashMap)jsonObject).containsKey(key)) {
                return readJSONValue(jsonPath.substring(jsonPath.indexOf(".")+1),((HashMap)jsonObject).get(key), true);
            } else {
                if (partialFind) {
                    return jsonObject;
                } else {
                    return null;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
