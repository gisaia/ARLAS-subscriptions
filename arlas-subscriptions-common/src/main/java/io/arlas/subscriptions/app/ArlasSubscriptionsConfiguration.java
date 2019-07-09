package io.arlas.subscriptions.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class ArlasSubscriptionsConfiguration extends Configuration {
    @JsonProperty("kafka")
    public KafkaConfiguration kafkaConfiguration;
}
