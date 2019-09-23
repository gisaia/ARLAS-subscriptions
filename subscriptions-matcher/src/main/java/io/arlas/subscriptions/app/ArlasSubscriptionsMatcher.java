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

import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.service.ManagedKafkaConsumers;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.kafka.common.KafkaException;

public class ArlasSubscriptionsMatcher extends Application<ArlasSubscriptionsMatcherConfiguration> {
    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(ArlasSubscriptionsMatcher.class, MATCHER);

    public final static String MATCHER = "MATCHER";

    public static void main(String... args) throws Exception {
        new ArlasSubscriptionsMatcher().run(args);
    }

    @Override
    public void initialize(Bootstrap<ArlasSubscriptionsMatcherConfiguration> bootstrap) {
        bootstrap.registerMetrics();
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(ArlasSubscriptionsMatcherConfiguration configuration, Environment environment) {
        try {
            ManagedKafkaConsumers consumersManager = new ManagedKafkaConsumers(configuration);
            environment.lifecycle().manage(consumersManager);
        } catch (KafkaException e) {
            logger.fatal("Kafka problem: " + e.getMessage() + "(" + e.getCause().getMessage() + ")");
            System.exit(1);
        }
    }
}
