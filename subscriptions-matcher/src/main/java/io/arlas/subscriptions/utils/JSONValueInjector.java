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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import static io.arlas.subscriptions.utils.JSONHelper.readJSONValue;

public class JSONValueInjector {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final WKTWriter wktWriter = new WKTWriter();
    private static final GeoJsonReader geoJsonReader = new GeoJsonReader();

    /*
     * Replace mentions to {json.path} in String by corresponding value in JSONObject.
     */
    public static String inject(String s, JSONObject object) throws JsonProcessingException, ParseException {
        String ret = s;
        String[] jsonPaths = StringUtils.substringsBetween(s,"{", "}");
        for(String jsonPath : jsonPaths) {
            String searchString = "{" + jsonPath + "}";
            Object value = readJSONValue(jsonPath, object);
            if (jsonPath.endsWith("geometry") || jsonPath.endsWith("centroid")) {
                Geometry geometry = geoJsonReader.read(objectMapper.writeValueAsString(value));
                String replacement = wktWriter.write(geometry);
                ret = StringUtils.replace(ret, searchString, replacement);
            } else {
                ret = StringUtils.replace(ret, searchString, value.toString());
            }
        }
        return ret;
    }
}
