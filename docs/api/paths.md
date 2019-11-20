
<a name="paths"></a>
## Resources

<a name="admin_resource"></a>
### Admin
Optional endpoints to manage all subscriptions as an administrator of the service.


<a name="post"></a>
#### Register a new subscription
```
POST /admin/subscriptions
```


##### Description
Register a subscription for further notification.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**subscription**  <br>*required*|Subscription description|[UserSubscription](#usersubscription)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**201**|Subscription has been registered|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**400**|JSON parameter malformed.|[Error](#error)|
|**401**|Unauthorized.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="getall"></a>
#### List all available subscriptions
```
GET /admin/subscriptions
```


##### Description
Return the list of all registered subscriptions from the latest created to the earliest.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**active**  <br>*optional*|Filter subscriptions whether they are active or not (returns all if missing, 'active' if 'true', 'not active' if 'false').|boolean||
|**Query**|**after**  <br>*optional*|Retrieve subscriptions created after given timestamp.|integer (int64)||
|**Query**|**before**  <br>*optional*|Retrieve subscriptions created before given timestamp.|integer (int64)||
|**Query**|**created-by**  <br>*optional*|Filter subscriptions by creator's identifier|string||
|**Query**|**created-by-admin**  <br>*optional*|Filter subscriptions whether they have been created by admin or not (returns all if missing, 'created_by_admin' if 'true', 'not created_by_admin' if 'false').|boolean||
|**Query**|**deleted**  <br>*optional*|Filter subscriptions whether they are deleted or not.|boolean|`"true"`|
|**Query**|**expired**  <br>*optional*|Filter subscriptions whether they are expired or not (returns all if missing, 'expired' if 'true', 'not expired' if 'false').|boolean||
|**Query**|**page**  <br>*optional*|Page ID|integer (int32)|`1`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**size**  <br>*optional*|Page Size|integer (int32)|`10`|
|**Query**|**started**  <br>*optional*|Filter subscriptions whether they are started or not (returns all if missing, 'started' if 'true', 'not started' if 'false').|boolean||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[SubscriptionListResource](#subscriptionlistresource)|
|**401**|Unauthorized.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="get"></a>
#### Find subscription by ID
```
GET /admin/subscriptions/{id}
```


##### Description
Return a single subscription. Only creator can access their subscriptions.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|ID of subscription to return|string||
|**Query**|**deleted**  <br>*optional*|Filter subscriptions whether they are deleted or not.|boolean|`"true"`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**401**|Unauthorized.|[Error](#error)|
|**404**|Subscription not found.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="put"></a>
#### Update an existing subscription
```
PUT /admin/subscriptions/{id}
```


##### Description
Update an existing subscription.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|ID of subscription to return|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**subscription**  <br>*required*|Subscription description|[UserSubscription](#usersubscription)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**201**|Successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**400**|JSON parameter malformed.|[Error](#error)|
|**401**|Unauthorized.|[Error](#error)|
|**404**|Not Found Error.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="delete"></a>
#### Delete a subscription
```
DELETE /admin/subscriptions/{id}
```


##### Description
Mark a subscription as deleted.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|Subscription ID to delete|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**202**|Subscription has been deleted.|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**401**|Unauthorized.|[Error](#error)|
|**404**|Subscription not found.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="end-user_resource"></a>
### End-user
Standard endpoints to manage one's subscriptions as an end-user.


<a name="post_1"></a>
#### Register a new subscription
```
POST /subscriptions
```


##### Description
Register a subscription for further notification.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**subscription**  <br>*required*|Subscription description|[UserSubscription](#usersubscription)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**201**|Subscription has been registered|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**400**|JSON parameter malformed.|[Error](#error)|
|**401**|Unauthorized.|[Error](#error)|
|**403**|Forbidden.|[Error](#error)|
|**404**|Not Found Error.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="getall_1"></a>
#### List all available subscriptions
```
GET /subscriptions
```


##### Description
Return the list of all registered subscriptions that are available for current user from the latest created to the earliest.
Only current user's subscriptions that are not deleted are listed.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**active**  <br>*optional*|Filter subscriptions whether they are active or not (returns all if missing, 'active' if 'true', 'inactive' if 'false').|boolean||
|**Query**|**before**  <br>*optional*|Retrieve subscriptions created before given timestamp.|integer (int64)||
|**Query**|**expired**  <br>*optional*|Filter subscriptions whether they are expired or not (returns all if missing, 'expired' if 'true', 'not expired' if 'false').|boolean||
|**Query**|**page**  <br>*optional*|Page ID|integer (int32)|`1`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**size**  <br>*optional*|Page Size|integer (int32)|`10`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[SubscriptionListResource](#subscriptionlistresource)|
|**401**|Unauthorized.|[Error](#error)|
|**403**|Forbidden.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="get_1"></a>
#### Find subscription by ID
```
GET /subscriptions/{id}
```


##### Description
Return a single subscription. Only creator can access their subscriptions.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|ID of subscription to return|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**401**|Unauthorized.|[Error](#error)|
|**403**|Forbidden.|[Error](#error)|
|**404**|Subscription not found.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="put_1"></a>
#### Update an existing subscription
```
PUT /subscriptions/{id}
```


##### Description
Update an existing subscription. Only creator can update their own subscriptions.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|ID of subscription to return|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**subscription**  <br>*required*|Subscription description|[UserSubscription](#usersubscription)||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**201**|Successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**400**|JSON parameter malformed.|[Error](#error)|
|**401**|Unauthorized.|[Error](#error)|
|**403**|Forbidden.|[Error](#error)|
|**404**|Not Found Error.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="delete_1"></a>
#### Delete a subscription
```
DELETE /subscriptions/{id}
```


##### Description
Mark a subscription as deleted. Only creator can delete their own subscriptions.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|Subscription ID to delete|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**202**|Subscription has been deleted.|[UserSubscriptionWithLinks](#usersubscriptionwithlinks)|
|**401**|Unauthorized.|[Error](#error)|
|**403**|Forbidden.|[Error](#error)|
|**404**|Subscription not found.|[Error](#error)|
|**503**|Arlas Subscriptions Manager Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`



