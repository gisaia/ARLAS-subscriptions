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

package io.arlas.subscriptions.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.arlas.subscriptions.app.KafkaConfiguration;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class KafkaHealthCheck extends HealthCheck {
    private final AdminClient adminClient;
    private final String name;

    public KafkaHealthCheck(final KafkaConfiguration configuration, final String name) {
        final Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.bootstrapServers);
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        this.adminClient = AdminClient.create(props);
        this.name = requireNonNull(name);
    }

    @Override
    protected Result check() throws Exception {
        try {
            final DescribeClusterResult response = adminClient.describeCluster();

            final boolean nodesNotEmpty = !response.nodes().get().isEmpty();
            final boolean clusterIdAvailable = response.clusterId() != null;
            final boolean aControllerExists = response.controller().get() != null;

            final List<String> errors = new ArrayList<>();

            if (!nodesNotEmpty) {
                errors.add("no nodes found for " + name);
            }

            if (!clusterIdAvailable) {
                errors.add("no cluster id available for " + name);
            }

            if (!aControllerExists) {
                errors.add("no active controller exists for " + name);
            }

            if (!errors.isEmpty()) {
                final String errorMessage = String.join(",", errors);
                return Result.unhealthy(errorMessage);
            }

            return Result.healthy();
        } catch (final Exception e) {
            return Result.unhealthy("Error describing Kafka Cluster name={}", name, e);
        }
    }
}
