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

import io.arlas.subscriptions.service.ManagedKafkaConsumers;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArlasSubscriptionsMatcher extends Application<ArlasSubscriptionsConfiguration> {
    Logger LOGGER = LoggerFactory.getLogger(ArlasSubscriptionsMatcher.class);

    public static void main(String... args) throws Exception {
        new ArlasSubscriptionsMatcher().run(args);
    }

    @Override
    public void initialize(Bootstrap<ArlasSubscriptionsConfiguration> bootstrap) {
        bootstrap.registerMetrics();
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(ArlasSubscriptionsConfiguration configuration, Environment environment) {
        ManagedKafkaConsumers consumersManager = new ManagedKafkaConsumers(configuration);
        environment.lifecycle().manage(consumersManager);
    }
}
