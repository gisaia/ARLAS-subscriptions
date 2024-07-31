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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;

import static io.arlas.subscriptions.app.ArlasSubscriptionsManager.MANAGER;

@Path("/subscriptions")
@Tag(name="end-user", description = "Standard endpoints to manage one's subscriptions as an end-user.")
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
    @Operation(
            summary = "List all available subscriptions",
            description = """
                    Return the list of all registered subscriptions that are available for current user from the latest created to the earliest.
                    Only current user's subscriptions that are not deleted are listed."""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = SubscriptionListResource.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response getAll(@Context UriInfo uriInfo,
                           @Context HttpHeaders headers,

                           // ----------------------------------------------------
                           // ----------------------- FORM -----------------------
                           // ----------------------------------------------------
                           @Parameter(name = "before",
                                   description = "Retrieve subscriptions created before given timestamp.")
                           @QueryParam(value = "before") Long before,

                           @Parameter(name = "active",
                                   description = "Filter subscriptions whether they are active or not (returns all if missing, 'active' if 'true', 'inactive' if 'false').")
                           @QueryParam(value = "active") Boolean active,

                           @Parameter(name = "expired",
                                   description = "Filter subscriptions whether they are expired or not (returns all if missing, 'expired' if 'true', 'not expired' if 'false').")
                           @QueryParam(value = "expired") Boolean expired,

                           @Parameter(name = "pretty",
                                   description = "Pretty print",
                                   schema = @Schema(defaultValue = "false"))
                           @QueryParam(value = "pretty") Boolean pretty,

                           @Parameter(name = "size",
                                   description = "Page Size",
                                   schema = @Schema(type="integer", minimum = "1", defaultValue = "10"))
                           @DefaultValue("10")
                           @QueryParam(value = "size") Integer size,

                           @Parameter(name = "page",
                                   description = "Page ID",
                                   schema = @Schema(type="integer", minimum = "1", defaultValue = "1"))
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
    @Operation(
            summary = "Find subscription by ID",
            description = "Return a single subscription. Only creator can access their subscriptions."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = UserSubscriptionWithLinks.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response get(@Context UriInfo uriInfo,
                        @Context HttpHeaders headers,

                        // ----------------------------------------------------
                        // ----------------------- FORM -----------------------
                        // ----------------------------------------------------
                        @Parameter(name = "id",
                                description = "ID of subscription to return",
                                required = true)
                        @PathParam(value = "id") String id,

                        @Parameter(name = "pretty", description = "Pretty print",
                                schema = @Schema(defaultValue = "false"))
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
    @Operation(
            summary = "Delete a subscription",
            description = "Mark a subscription as deleted. Only creator can delete their own subscriptions."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Subscription has been deleted.",
                    content = @Content(schema = @Schema(implementation = UserSubscriptionWithLinks.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response delete(@Context HttpHeaders headers,

                           // ----------------------------------------------------
                           // ----------------------- FORM -----------------------
                           // ----------------------------------------------------
                           @Parameter(name = "id",
                                   description = "Subscription ID to delete",
                                   required = true)
                           @PathParam(value = "id") String id,

                           @Parameter(name = "pretty", description = "Pretty print",
                                   schema = @Schema(defaultValue = "false"))
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
    @Operation(
            summary = "Register a new subscription",
            description = "Register a subscription for further notification."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription has been registered",
                    content = @Content(schema = @Schema(implementation = UserSubscriptionWithLinks.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Not Found Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response post(@Context UriInfo uriInfo,
                         @Context HttpHeaders headers,

                         // ----------------------------------------------------
                         // ----------------------- FORM -----------------------
                         // ----------------------------------------------------
                         @Parameter(name = "subscription",
                                 description = "Subscription description",
                                 required = true)
                         @NotNull @Valid UserSubscription subscription,

                         @Parameter(name = "pretty", description = "Pretty print",
                                 schema = @Schema(defaultValue = "false"))
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
    @Operation(
            summary = "Update an existing subscription",
            description = "Update an existing subscription. Only creator can update their own subscriptions."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = UserSubscriptionWithLinks.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Not Found Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response put(@Context UriInfo uriInfo,
                        @Context HttpHeaders headers,

                        // ----------------------------------------------------
                        // ----------------------- FORM -----------------------
                        // ----------------------------------------------------
                        @Parameter(name = "id",
                                description = "ID of subscription to return",
                                required = true)
                        @PathParam(value = "id") String id,

                        @Parameter(name = "subscription",
                                description = "Subscription description",
                                required = true)
                        @NotNull @Valid UserSubscription updUserSubscription,

                        @Parameter(name = "pretty", description = "Pretty print",
                                schema = @Schema(defaultValue = "false"))
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
                halService.subscriptionWithLinks(subscriptionManagerService.putUserSubscription(oldUserSubscription, updUserSubscription), uriInfo));
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
