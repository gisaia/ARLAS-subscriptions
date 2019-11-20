
<a name="definitions"></a>
## Definitions

<a name="error"></a>
### Error

|Name|Schema|
|---|---|
|**error**  <br>*optional*|string|
|**message**  <br>*optional*|string|
|**status**  <br>*optional*|integer (int32)|


<a name="hits"></a>
### Hits

|Name|Schema|
|---|---|
|**filter**  <br>*required*|string|
|**projection**  <br>*optional*|string|


<a name="link"></a>
### Link

|Name|Schema|
|---|---|
|**href**  <br>*required*|string|
|**method**  <br>*required*|string|
|**relation**  <br>*required*|string|
|**type**  <br>*required*|string|


<a name="subscription"></a>
### Subscription

|Name|Schema|
|---|---|
|**callback**  <br>*required*|string|
|**hits**  <br>*required*|[Hits](#hits)|
|**trigger**  <br>*required*|< string, object > map|


<a name="subscriptionlistresource"></a>
### SubscriptionListResource

|Name|Schema|
|---|---|
|**_links**  <br>*optional*|< string, [Link](#link) > map|
|**count**  <br>*optional*|integer (int32)|
|**subscriptions**  <br>*optional*|< [UserSubscriptionWithLinks](#usersubscriptionwithlinks) > array|
|**total**  <br>*optional*|integer (int32)|


<a name="usersubscription"></a>
### UserSubscription

|Name|Schema|
|---|---|
|**active**  <br>*required*|boolean|
|**created_at**  <br>*optional*  <br>*read-only*|integer (int64)|
|**created_by**  <br>*required*|string|
|**created_by_admin**  <br>*optional*  <br>*read-only*|boolean|
|**deleted**  <br>*optional*  <br>*read-only*|boolean|
|**expires_at**  <br>*required*|integer (int64)|
|**id**  <br>*optional*  <br>*read-only*|string|
|**modified_at**  <br>*optional*  <br>*read-only*|integer (int64)|
|**starts_at**  <br>*required*|integer (int64)|
|**subscription**  <br>*required*|[Subscription](#subscription)|
|**title**  <br>*required*|string|
|**userMetadatas**  <br>*optional*|< string, object > map|


<a name="usersubscriptionwithlinks"></a>
### UserSubscriptionWithLinks

|Name|Schema|
|---|---|
|**_links**  <br>*optional*|< string, [Link](#link) > map|
|**active**  <br>*required*|boolean|
|**created_at**  <br>*optional*  <br>*read-only*|integer (int64)|
|**created_by**  <br>*required*|string|
|**created_by_admin**  <br>*optional*  <br>*read-only*|boolean|
|**deleted**  <br>*optional*  <br>*read-only*|boolean|
|**expires_at**  <br>*required*|integer (int64)|
|**id**  <br>*optional*  <br>*read-only*|string|
|**modified_at**  <br>*optional*  <br>*read-only*|integer (int64)|
|**starts_at**  <br>*required*|integer (int64)|
|**subscription**  <br>*required*|[Subscription](#subscription)|
|**title**  <br>*required*|string|
|**userMetadatas**  <br>*optional*|< string, object > map|



