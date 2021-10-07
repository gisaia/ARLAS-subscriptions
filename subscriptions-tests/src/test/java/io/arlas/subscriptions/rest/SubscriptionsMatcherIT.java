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

import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.subscriptions.AbstractTestContext;
import io.arlas.subscriptions.DataSetTool;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.NotificationOrder;
import io.arlas.subscriptions.tools.KafkaTool;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SubscriptionsMatcherIT extends AbstractTestContext {
    static Logger LOGGER = LoggerFactory.getLogger(SubscriptionsMatcherIT.class);
    private static final KafkaTool kafkaTool = new KafkaTool();

    @BeforeClass
    public static void beforeClass() {
        kafkaTool.init();
        new Thread(kafkaTool).start();
        try {
            DataSetTool.loadDataSet(true);
            DataSetTool.loadSubscriptions(false);
        } catch (IOException | ArlasSubscriptionsException | ArlasException e) {
            LOGGER.error("Could not load data in ES", e);
        }
    }

    @AfterClass
    public static void afterClass() {
        kafkaTool.stop();
        DataSetTool.clearDataSet();
        DataSetTool.clearSubscriptions(false);
    }

    @Test
    public void testMatchingSubscriptionEventWithExistingProductIndex() throws Exception {

        String  event =
                "{\"object\": {\"id\": \"ID__10__30DI\",\"geometry\": {\"type\" : \"Polygon\",\"coordinates\" : [[[-11,-29],[-9,-29],[-9,-31],[-11,-31],[-11,-29]]]},\"job\": \"Brain Scientist\",\"event\": \"UPDATE\"}}";
        kafkaTool.produce(event);
        Thread.sleep(10000);

        // check that all the messages were received
        NotificationOrder received = kafkaTool.consume(5, TimeUnit.SECONDS);
        int nbNotifReceived = 0;
        while(received != null) {
            nbNotifReceived++;
            checkNotificationOrder(received);
            received = kafkaTool.consume(10, TimeUnit.SECONDS);
        }

        assertThat(nbNotifReceived,is(1));
    }

    public void checkNotificationOrder(NotificationOrder notifOrder) {
        assertNotNull(notifOrder.get("object"));
        assertThat(((Map<String,Object>)notifOrder.get("object")).get("id"), is("ID__10__30DI"));
        assertThat(((Map<String,Object>)notifOrder.get("object")).get("job"), is("Brain Scientist"));
    }
}
