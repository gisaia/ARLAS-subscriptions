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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserSubscriptionManagerEndUserIT extends AbstractTestWithData {

    @Test
    public void test01GetAllUserSubscriptions() {
        when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void test02GetUserSubscriptionNotFound()  {
        when().get(arlasSubManagerPath + "subscriptions/foo")
                .then().statusCode(404);
    }

    @Test
    public void test03GetUserSubscriptionFound() {
        when().get(arlasSubManagerPath + "subscriptions/1234")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo("1234"));
    }

    @Test
    public void test04PostUserSubscription() throws Exception {
        String id = given().contentType("application/json")
                .body(generateTestSubscription())
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(201)
                .body("title", equalTo("title"))
                .extract().jsonPath().get("id");

        assertThat(DataSetTool.getUserSubscriptionFromMongo(id).get().title, is("title"));
        assertThat(DataSetTool.getUserSubscriptionFromES(id).title, is("title"));
    }

    @Test
    public void test05PostInvalidUserSubscription() {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("created_by","John Doe");
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(400);
        when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(2));
    }

    @Test
    public void test06PostInvalidUserSubscription() {
        Map<String,Object> jsonAsMap = generateTestSubscription();
        ((UserSubscription.Subscription )jsonAsMap.get("subscription")).trigger.put("job","Aviator");
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(503);
    }

    @Test
    public void test07PutUserSubscription() throws Exception {
        given().contentType("application/json")
                .when().body(generateTestSubscription())
                .put(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(201)
                .body("title", equalTo("title"));

        assertThat(DataSetTool.getUserSubscriptionFromMongo("1234").get().title, is("title"));
        assertThat(DataSetTool.getUserSubscriptionFromES("1234").title, is("title"));
    }

    @Test
    public void test08GetAllUserSubscriptionsWithPaging() {
        given().param("size", "1")
                .param("page", "1")
                .when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(1))
                .body("total", equalTo(2));
        given().param("size", "1")
                .param("page", "2")
                .when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(1))
                .body("total", equalTo(2));
    }

    @Test
    public void test09DeleteExistingUserSubscription() throws Exception {
        when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(2));

        when().delete(arlasSubManagerPath + "subscriptions/1234")
                .then()
                .statusCode(202)
                .contentType(ContentType.JSON)
                .body("id", equalTo("1234"));

        assertTrue(DataSetTool.getUserSubscriptionFromMongo("1234").get().getDeleted());
        assertTrue(DataSetTool.getUserSubscriptionFromES("1234").getDeleted());

        when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void test10GetAllUserSubscriptionsWithParamBefore() {
        given().param("before", "2145913200")
                .when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void test11GetAllUserSubscriptionsWithParamExpiredTrue() {
        given().param("expired", "true")
                .when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void test12GetAllUserSubscriptionsWithParamExpiredFalse() {
        given().param("expired", "false")
                .when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void test13GetAllUserSubscriptionsWithParamActiveTrue() {
        given().param("active", "true")
                .when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void test14GetAllUserSubscriptionsWithParamActiveFalse() {
        given().param("active", "false")
                .when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }
}
