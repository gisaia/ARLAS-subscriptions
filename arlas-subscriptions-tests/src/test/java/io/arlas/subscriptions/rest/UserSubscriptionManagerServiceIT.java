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

import io.arlas.subscriptions.AbstractTestContext;
import io.arlas.subscriptions.model.UserSubscription;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class UserSubscriptionManagerServiceIT extends AbstractTestContext {


    @Test
    public void testGetAllUserSubscriptions() throws Exception {
        // GET all collections
        getAllUserSubscriptions(emptyArray());
    }

    @Test
    public void testPostUserSubscription() throws Exception{
        
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("created_by","John Doe");
        jsonAsMap.put("active","true");
        jsonAsMap.put("expires_at","-1");
        UserSubscription.Hits hits = new UserSubscription.Hits();
        hits.filter="filter";
        hits.projection="projection";
        UserSubscription.Subscription subscription = new UserSubscription.Subscription();
        Map<String, String> trigger = new HashMap<>();
        trigger.put("correlationId","2007");
        subscription.callback ="callback";
        subscription.hits =hits;
        subscription.trigger = trigger;
        jsonAsMap.put("subscription",subscription);
        given().contentType("application/json")
                .when().body(jsonAsMap)
                .post(arlasSubManagerPath + "subscriptions/")
                .then().statusCode(200);

        getAllUserSubscriptions(hasSize(1));
        getAllUserSubscriptions(hasItem(hasProperty("created_by",hasValue("John Doe"))));

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