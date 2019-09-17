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

import com.mongodb.client.model.Filters;
import io.arlas.subscriptions.AbstractTestWithData;
import io.arlas.subscriptions.DataSetTool;
import io.arlas.subscriptions.model.UserSubscription;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserSubscriptionManagerAdminIT extends AbstractTestWithData {

    //_id to use to create temporary subscriptions from single tests, deleted after each test
    public static final String TEMP_SUBSCRIPTION_ID = "9876";

    @After
    public void after() {
        DataSetTool.mongoCollection.deleteOne(Filters.eq("_id", TEMP_SUBSCRIPTION_ID));
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

        UserSubscription deleteSubscription = new UserSubscription();
        deleteSubscription.setDeleted(true);
        deleteSubscription.setId(TEMP_SUBSCRIPTION_ID);
        DataSetTool.mongoCollection.insertOne(deleteSubscription);

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

        UserSubscription subscription = new UserSubscription();
        subscription.setId(TEMP_SUBSCRIPTION_ID);
        subscription.setCreated_at(1564578987l);
        DataSetTool.mongoCollection.insertOne(subscription);

        when()
                .get(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("subscriptions[0].id", equalTo("1234"))
                .body("subscriptions[1].id", equalTo(TEMP_SUBSCRIPTION_ID));

        subscription.setCreated_at(1564578989l);
        DataSetTool.mongoCollection.replaceOne(Filters.eq("_id", TEMP_SUBSCRIPTION_ID), subscription);

        when()
                .get(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("subscriptions[0].id", equalTo(TEMP_SUBSCRIPTION_ID))
                .body("subscriptions[1].id", equalTo("1234"));

    }

}
