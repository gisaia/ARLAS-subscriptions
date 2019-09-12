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
import io.restassured.http.ContentType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserSubscriptionAuthManagerServiceIT extends AbstractTestWithData {

    //
    // Tests without identity header
    //
    @Test
    public void test01GetAllUserSubscriptionsWithoutHeader() {
        when().get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(401);
    }

    @Test
    public void test02GetUserSubscriptionWithoutHeader() {
        when().get(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(401);
    }

    @Test
    public void test03PostUserSubscriptionWithoutHeader() {
        given().contentType("application/json")
                .body(generateTestSubscription())
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(401);
    }
    @Test
    public void test04PutUserSubscriptionWithoutHeader() {
        given().contentType("application/json")
                .when().body(generateTestSubscription())
                .put(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(401);
    }

    @Test
    public void test05DeleteExistingUserSubscriptionWithoutHeader() {
        when().delete(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(401);
    }

    //
    // Tests with identity header value 'admin'
    //

    @Test
    public void test06GetAllUserSubscriptionsWithHeaderAdmin() {
        given().header(identityHeader, "admin").when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(403);
    }

    @Test
    public void test07GetUserSubscriptionWithHeaderAdmin() {
        given().header(identityHeader, "admin").when()
                .get(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(403);
    }

    @Test
    public void test08PostUserSubscriptionWithHeaderAdmin() {
        given().contentType("application/json")
                .header(identityHeader, "admin").when()
                .body(generateTestSubscription())
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(403);
    }

    @Test
    public void test09PutUserSubscriptionWithHeaderAdmin() {
        given().contentType("application/json")
                .header(identityHeader, "admin").when()
                .body(generateTestSubscription())
                .put(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(403);
    }

    @Test
    public void test10DeleteExistingUserSubscriptionWithHeaderAdmin() {
        given().header(identityHeader, "admin").when()
                .delete(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(403);
    }

    //
    // Tests with identity header value != {owner} (i.e. "foo")
    //

    @Test
    public void test11GetAllUserSubscriptionsWithHeaderNotOwner() {
        given().header(identityHeader, "foo")
                .when().get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(0));
    }

    @Test
    public void test12GetUserSubscriptionWithHeaderNotOwner() {
        given().header(identityHeader, "foo").when()
                .get(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(404);
    }

    @Test
    public void test13PostUserSubscriptionWithHeaderNotOwner() {
        given().contentType("application/json")
                .header(identityHeader, "foo").when()
                .body(generateTestSubscription())
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(403);
    }

    @Test
    public void test14PostInvalidUserSubscriptionWithHeaderNotOwner() {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("created_by","gisaia");
        given().contentType("application/json")
                .header(identityHeader, "foo").when()
                .body(jsonAsMap)
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(400);
    }

    @Test
    public void test15PutUserSubscriptionWithHeaderNotOwner() {
        given().contentType("application/json")
                .header(identityHeader, "foo").when()
                .body(generateTestSubscription())
                .put(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(404);
    }

    @Test
    public void test16DeleteExistingUserSubscriptionWithHeaderNotOwner() {
        given().header(identityHeader, "foo").when()
                .delete(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(404);
    }

    //
    // Tests with identity header value == {owner} (i.e. "gisaia")
    //
    @Test
    public void test17GetAllUserSubscriptionsWithHeaderOwner() {
        given().header(identityHeader, "gisaia").when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", equalTo(1));
    }

    @Test
    public void test18GetUserSubscriptionNotFound() {
        given().header(identityHeader, "gisaia").when()
                .get(arlasSubManagerPath + "subscriptions/666")
                .then().statusCode(404);
    }

    @Test
    public void test19GetUserSubscriptionWithHeaderOwner() {
        given().header(identityHeader, "gisaia").when()
                .get(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo("1234"));
    }

    @Test
    public void test20PostInvalidUserSubscriptionWithHeaderOwner() {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("created_by","gisaia");
        given().contentType("application/json")
                .header(identityHeader, "gisaia").when()
                .body(jsonAsMap)
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(400);
    }

    @Test
    public void test21PostUserSubscriptionWithHeaderOwner() throws Exception {
        String id = given().contentType("application/json")
                .header(identityHeader, "gisaia").when()
                .body(generateTestSubscription())
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(201)
                .body("title", equalTo("title"))
                .extract().jsonPath().get("id");

        assertThat(DataSetTool.getUserSubscriptionFromMongo(id).get().title, is("title"));
        assertThat(DataSetTool.getUserSubscriptionFromES(id).title, is("title"));
    }

    @Test
    public void test22PutUserSubscriptionWithHeaderOwner() throws Exception {
        given().contentType("application/json")
                .header(identityHeader, "gisaia").when()
                .body(generateTestSubscription())
                .put(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(201)
                .contentType(ContentType.JSON)
                .body("title", equalTo("title"));

        assertThat(DataSetTool.getUserSubscriptionFromMongo("1234").get().title, is("title"));
        assertThat(DataSetTool.getUserSubscriptionFromES("1234").title, is("title"));
    }

    @Test
    public void test23DeleteExistingUserSubscriptionWithHeaderOwner() throws Exception {
        given().header(identityHeader, "gisaia").when()
                .delete(arlasSubManagerPath + "subscriptions/1234")
                .then()
                .statusCode(202)
                .contentType(ContentType.JSON)
                .body("id", equalTo("1234"));

        assertTrue(DataSetTool.getUserSubscriptionFromMongo("1234").get().getDeleted());
        assertTrue(DataSetTool.getUserSubscriptionFromES("1234").getDeleted());
    }
}
