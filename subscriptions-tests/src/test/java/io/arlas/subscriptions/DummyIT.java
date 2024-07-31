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

package io.arlas.subscriptions;

import io.arlas.client.api.ExploreApi;
import io.arlas.client.model.Hits;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;


public class DummyIT extends AbstractTestContext {

    @BeforeClass
    public static void beforeClass() {
        try {
            DataSetTool.loadDataSet(true);
            DataSetTool.loadSubscriptions(true, true);
        } catch (IOException | ArlasSubscriptionsException | ArlasException e) {
            LOGGER.error("Could not load data in ES", e);
        }
    }

    @AfterClass
    public static void afterClass() {
        DataSetTool.clearDataSet();
        DataSetTool.clearSubscriptions(true);
    }

    @Test
    public void testArlasCollection() throws Exception {

        //GEODATA SEARCH REQUEST
        Hits hits = new ExploreApi(DataSetTool.getApiClient())
                .search(DataSetTool.COLLECTION_GEODATA_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        Assert.assertEquals("Search response returns 10 hits", Long.valueOf(10L), hits.getNbhits());
        Assert.assertEquals("Search response has 595 hits", Long.valueOf(595L), hits.getTotalnb());

        //SUBSCRIPTIONS SEARCH REQUEST
        Hits subscriptionsHits = new ExploreApi(DataSetTool.getApiClient())
                .search(DataSetTool.COLLECTION_SUBSCRIPTIONS_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        Assert.assertEquals("Subscriptions search returns 1 hits", Long.valueOf(1L), subscriptionsHits.getNbhits());
        Assert.assertEquals("Subscriptions search has 1 hits", Long.valueOf(1L), subscriptionsHits.getTotalnb());
    }
}