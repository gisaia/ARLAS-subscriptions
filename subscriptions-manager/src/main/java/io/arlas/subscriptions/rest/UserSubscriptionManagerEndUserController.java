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
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.model.SubscriptionListResource;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.model.UserSubscriptionWithLinks;
import io.arlas.subscriptions.model.response.Error;
import io.arlas.subscriptions.service.UserSubscriptionHALService;
import io.arlas.subscriptions.service.UserSubscriptionManagerService;
import io.arlas.subscriptions.utils.ResponseFormatter;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

import static io.arlas.subscriptions.app.ArlasSubscriptionsManager.MANAGER;

@Path("/subscriptions")
@Api(value = "/subscriptions", tags = {"end-user"})
public class UserSubscriptionManagerEndUserController extends UserSubscriptionManagerAbstractController {
    public final ArlasLogger logger = ArlasLoggerFactory.getLogger(UserSubscriptionManagerEndUserController.class, MANAGER);
    private static final String UNKNOWN_USER = "unknown";

    public UserSubscriptionManagerEndUserController(
            UserSubscriptionManagerService subscriptionManagerService,
            UserSubscriptionHALService halService,
            String identityHeader,
            String identityAdmin) {
        super(subscriptionManagerService, halService, identityHeader, identityAdmin);
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
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = SubscriptionListResource.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response getAll(@Context UriInfo uriInfo,
                           @Context HttpHeaders headers,
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
        logger.debug(String.format("User %s requests all subscriptions (before %d, after %d, active %s, started %b, expired %s, deleted %b, created-by-admin %b, page %d, size %d)",
                user, before, null, active, null, expired, false, false, page, size));
        Pair<Integer, List<UserSubscription>> subscriptionList = subscriptionManagerService.getAllUserSubscriptions(user, before, null, active, null, expired, false, null, page, size);
        SubscriptionListResource subscriptionListResource = halService.subscriptionListToResource(subscriptionList, uriInfo, page, size);
        return ResponseFormatter.getResultResponse(subscriptionListResource);
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
            response = UserSubscriptionWithLinks.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = UserSubscriptionWithLinks.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 404, message = "Subscription not found.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response get(@Context UriInfo uriInfo,
                        @Context HttpHeaders headers,
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
        logger.debug(String.format("User %s requests subscription %s", Optional.ofNullable(user).orElse(UNKNOWN_USER), id));
        UserSubscription userSubscription = subscriptionManagerService.getUserSubscription(id, Optional.ofNullable(user), false)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found for user " + user));

        return ResponseFormatter.getResultResponse(halService.subscriptionWithLinks(userSubscription, uriInfo));
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
            response = UserSubscriptionWithLinks.class
    )
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Subscription has been deleted.", response = UserSubscriptionWithLinks.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 404, message = "Subscription not found.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

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
        logger.debug(String.format("User %s deletes subscription %s", Optional.ofNullable(user).orElse(UNKNOWN_USER), id));
        UserSubscription userSubscription = subscriptionManagerService.getUserSubscription(id, Optional.ofNullable(user), true)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found for user " + user));
        subscriptionManagerService.deleteUserSubscription(userSubscription);

        return ResponseFormatter.getAcceptedResponse(new UserSubscriptionWithLinks(userSubscription));
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
            response = UserSubscriptionWithLinks.class
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Subscription has been registered", response = UserSubscriptionWithLinks.class),
            @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})
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
        logger.debug(String.format("User %s creates a new subscription", Optional.ofNullable(user).orElse(UNKNOWN_USER)));
        if (user != null && !user.equals(subscription.created_by)) {
            throw new ForbiddenException("New subscription does not belong to authenticated user " + user);
        }
        return ResponseFormatter.getCreatedResponse(uriInfo.getRequestUriBuilder().build(),
                halService.subscriptionWithLinks(subscriptionManagerService.postUserSubscription(subscription, false), uriInfo));
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
            response = UserSubscriptionWithLinks.class
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = UserSubscriptionWithLinks.class),
            @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 403, message = "Forbidden.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})
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
        logger.debug(String.format("User %s updates subscription %s", Optional.ofNullable(user).orElse(UNKNOWN_USER), id));
        UserSubscription oldUserSubscription = subscriptionManagerService.getUserSubscription(id, Optional.ofNullable(user), false)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found for user " + user));

        // we must ensure that:
        // - either identity control if OFF and both existing and updated subscription have the same creator
        // - or identity control is ON and the updated subscription has not changed the creator (if found, the existing sub has the good creator)
        if ( (user == null && !oldUserSubscription.created_by.equals(updUserSubscription.created_by)) ||
                (user != null && !user.equals(updUserSubscription.created_by)) ) {
            throw new ForbiddenException("Existing or updated subscription does not belong to authenticated user " + user);
        }
        return ResponseFormatter.getCreatedResponse(uriInfo.getRequestUriBuilder().build(),
                halService.subscriptionWithLinks(subscriptionManagerService.putUserSubscription(oldUserSubscription, updUserSubscription, Optional.ofNullable(user)), uriInfo));
    }

    private String getUser(HttpHeaders headers) throws UnauthorizedException, ForbiddenException {
        if (StringUtils.isEmpty(identityHeader)) {
            return null; // header configuration not defined -> no identity control
        } else {
            String userId = headers.getHeaderString(identityHeader);
            if (StringUtils.isEmpty(userId)) {
                throw new UnauthorizedException("Missing header " + identityHeader);
            }
            if (userId.equals(identityAdmin)) {
                throw new ForbiddenException("External endpoint forbidden to user " + identityAdmin);
            }
            return userId;
        }
    }

}
