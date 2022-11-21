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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.arlas.server.admin.health.ElasticsearchHealthCheck;
import io.arlas.subscriptions.db.elastic.ElasticDBFactoryConnection;
import io.arlas.subscriptions.db.elastic.ElasticDBManaged;
import io.arlas.subscriptions.db.mongo.MongoDBFactoryConnection;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.exception.ArlasSubscriptionsExceptionMapper;
import io.arlas.subscriptions.exception.ConstraintViolationExceptionMapper;
import io.arlas.subscriptions.exception.IllegalArgumentExceptionMapper;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.healthcheck.MongoHealthCheck;
import io.arlas.subscriptions.configuration.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.rest.UserSubscriptionManagerAdminController;
import io.arlas.subscriptions.rest.UserSubscriptionManagerEndUserController;
import io.arlas.subscriptions.service.UserSubscriptionHALService;
import io.arlas.subscriptions.service.UserSubscriptionManagerService;
import io.arlas.subscriptions.task.MongoDBToESSync;
import io.arlas.subscriptions.utils.PrettyPrintFilter;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import java.io.IOException;

public class ArlasSubscriptionsManager extends Application<ArlasSubscriptionManagerConfiguration> {
    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(ArlasSubscriptionsManager.class, MANAGER);

    public final static String MANAGER = "MANAGER";

    public static void main(String... args) throws Exception {
        new ArlasSubscriptionsManager().run(args);
    }

    @Override
    public void initialize(Bootstrap<ArlasSubscriptionManagerConfiguration> bootstrap) {
        bootstrap.registerMetrics();
        bootstrap.getObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
        bootstrap.addBundle(new SwaggerBundle<>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ArlasSubscriptionManagerConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    public void run(ArlasSubscriptionManagerConfiguration configuration, Environment environment) throws Exception {

        logger.info("Raw configuration: " + (new ObjectMapper()).writer().writeValueAsString(configuration));
        configuration.check();
        logger.info("Checked configuration: " + (new ObjectMapper()).writer().writeValueAsString(configuration));

        final MongoDBFactoryConnection mongoDBFactoryConnection = new MongoDBFactoryConnection(configuration.mongoDBConfiguration);
        final ElasticDBFactoryConnection elasticDBFactoryConnection = new ElasticDBFactoryConnection(configuration.elasticDBConfiguration);

        try {
            final MongoDBManaged mongoDBManaged = new MongoDBManaged(mongoDBFactoryConnection.getClient());
            final ElasticDBManaged elasticDBManaged = new ElasticDBManaged(elasticDBFactoryConnection.getClient());
            final UserSubscriptionManagerService subscriptionManagerService = new UserSubscriptionManagerService(configuration, mongoDBManaged, elasticDBManaged);

            environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            environment.getObjectMapper().configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
            environment.jersey().register(MultiPartFeature.class);
            environment.jersey().register(PrettyPrintFilter.class);
            environment.jersey().register(new ArlasSubscriptionsExceptionMapper(MANAGER));
            environment.jersey().register(new IllegalArgumentExceptionMapper(MANAGER));
            environment.jersey().register(new JsonProcessingExceptionMapper());
            environment.jersey().register(new ConstraintViolationExceptionMapper(MANAGER));
            environment.lifecycle().manage(mongoDBManaged);
            environment.lifecycle().manage(elasticDBManaged);
            registerControllers(configuration, environment, subscriptionManagerService);
            environment.healthChecks().register("elasticsearch", new ElasticsearchHealthCheck(elasticDBManaged.esClient));
    	    environment.healthChecks().register("mongo", new MongoHealthCheck(mongoDBManaged.mongoClient, configuration.mongoDBConfiguration.database));

            //tasks
            environment.admin().addTask(new MongoDBToESSync(subscriptionManagerService));

        } catch (IOException|ArlasSubscriptionsException e) {
            logger.fatal(e.getMessage());
            System.exit(1);
        }
    }

    private void registerControllers(ArlasSubscriptionManagerConfiguration configuration,
                                     Environment environment,
                                     UserSubscriptionManagerService subscriptionManagerService) {

        UserSubscriptionHALService halService = new UserSubscriptionHALService();

        UserSubscriptionManagerEndUserController subscriptionsManagerEndUserController = new UserSubscriptionManagerEndUserController(
                subscriptionManagerService,
                halService,
                configuration.identityConfiguration.identityHeader,
                configuration.identityConfiguration.identityAdmin);
        environment.jersey().register(subscriptionsManagerEndUserController);

        UserSubscriptionManagerAdminController subscriptionsManagerAdminController = new UserSubscriptionManagerAdminController(
                subscriptionManagerService,
                halService,
                configuration.identityConfiguration.identityHeader,
                configuration.identityConfiguration.identityAdmin);
        environment.jersey().register(subscriptionsManagerAdminController);
    }
}
