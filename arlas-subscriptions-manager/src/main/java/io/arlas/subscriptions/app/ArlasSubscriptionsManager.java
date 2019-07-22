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
import io.arlas.subscriptions.rest.SubscriptionsManagerController;
import io.arlas.subscriptions.service.SubscriptionManagerService;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ArlasSubscriptionsManager extends Application<ArlasSubscriptionsConfiguration> {

    public static void main(String... args) throws Exception {
        new ArlasSubscriptionsManager().run(args);
    }

    @Override
    public void initialize(Bootstrap<ArlasSubscriptionsConfiguration> bootstrap) {
        bootstrap.registerMetrics();
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }
    @Override
    public void run(ArlasSubscriptionsConfiguration configuration, Environment environment) throws Exception {

        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        SubscriptionManagerService subscriptionManagerService = new SubscriptionManagerService(configuration);
        SubscriptionsManagerController subscriptionsManagerController = new SubscriptionsManagerController(subscriptionManagerService);
        environment.jersey().register(subscriptionsManagerController);
    }
}
