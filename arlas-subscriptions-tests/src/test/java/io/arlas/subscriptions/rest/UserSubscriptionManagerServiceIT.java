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

package io.arlas.subscriptions.rest;

import io.arlas.subscriptions.AbstractTestWithData;
import io.arlas.subscriptions.DataSetTool;
import io.arlas.subscriptions.model.UserSubscription;
import io.restassured.http.ContentType;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.hamcrest.Matcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.util.*;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class UserSubscriptionManagerServiceIT extends AbstractTestWithData {

    @Test
    public void testGetAllUserSubscriptions() throws Exception {
        // GET all collections
        getAllUserSubscriptions(emptyArray());
    }

    @Test
    public void testGetUserSubscriptionNotFound() throws Exception {
        when().get(arlasSubManagerPath + "subscriptions/foo")
                .then().statusCode(404);
    }

    @Test
    public void testGetUserSubscriptionFound() throws Exception {
        when().get(arlasSubManagerPath + "subscriptions/1234")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo("1234"));
    }

    @Test
    public void testPostUserSubscription() throws Exception{
        Map<String, Object> jsonAsMap = generateTestSubscription();
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200);
        getAllUserSubscriptions(hasSize(1));
        getAllUserSubscriptions(hasItem(hasProperty("created_by",hasValue("John Doe"))));

    }

    @Test
    public void testPostInvalidUserSubscription() throws Exception{
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("created_by","John Doe");
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(400);
    }

    @Test
    public void testPutUserSubscription() throws Exception{
        Map<String, Object> jsonAsMap = generateTestSubscription();
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .put(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(201)
                .body("title", equalTo("title"));
        assertThat(DataSetTool.getUserSubscriptionFromMongo("1234").get().title, is("title"));
        assertThat(DataSetTool.getUserSubscriptionFromES("1234").title, is("title"));
    }

    @Test
    public void testDeleteExistingUserSubscription() throws Exception {
        when().delete(arlasSubManagerPath + "subscriptions/1234")
                .then()
                .statusCode(202)
                .contentType(ContentType.JSON)
                .body("id", equalTo("1234"));

        assertTrue(DataSetTool.getUserSubscriptionFromMongo("1234").get().getDeleted());
        assertTrue(DataSetTool.getUserSubscriptionFromES("1234").getDeleted());
    }

    private Map<String, Object> generateTestSubscription() {
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

    private void getAllUserSubscriptions(Matcher matcher) throws InterruptedException {
        int cpt = 0;
        while (cpt > 0 && cpt < 5) {
            try {
                when().get(arlasSubManagerPath + "subscriptions/")
                        .then().statusCode(200)
                        .body(matcher);
                cpt = -1;
            } catch (Exception e) {
                cpt++;
                Thread.sleep(1000);
            }
        }
    }

}
