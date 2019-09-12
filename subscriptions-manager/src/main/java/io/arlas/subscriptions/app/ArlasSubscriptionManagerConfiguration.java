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

package io.arlas.subscriptions.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.model.elastic.ElasticDBConnection;
import io.arlas.subscriptions.model.mongo.MongoDBConnection;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import static io.arlas.subscriptions.app.ArlasSubscriptionsManager.MANAGER;

public class ArlasSubscriptionManagerConfiguration extends ArlasSubscriptionsConfiguration {
    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(ArlasSubscriptionManagerConfiguration.class, MANAGER);

    @JsonProperty("mongo")
    public MongoDBConnection mongoDBConnection;

    public MongoDBConnection getMongoDBConnection() { return mongoDBConnection;}

    @JsonProperty("elastic")
    public ElasticDBConnection elasticDBConnection;

    public ElasticDBConnection getElasticDBConnection() { return elasticDBConnection;}

    @JsonProperty("zipkin")
    public ZipkinFactory zipkinConfiguration;

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    public void check() throws ArlasSubscriptionsException {
        if (mongoDBConnection == null) {
            logger.fatal("MongoDB configuration missing in config file.");
            throw new ArlasSubscriptionsException("mongoDB configuration missing in config file");
        }
        if (elasticDBConnection == null) {
            logger.fatal("Elastic configuration missing in config file");
            throw new ArlasSubscriptionsException();
        }
        if (zipkinConfiguration == null) {
            logger.fatal("Zipkin configuration missing in config file");
            throw new ArlasSubscriptionsException();
        }
        if (swaggerBundleConfiguration == null) {
            logger.fatal("Swagger configuration missing in config file.");
            throw new ArlasSubscriptionsException("Swagger configuration missing in config file");

        }
    }
}
