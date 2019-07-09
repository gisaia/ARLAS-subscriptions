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
