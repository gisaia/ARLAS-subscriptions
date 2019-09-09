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
import com.mongodb.MongoClientException;
import com.mongodb.client.MongoClient;
import org.bson.Document;

public class MongoHealthCheck extends HealthCheck {

    private final MongoClient mongoClient;

    public MongoHealthCheck(MongoClient mongoClient) {
        super();
        this.mongoClient = mongoClient;
    }

    /**
     * Checks if the system database, which exists in all MongoDB instances can be reached.
     * This is a mix from
     * https://github.com/eeb/dropwizard-mongo/blob/master/dropwizard-mongo/src/main/java/com/eeb/dropwizardmongo/health/MongoHealthCheck.java
     * https://jira.mongodb.org/browse/JAVA-1762?focusedCommentId=881556&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-881556
     * @return A Result object
     * @throws Exception
     */
    @Override
    protected Result check() throws Exception {

        try {
            mongoClient.getDatabase("system").runCommand(Document.parse("{ dbStats: 1, scale: 1 }"));
        }catch(MongoClientException ex) {
            return Result.unhealthy(ex.getMessage());
        }
        return Result.healthy();
    }

}