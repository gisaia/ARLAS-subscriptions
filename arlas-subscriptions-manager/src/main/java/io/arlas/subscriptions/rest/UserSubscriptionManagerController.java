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

import com.codahale.metrics.annotation.Timed;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.exception.NotFoundException;
import io.arlas.subscriptions.exception.UnauthorizedException;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.model.response.Error;
import io.arlas.subscriptions.service.UserSubscriptionManagerService;
import io.arlas.subscriptions.utils.ResponseFormatter;
import io.swagger.annotations.*;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("/subscriptions")
@Api(value = "/subscriptions")
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                title = "ARLAS Subscriptions Manager API",
                description = "Manage ARLAS Subscriptions",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "API_VERSION"))
public class UserSubscriptionManagerController {
    public Logger LOGGER = LoggerFactory.getLogger(UserSubscriptionManagerController.class);
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";
    public UserSubscriptionManagerService subscriptionManagerService;

    public UserSubscriptionManagerController(UserSubscriptionManagerService subscriptionManagerService) {
        this.subscriptionManagerService = subscriptionManagerService;
    }

    @Timed
    @Path("/")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Get all subscriptions",
            produces = UTF8JSON,
            notes = "Get all subscriptions",
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = UserSubscription.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response getAll(
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasSubscriptionsException {
        List<UserSubscription> userSubscriptions = subscriptionManagerService.getAllUserSubscriptions();
        return ResponseFormatter.getResultResponse(userSubscriptions);
    }

    @Timed
    @Path("{id}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Get a subscription",
            produces = UTF8JSON,
            notes = "Get a subscription",
            consumes = UTF8JSON,
            response = UserSubscription.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = UserSubscription.class),
            @ApiResponse(code = 403, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 404, message = "Subscription not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response get(@Context HttpHeaders headers,
            @ApiParam(
                    name = "id",
                    value = "id",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "id") String id,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasSubscriptionsException {
        String user = getUser(headers);
        UserSubscription userSubscription = subscriptionManagerService.getUserSubscription(user, id).orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found for user " + user));

        return ResponseFormatter.getResultResponse(userSubscription);
    }

    @Timed
    @Path("{id}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Delete a subscription",
            produces = UTF8JSON,
            notes = "Delete a subscription",
            consumes = UTF8JSON,
            response = UserSubscription.class
    )
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Successful operation", response = UserSubscription.class),
            @ApiResponse(code = 403, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 404, message = "Subscription not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response delete(@Context HttpHeaders headers,
                        @ApiParam(
                                name = "id",
                                value = "id",
                                allowMultiple = false,
                                required = true)
                        @PathParam(value = "id") String id,
                        // --------------------------------------------------------
                        // ----------------------- FORM -----------------------
                        // --------------------------------------------------------
                        @ApiParam(name = "pretty", value = "Pretty print",
                                allowMultiple = false,
                                defaultValue = "false",
                                required = false)
                        @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasSubscriptionsException {
        String user = getUser(headers);
        UserSubscription userSubscription = subscriptionManagerService.getUserSubscription(user, id).orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found for user " + user));
        subscriptionManagerService.deleteUserSubscription(userSubscription);

        return ResponseFormatter.getAcceptedResponse(userSubscription);
    }

    @Path("/")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Add a configuration reference",
            produces = UTF8JSON,
            notes = "Add a configuration reference in the manager",
            consumes = UTF8JSON,
            response = UserSubscription.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = UserSubscription.class),
            @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
            @ApiResponse(code = 403, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})
    public Response post(
            @ApiParam(name = "userSubscription",
                    value = "userSubscription",
                    required = true)
            @NotNull @Valid UserSubscription userSubscription,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty

    ) throws ArlasSubscriptionsException, IOException, ParseException {

        return ResponseFormatter.getResultResponse(subscriptionManagerService.postUserSubscription(userSubscription));
    }

    private String getUser(HttpHeaders headers) throws UnauthorizedException {
        // TODO in issue #11
        return "gisaia";
    }
}
