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

package io.arlas.subscriptions.db.elastic;

import com.mongodb.client.MongoClient;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.client.Client;

public class ElasticDBManaged implements Managed {


    public Client esClient;

    public ElasticDBManaged(final Client esClient) {
        this.esClient = esClient;
    }

    @Override
    public void start() throws ArlasSubscriptionsException {
    }

    @Override
    public void stop() throws ArlasSubscriptionsException {
        esClient.close();
    }
}
