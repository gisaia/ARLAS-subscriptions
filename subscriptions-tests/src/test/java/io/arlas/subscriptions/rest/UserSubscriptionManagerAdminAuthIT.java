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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserSubscriptionManagerAdminAuthIT extends AbstractTestWithData {

    //
    // Tests with identity header
    //
    @Test
    public void test01GetAllUserSubscriptionsWithIdentityHeader() {
        given().header(identityHeader, "admin")
                .when().get(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(401);
    }

    @Test
    public void test02PostSubscriptionsWithIdentityHeader() {
        given().header(identityHeader, "admin")
                .contentType("application/json")
                .body(generateTestSubscription())
                .when().post(arlasSubManagerPath + "admin/subscriptions/")
                .then().statusCode(401);
    }

    @Test
    public void test03GetASubscriptionsWithIdentityHeader() {
        given().header(identityHeader, "admin")
                .when().get(arlasSubManagerPath + "admin/subscriptions/1234")
                .then().statusCode(401);
        }

    }
