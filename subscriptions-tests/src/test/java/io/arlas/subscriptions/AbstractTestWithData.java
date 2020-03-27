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

package io.arlas.subscriptions;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.*;

public abstract class AbstractTestWithData extends AbstractTestContext {

    @BeforeClass
    public static void beforeClass() {
        try {
            DataSetTool.loadDataSet(false);
            DataSetTool.loadSubscriptions(true);
        } catch (IOException | ArlasSubscriptionsException | ArlasException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() {
        DataSetTool.clearDataSet();
        DataSetTool.clearSubscriptions(true);
    }

    protected Map<String, Object> generateTestSubscription() {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("created_by","gisaia");
        jsonAsMap.put("active",true);
        jsonAsMap.put("starts_at",1564578988l);
        jsonAsMap.put("expires_at",2145913200l);
        jsonAsMap.put("title","title");
        UserSubscription.Hits hits = new UserSubscription.Hits();
        hits.filter="filter";
        hits.projection="projection";
        UserSubscription.Subscription subscription = new UserSubscription.Subscription();
        Map<String, Object> trigger = new HashMap<>();
        JSONObject coverage = new JSONObject();
        JSONArray jsonArrayExt = new JSONArray();
        List<LngLatAlt> coords = new ArrayList<>();
        coords.add(new LngLatAlt(-50, 50));
        coords.add(new LngLatAlt(50, 50));
        coords.add(new LngLatAlt(50, -50));
        coords.add(new LngLatAlt(-50, -50));
        coords.add(new LngLatAlt(-50, 50));
        new Polygon(coords).getExteriorRing().forEach(lngLatAlt -> {
            JSONArray jsonArrayLngLat = new JSONArray();
            jsonArrayLngLat.add(0, lngLatAlt.getLongitude());
            jsonArrayLngLat.add(1, lngLatAlt.getLatitude());
            jsonArrayExt.add(jsonArrayLngLat);
        });
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonArrayExt);
        coverage.put("type", "Polygon");
        coverage.put("coordinates", jsonArray);
        trigger.put("geometry", coverage);
        trigger.put("job", Arrays.asList("Actor"));
        trigger.put("event", Arrays.asList("UPDATE"));
        trigger.put("correlationId","2007");
        subscription.callback ="callback";
        subscription.hits =hits;
        subscription.trigger = trigger;
        Map<String, String> userMetadatas = new HashMap<>();
        userMetadatas.put("correlationId","2007");
        jsonAsMap.put("userMetadatas",userMetadatas);
        jsonAsMap.put("subscription",subscription);
        return jsonAsMap;
    }
}