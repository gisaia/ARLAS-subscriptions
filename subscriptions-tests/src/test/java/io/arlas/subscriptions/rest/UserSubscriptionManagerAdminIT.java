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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.model.Filters;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.subscriptions.AbstractTestWithData;
import io.arlas.subscriptions.DataSetTool;
import io.arlas.subscriptions.model.IndexedUserSubscription;
import io.arlas.subscriptions.model.UserSubscription;
import io.restassured.http.ContentType;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UserSubscriptionManagerAdminIT extends AbstractTestWithData {

    //marks subscriptions created by given tests, to be removed after each test
    public static final String TEMP_SUBS_CREATED_BY = "junit";

    @After
    public void after() {
        DataSetTool.mongoCollection.deleteOne(new Document("created_by", TEMP_SUBS_CREATED_BY));
    }

    @Test
    public void testGetAllUserSubscriptions_withNoParams_shouldReturnAllSubscriptions() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByCreatedBy_shouldReturnSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?created-by=gisaia")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByUnknownCreatedBy_shouldReturnNoSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?created-by=unknown")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void testGetAllUserSubscriptions_ByBefore_shouldReturnSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?before=1564578989")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByNoneBefore_shouldReturnNoSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?before=1564578987")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void testGetAllUserSubscriptions_ByAfter_shouldReturnSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?after=1564578987")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByNoneAfter_shouldReturnNoSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?after=1564578989")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void testGetAllUserSubscriptions_ByActive_shouldReturnSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?active=true")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByNotActive_shouldReturnNoSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?active=false")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void testGetAllUserSubscriptions_ByStarted_shouldReturnSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?started=true")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByNotStarted_shouldReturnNoSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?started=false")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void testGetAllUserSubscriptions_ByExpired_shouldReturnNoSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?expired=true")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void testGetAllUserSubscriptions_ByNotExpired_shouldReturnSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?expired=false")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByDeleted_shouldReturnSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?deleted=true")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByNotDeleted_shouldReturnNotDeletedSubscription() {

        insertTestSubscriptionInMongo(true, null);

        when()
                .get(arlasSubManagerPath + "admin/subscriptions")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(2));

        when()
                .get(arlasSubManagerPath + "admin/subscriptions?deleted=false")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_ByCreatedByAdmin_shouldReturnNoSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?created-by-admin=true")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void testGetAllUserSubscriptions_ByNotCreatedByAdmin_shouldReturnSubscription() {
        when()
                .get(arlasSubManagerPath + "admin/subscriptions?created-by-admin=false")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testGetAllUserSubscriptions_shouldSortSubscriptions() {

        UserSubscription subscription = insertTestSubscriptionInMongo(null, 1564578987l);

        when()
                .get(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("subscriptions[0].id", equalTo("1234"))
                .body("subscriptions[1].id", equalTo(subscription.getId()));

        subscription.setCreated_at(1564578989l);
        DataSetTool.mongoCollection.replaceOne(Filters.eq("created_by", TEMP_SUBS_CREATED_BY), subscription);

        when()
                .get(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("subscriptions[0].id", equalTo(subscription.getId()))
                .body("subscriptions[1].id", equalTo("1234"));
    }

    @Test
    public void testPostUserSubscription_withCorrectSubscription_shouldCreateSubscription() throws Exception {
        Map<String, Object> jsonAsMap = generateTestSubscription();
        jsonAsMap.put("created_by", TEMP_SUBS_CREATED_BY);

        String id = given().contentType("application/json")
                .body(jsonAsMap)
                .post(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(201)
                .body("title", equalTo("title"))
                .extract().jsonPath().get("id");

        assertThat(DataSetTool.getUserSubscriptionFromMongo(id).get().title, is("title"));
        assertThat(DataSetTool.getUserSubscriptionFromMongo(id).get().getCreated_by_admin(), is(true));
        assertThat(DataSetTool.getUserSubscriptionFromES(id).title, is("title"));
    }

    @Test
    public void testPostUserSubscription_withMissingFields_shouldFail() {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("created_by","John Doe");
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .post(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(400);
        when()
                .get(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void testPostUserSubscription_withBadTrigger_shouldFail() {

        Map<String,Object> jsonAsMap = generateTestSubscription();
        ((UserSubscription.Subscription )jsonAsMap.get("subscription")).trigger.put("job","Aviator");
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .post(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(503);
    }

    @Test
    public void testGetUserSubscription_WithNonExistingSubscription_shouldReturnNotFound()  {
        when().get(arlasSubManagerPath + "admin/subscriptions/foo")
                .then().statusCode(404);
    }

    @Test
    public void testGetUserSubscription_withExistingSubscription_shouldReturnIt() {
        when().get(arlasSubManagerPath + "admin/subscriptions/1234")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo("1234"));
    }

    @Test
    public void testGetUserSubscription_withNoAdditionalParam_shouldReturnDeletedSubscrption() {

        UserSubscription deletedSubscription = insertTestSubscriptionInMongo(true, null);

        when().get(arlasSubManagerPath + "admin/subscriptions/" + deletedSubscription.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(deletedSubscription.getId()));
    }

    @Test
    public void testGetUserSubscription_withDeleteParamFalse_shouldNotReturnDeletedSubscription() {

        UserSubscription deletedSubscription = insertTestSubscriptionInMongo(true, null);

        when().get(arlasSubManagerPath + "admin/subscriptions/" + deletedSubscription.getId() + "?deleted=false")
                .then().statusCode(404);
    }

    @Test
    public void testDeleteUserSubscription_withUnknownSubscriptions_shouldFail() throws Exception {

        when().delete(arlasSubManagerPath + "admin/subscriptions/foo")
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeleteUserSubscription_withExistingSubscriptions_shouldDeleteIt() throws Exception {

        String subscriptionId = insertTestSubscriptionInMongo(false, null).getId();
        insertTestSubscriptionInES(subscriptionId);

        when().delete(arlasSubManagerPath + "admin/subscriptions/" + subscriptionId)
                .then()
                .statusCode(202)
                .contentType(ContentType.JSON)
                .body("id", equalTo(subscriptionId));

        assertTrue(DataSetTool.getUserSubscriptionFromMongo(subscriptionId).get().getDeleted());
        assertTrue(DataSetTool.getUserSubscriptionFromES(subscriptionId).getDeleted());
    }

    @Test
    public void testPutUserSubscription_withValidSubscription_shouldUpdateIt() throws Exception {
        given().contentType("application/json")
                .when().body(generateTestSubscription())
                .put(arlasSubManagerPath + "admin/subscriptions/1234")
                .then().statusCode(201)
                .body("title", equalTo("title"));

        assertThat(DataSetTool.getUserSubscriptionFromMongo("1234").get().title, is("title"));
        assertThat(DataSetTool.getUserSubscriptionFromES("1234").title, is("title"));
    }

    @Test
    public void testPutUserSubscription_withUnknownId_shouldFail() throws Exception {
        given().contentType("application/json")
                .when().body(generateTestSubscription())
                .put(arlasSubManagerPath + "admin/subscriptions/foo")
                .then().statusCode(404);
    }

    @Test
    public void testPutUserSubscription_withMissingFields_shouldFail() throws Exception {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("created_by","John Doe");

        given().contentType("application/json")
                .when().body(jsonAsMap)
                .put(arlasSubManagerPath + "admin/subscriptions/foo")
                .then().statusCode(400);
    }

    @Test
    public void testPutUserSubscription_withBadTrigger_shouldFail() {

        Map<String,Object> jsonAsMap = generateTestSubscription();
        ((UserSubscription.Subscription )jsonAsMap.get("subscription")).trigger.put("job","Aviator");
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .put(arlasSubManagerPath + "admin/subscriptions/1234")
                .then().statusCode(503);
    }

    private UserSubscription insertTestSubscriptionInMongo(Boolean deleted, Long createdAt) {
        UserSubscription subscription = new UserSubscription();
        subscription.setDeleted(deleted);
        subscription.setId("9876");
        subscription.created_by = TEMP_SUBS_CREATED_BY;
        subscription.setCreated_at(createdAt);
        DataSetTool.mongoCollection.insertOne(subscription);
        return subscription;
    }

    private void insertTestSubscriptionInES(String id) throws JsonProcessingException, ArlasException {
        IndexedUserSubscription indexedUserSubscription = new IndexedUserSubscription();
        indexedUserSubscription.setId(id);
        indexedUserSubscription.created_by = TEMP_SUBS_CREATED_BY;
        DataSetTool.client.index(DataSetTool.SUBSCRIPTIONS_INDEX_NAME, indexedUserSubscription.getId(), indexedUserSubscription);
    }

}
