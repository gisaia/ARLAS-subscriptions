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

import java.util.HashMap;
import java.util.Optional;

public class JSONHelper {

    public static Optional<Object> readJSONValue(String jsonPath, Object jsonObject) {

        if (!(jsonObject instanceof HashMap)) { return Optional.empty(); }
        HashMap object = (HashMap) jsonObject;
        String[] keys = jsonPath.split("\\.");
        for (int i=0; i<keys.length; i++) {
            String key = keys[i];
            if (object.containsKey(key)) {
                if (i < keys.length - 1) {
                    object = (HashMap) object.get(key);
                } else {
                    return Optional.of(object.get(key));
                }
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
