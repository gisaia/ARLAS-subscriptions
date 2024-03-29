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

package io.arlas.subscriptions.service;

import io.arlas.subscriptions.app.ArlasSubscriptionsMatcherConfiguration;
import io.arlas.subscriptions.kafka.NotificationOrderKafkaProducer;
import io.dropwizard.lifecycle.Managed;

public class ManagedKafkaConsumers implements Managed {
    private KafkaConsumerRunner consumerRunner;

    public ManagedKafkaConsumers(ArlasSubscriptionsMatcherConfiguration configuration, NotificationOrderKafkaProducer notificationOrderKafkaProducer) {
        this.consumerRunner = new KafkaConsumerRunner(configuration, notificationOrderKafkaProducer);
    }

    @Override
    public void start() {
        new Thread(this.consumerRunner).start();
    }

    @Override
    public void stop() {
        this.consumerRunner.stop();
    }
}
