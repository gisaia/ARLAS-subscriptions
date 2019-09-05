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
import io.arlas.subscriptions.exception.ForbiddenException;
import io.arlas.subscriptions.exception.NotFoundException;
import io.arlas.subscriptions.exception.UnauthorizedException;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.model.response.Error;
import io.arlas.subscriptions.service.UserSubscriptionManagerService;
import io.arlas.subscriptions.utils.ResponseFormatter;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/subscriptions")
@Api(value = "/subscriptions", tags = {"end-user"})
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "ARLAS", url = "https://arlas.io/"),
                title = "ARLAS Subscriptions Manager API",
                description = "Manage ARLAS subscriptions on ARLAS collections' events.",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "API_VERSION"),
        tags = {@Tag(name="end-user", description = "Standard endpoints to manage one's subscriptions as an end-user."),
                @Tag(name="admin", description = "Optional endpoints to manage all subscriptions as an administrator of the service.")}
        )
public class UserSubscriptionManagerController {
    public Logger LOGGER = LoggerFactory.getLogger(UserSubscriptionManagerController.class);
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";
    private final UserSubscriptionManagerService subscriptionManagerService;
    private final String identityHeader;
    private final String identityAdmin;

    public UserSubscriptionManagerController(UserSubscriptionManagerService subscriptionManagerService, String identityHeader, String identityAdmin) {
        this.subscriptionManagerService = subscriptionManagerService;
        this.identityHeader = identityHeader;
        this.identityAdmin = identityAdmin;
    }

    @Timed
    @Path("/")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "List all available subscriptions",
            produces = UTF8JSON,
            notes = "Return the list of all registered subscriptions that are available " +
                    "for current user from the latest created to the earliest.\n" +
                    "Only current user's subscriptions that are not deleted are listed.",
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = UserSubscription.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response getAll(@Context HttpHeaders headers,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "before", value = "Retrieve subscriptions created before given timestamp.",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "before") Long before,
            @ApiParam(name = "active", value = "Filter subscriptions whether they are active or not (returns all if missing, 'active' if 'true', 'inactive' if 'false').",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "active") Boolean active,
            @ApiParam(name = "expired", value = "Filter subscriptions whether they are expired or not (returns all if missing, 'expired' if 'true', 'not expired' if 'false').",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "expired") Boolean expired,
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,
            @ApiParam(name = "size", value = "Page Size",
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("10")
            @QueryParam(value = "size") Integer size,
            @ApiParam(name = "page", value = "Page ID",
                    defaultValue = "1",
                    allowableValues = "range[1, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("1")
            @QueryParam(value = "page") Integer page
    ) throws ArlasSubscriptionsException {
        String user = getUser(headers);
        List<UserSubscription> userSubscriptions = subscriptionManagerService.getAllUserSubscriptions(user, before, active, expired, false, page, size);
        return ResponseFormatter.getResultResponse(userSubscriptions);
    }

    @Timed
    @Path("{id}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Find subscription by ID",
            produces = UTF8JSON,
            notes = "Return a single subscription. " +
                    "Only creator can access their subscriptions.",
            consumes = UTF8JSON,
            response = UserSubscription.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = UserSubscription.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 404, message = "Subscription not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response get(@Context HttpHeaders headers,
            @ApiParam(
                    name = "id",
                    value = "ID of subscription to return",
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
        UserSubscription userSubscription = subscriptionManagerService.getUserSubscription(user, id, false)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found for user " + user));

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
            notes = "Mark a subscription as deleted. " +
                    "Only creator can delete their own subscriptions.",
            consumes = UTF8JSON,
            response = UserSubscription.class
    )
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Subscription has been deleted.", response = UserSubscription.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 404, message = "Subscription not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response delete(@Context HttpHeaders headers,
                        @ApiParam(
                                name = "id",
                                value = "Subscription ID to delete",
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
        UserSubscription userSubscription = subscriptionManagerService.getUserSubscription(user, id, false)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found for user " + user));
        subscriptionManagerService.deleteUserSubscription(userSubscription);

        return ResponseFormatter.getAcceptedResponse(userSubscription);
    }

    @Path("/")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Register a new subscription",
            produces = UTF8JSON,
            notes = "Register a subscription for further notification.",
            consumes = UTF8JSON,
            response = UserSubscription.class
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Subscription has been registered", response = UserSubscription.class),
            @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})
    public Response post(@Context UriInfo uriInfo,
            @Context HttpHeaders headers,
            @ApiParam(name = "subscription",
                    value = "Subscription description",
                    required = true)
            @NotNull @Valid UserSubscription subscription,

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
        if (user != null && !user.equals(subscription.created_by)) {
            throw new ForbiddenException("New subscription does not belong to authenticated user " + user);
        }
        return ResponseFormatter.getCreatedResponse(uriInfo.getRequestUriBuilder().build(),subscriptionManagerService.postUserSubscription(subscription, false));
    }

    @Path("{id}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Update an existing subscription",
            produces = UTF8JSON,
            notes = "Update an existing subscription. " +
                    "Only creator can update their own subscriptions.",
            consumes = UTF8JSON,
            response = UserSubscription.class
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = UserSubscription.class),
            @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Subscriptions Manager Error.", response = Error.class)})
    public Response put(@Context UriInfo uriInfo,
                        @Context HttpHeaders headers,
                        @ApiParam(
                                name = "id",
                                value = "ID of subscription to return",
                                allowMultiple = false,
                                required = true)
                        @PathParam(value = "id") String id,
                        @ApiParam(name = "subscription",
                                value = "Subscription description",
                                required = true)
                        @NotNull @Valid UserSubscription updUserSubscription,

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
        UserSubscription oldUserSubscription = subscriptionManagerService.getUserSubscription(user, id, false)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found for user " + user));

        // we must ensure that:
        // - either identity control if OFF and both existing and updated subscription have the same creator
        // - or identity control is ON and the updated subscription has not changed the creator (if found, the existing sub has the good creator)
        if ( (user == null && !oldUserSubscription.created_by.equals(updUserSubscription.created_by)) ||
                (user != null && !user.equals(updUserSubscription.created_by)) ) {
            throw new ForbiddenException("Existing or updated subscription does not belong to authenticated user " + user);
        }
        return ResponseFormatter.getCreatedResponse(uriInfo.getRequestUriBuilder().build(),
                subscriptionManagerService.putUserSubscription(user, oldUserSubscription, updUserSubscription));
    }

    private String getUser(HttpHeaders headers) throws UnauthorizedException, ForbiddenException {
        if (StringUtils.isEmpty(identityHeader)) {
            return null; // header configuration not defined -> no identity control
        } else {
            String userId = headers.getHeaderString(identityHeader);
            if (StringUtils.isEmpty(userId)) {
                throw new UnauthorizedException();
            }
            if (userId.equals(identityAdmin)) {
                throw new ForbiddenException();
            }
            return userId;
        }
    }
}
