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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.subscriptions.model.SubscriptionEvent;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;

public class JSONHelperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final static String  jsonString =
            "{"
            + "    \"geo_event\": {"
            + "        \"id\": 1,"
            + "        \"value\": {"
            + "            \"name\": \"Object 1\","
            + "            \"type\" : \"geo_object\","
            + "            \"latitude\" : 37.33774833333334,"
            + "            \"longitude\" : -121.88670166666667"
            + "        }"
            + "    }"
            + "}";

    @Test
    public void readJSONValueTest() {
        try {
            SubscriptionEvent event = objectMapper.readValue(jsonString, SubscriptionEvent.class);
            assertThat(JSONHelper.readJSONValue("geo_event.id",event), equalTo(1));
            assertThat(JSONHelper.readJSONValue("geo_event.value.name",event), equalTo("Object 1"));
            assertThat(JSONHelper.readJSONValue("geo_event.value.latitude",event), equalTo(37.33774833333334d));
        } catch (IOException e) {
        }
    }
}
