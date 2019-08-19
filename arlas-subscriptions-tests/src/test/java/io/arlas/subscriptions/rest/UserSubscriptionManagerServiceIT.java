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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserSubscriptionManagerServiceIT extends AbstractTestWithData {

    @Test
    public void test01GetAllUserSubscriptions() {
        when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1));
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
                .then().statusCode(200)
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
    }

    @Test
    public void test06PutUserSubscription() throws Exception {
        given().contentType("application/json")
                .when().body(generateTestSubscription())
                .put(arlasSubManagerPath + "subscriptions/1234")
                .then().statusCode(201)
                .body("title", equalTo("title"));

        assertThat(DataSetTool.getUserSubscriptionFromMongo("1234").get().title, is("title"));
        assertThat(DataSetTool.getUserSubscriptionFromES("1234").title, is("title"));
    }

    @Test
    public void test07DeleteExistingUserSubscription() throws Exception {
        when()
                .get(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2));

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
                .body("size()", is(1));
    }
}
