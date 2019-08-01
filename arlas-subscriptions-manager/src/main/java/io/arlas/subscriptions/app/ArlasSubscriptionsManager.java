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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.subscriptions.db.elastic.ElasticDBFactoryConnection;
import io.arlas.subscriptions.db.elastic.ElasticDBManaged;
import io.arlas.subscriptions.db.mongo.MongoDBFactoryConnection;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsExceptionMapper;
import io.arlas.subscriptions.exception.ConstraintViolationExceptionMapper;
import io.arlas.subscriptions.exception.IllegalArgumentExceptionMapper;
import io.arlas.subscriptions.rest.UserSubscriptionManagerController;
import io.arlas.subscriptions.service.UserSubscriptionManagerService;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.glassfish.jersey.media.multipart.MultiPartFeature;


public class ArlasSubscriptionsManager extends Application<ArlasSubscriptionManagerConfiguration> {

    public static void main(String... args) throws Exception {
        new ArlasSubscriptionsManager().run(args);
    }

    @Override
    public void initialize(Bootstrap<ArlasSubscriptionManagerConfiguration> bootstrap) {
        bootstrap.registerMetrics();
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
        bootstrap.addBundle(new SwaggerBundle<ArlasSubscriptionManagerConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ArlasSubscriptionManagerConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
        bootstrap.addBundle(new ZipkinBundle<ArlasSubscriptionManagerConfiguration>(getName()) {
            @Override
            public ZipkinFactory getZipkinFactory(ArlasSubscriptionManagerConfiguration configuration) {
                return configuration.zipkinConfiguration;
            }
        });
    }
    @Override
    public void run(ArlasSubscriptionManagerConfiguration configuration, Environment environment) throws Exception {

        final MongoDBFactoryConnection mongoDBFactoryConnection = new MongoDBFactoryConnection(configuration.getMongoDBConnection());
        final ElasticDBFactoryConnection elasticDBFactoryConnection = new ElasticDBFactoryConnection(configuration.getElasticDBConnection());

        final MongoDBManaged mongoDBManaged = new MongoDBManaged(mongoDBFactoryConnection.getClient());
        final ElasticDBManaged elasticDBManaged = new ElasticDBManaged(elasticDBFactoryConnection.getClient());

        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(new ArlasSubscriptionsExceptionMapper());
        environment.jersey().register(new IllegalArgumentExceptionMapper());
        environment.jersey().register(new JsonProcessingExceptionMapper());
        environment.jersey().register(new ConstraintViolationExceptionMapper());
        environment.lifecycle().manage(mongoDBManaged);
        UserSubscriptionManagerService subscriptionManagerService = new UserSubscriptionManagerService(configuration,mongoDBManaged,elasticDBManaged);
        UserSubscriptionManagerController subscriptionsManagerController = new UserSubscriptionManagerController(subscriptionManagerService);
        environment.jersey().register(subscriptionsManagerController);
    }
}
