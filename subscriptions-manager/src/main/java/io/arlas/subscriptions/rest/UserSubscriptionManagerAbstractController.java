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

package io.arlas.subscriptions.rest;

import io.arlas.subscriptions.service.UserSubscriptionHALService;
import io.arlas.subscriptions.service.UserSubscriptionManagerService;
import io.swagger.annotations.*;

import javax.ws.rs.core.MediaType;

@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "ARLAS", url = "https://arlas.io/"),
                title = "ARLAS Subscriptions Manager API",
                description = "Manage ARLAS subscriptions on ARLAS collections' events.",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "19.0.8"),
        tags = {@Tag(name="end-user", description = "Standard endpoints to manage one's subscriptions as an end-user."),
                @Tag(name="admin", description = "Optional endpoints to manage all subscriptions as an administrator of the service.")},
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS }
)
public abstract class UserSubscriptionManagerAbstractController {

    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";
    protected UserSubscriptionManagerService subscriptionManagerService;
    protected UserSubscriptionHALService halService;
    protected String identityHeader;
    protected String identityAdmin;

    public UserSubscriptionManagerAbstractController(
            UserSubscriptionManagerService subscriptionManagerService,
            UserSubscriptionHALService halService,
            String identityHeader,
            String identityAdmin) {
        this.subscriptionManagerService = subscriptionManagerService;
        this.halService = halService;
        this.identityHeader = identityHeader;
        this.identityAdmin = identityAdmin;
    }

}
