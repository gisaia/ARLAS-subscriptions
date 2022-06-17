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

package io.arlas.subscriptions.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import io.arlas.subscriptions.configuration.elastic.ElasticDBConfiguration;
import io.arlas.subscriptions.configuration.mongo.MongoDBConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

@JsonIgnoreProperties(ignoreUnknown = false)
public class ArlasSubscriptionManagerConfiguration extends ArlasSubscriptionsConfiguration {

    @JsonProperty(value = "trigger", required = true)
    public TriggerConfiguration triggerConfiguration;

    @JsonProperty(value = "mongo", required = true)
    public MongoDBConfiguration mongoDBConfiguration;

    @JsonProperty(value = "elastic", required = true)
    public ElasticDBConfiguration elasticDBConfiguration;

    @JsonProperty(value = "zipkin", required = true)
    public ZipkinFactory zipkinConfiguration;

    @JsonProperty(value = "swagger", required = true)
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    public void check() throws ArlasConfigurationException {
        elasticDBConfiguration.check();
    }
}
