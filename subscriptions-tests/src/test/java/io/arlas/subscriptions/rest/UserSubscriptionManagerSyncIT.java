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

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import io.arlas.subscriptions.AbstractTestWithData;
import io.arlas.subscriptions.DataSetTool;
import io.arlas.subscriptions.model.UserSubscription;
import org.junit.Test;

import java.io.IOException;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UserSubscriptionManagerSyncIT extends AbstractTestWithData {

    @Test
    public void testSync_withDataOnlyInMongo_shouldCopyToES() throws IOException {
        UserSubscription subscription = DataSetTool.mongoCollection.find(Filters.eq("_id", "1234")).first();
        subscription.title = "New title";
        DataSetTool.mongoCollection.replaceOne(Filters.eq("_id", "1234"), subscription);
        subscription.setId("2345");
        subscription.title = "other title";
        DataSetTool.mongoCollection.insertOne(subscription);

        when()
                .post("/admin/tasks/MongoDB-to-ES-sync")
                .then().statusCode(200);

        assertThat(DataSetTool.getUserSubscriptionFromES("1234").title, is("New title"));
        assertThat(DataSetTool.getUserSubscriptionFromES("2345").title, is("other title"));
    }

}
